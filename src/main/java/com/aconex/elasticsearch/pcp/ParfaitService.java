package com.aconex.elasticsearch.pcp;

import static com.google.common.collect.Maps.newHashMap;

import javax.measure.unit.SI;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.custardsource.parfait.CompositeMonitoringView;
import com.custardsource.parfait.Counter;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoredCounter;
import com.custardsource.parfait.MonitoredLongValue;
import com.custardsource.parfait.PollingMonitoredValue;
import com.custardsource.parfait.ValueSemantics;
import com.custardsource.parfait.dxm.IdentifierSourceSet;
import com.custardsource.parfait.dxm.PcpMmvWriter;
import com.custardsource.parfait.io.ByteCountingInputStream;
import com.custardsource.parfait.io.ByteCountingOutputStream;
import com.custardsource.parfait.jmx.JmxView;
import com.custardsource.parfait.pcp.EmptyTextSource;
import com.custardsource.parfait.pcp.MetricDescriptionTextSource;
import com.custardsource.parfait.pcp.MetricNameMapper;
import com.custardsource.parfait.pcp.PcpMonitorBridge;
import com.custardsource.parfait.spring.SelfStartingMonitoringView;
import com.custardsource.parfait.timing.EventTimer;
import com.custardsource.parfait.timing.LoggerSink;
import com.custardsource.parfait.timing.StepMeasurementSink;
import com.custardsource.parfait.timing.ThreadMetricSuite;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.shard.ShardId;


public class ParfaitService extends AbstractLifecycleComponent<Void> {

    public static final String SEARCH_EVENT_GROUP = "search";
    public static final String INDEX_EVENT_GROUP = "index";


    /**
     * TODO these 2 event groups are clashing, so preventing the other from starting properly.  We need an EventTimer per group by the looks of it.. ?
     */
    private static final List<String> EVENT_GROUPS = Lists.newArrayList();//Arrays.asList(INDEX_EVENT_GROUP, SEARCH_EVENT_GROUP);

    private final MonitorableRegistry monitorableRegistry;
    private final SelfStartingMonitoringView selfStartingMonitoringView;

    private static final int ELASTICSEARCH_PCP_CLUSTER_IDENTIFIER = 0xB01; /*NG BO1NG - funny... ok you had to be there*/
    private final Map<String, EventTimer> eventTimers = newHashMap();
    private int updateFrequency;

    public ParfaitService(Settings settings) {
        super(settings);
        monitorableRegistry = new MonitorableRegistry();
        this.updateFrequency = settings.getAsInt("pcp.polling.frequency", 5000);

        Boolean isData = settings.getAsBoolean("node.data", false);
        Boolean isClient = settings.getAsBoolean("node.client", false);
        boolean isServer = isData || !isClient;

        String nodeType = isServer ? "server" : "client";

        // TODO remove this debug rubbish
        this.logger.info("isData=%s, isClient=%s, isServer=%s, nodeType=%s\n", isData, isClient, isServer, nodeType);

        final PcpMmvWriter mmvWriter = new PcpMmvWriter("elasticsearch-" + nodeType + ".mmv", IdentifierSourceSet.DEFAULT_SET);
        mmvWriter.setClusterIdentifier(ELASTICSEARCH_PCP_CLUSTER_IDENTIFIER);

        final PcpMonitorBridge pcpMonitorBridge = new PcpMonitorBridge(mmvWriter, MetricNameMapper.PASSTHROUGH_MAPPER, new MetricDescriptionTextSource(), new EmptyTextSource());

        // TODO whoops, forgot that JmxView relies on the Spring @ManagedResource stuff to expose, so need to get passed in the JmxService
        final JmxView jmxView = new JmxView();
        final CompositeMonitoringView compositeMonitoringView = new CompositeMonitoringView(pcpMonitorBridge, jmxView);
        selfStartingMonitoringView = new SelfStartingMonitoringView(monitorableRegistry, compositeMonitoringView, 2000);

        LoggerSink loggerSink = new LoggerSink(getClass().getSimpleName());
        loggerSink.normalizeUnits(SI.NANO(SI.SECOND), SI.MILLI(SI.SECOND));

        List<StepMeasurementSink> sinks = Collections.<StepMeasurementSink>singletonList(loggerSink);
        boolean enableCpuCollection = true;
        boolean enableContentionCollection = false;


        for (String eventGroup : EVENT_GROUPS) {
            EventTimer eventTimer = new EventTimer("elasticsearch", monitorableRegistry, ThreadMetricSuite.withDefaultMetrics(), enableCpuCollection, enableContentionCollection, sinks);
            eventTimer.registerMetric(eventGroup);
            eventTimers.put(eventGroup, eventTimer);
        }
    }


    public Monitorable<?> createMoniteredLongValue(String name, String description, Long initialValue) {
        return register(new MonitoredLongValue(name, description, monitorableRegistry, initialValue));
    }

    public MonitoredCounter createMoniteredCounter(String name, String description) {
        return register(new MonitoredCounter(name, description, monitorableRegistry));
    }


    @SuppressWarnings("unchecked")
    private <T extends Monitorable> T register(T monitorable) {
        // TODO need to check if the monitorable exits and resue it, this is particularly noticeable if Indexes are deleted and recreated
        // need to merge in Cowan's latest changes to see this though
        return (T) monitorableRegistry.registerOrReuse(monitorable);
    }

    public void registerNewPollingMonitoredValue(String name, String description, Supplier<Long> propertySupplier, ValueSemantics valueSemantics) {
        new PollingMonitoredValue(name, description, monitorableRegistry, updateFrequency, propertySupplier, valueSemantics);
    }

    public void registerMetricsForStatsObject(final Class<?> clazz, final Object dataSource, String metricNameFormat) {
        registerMetricsForStatsObject(clazz, new Supplier<Object>() {
            @Override
            public Object get() {
                return dataSource;
            }
        }, metricNameFormat);

    }

    public void registerMetricsForStatsObject(final Class<?> clazz, final Supplier<Object> dataSource, String metricNameFormat) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (final PropertyDescriptor pd : propertyDescriptors) {
                if (pd.getPropertyType().equals(long.class)) {
                    Supplier<Long> propertySupplier = new Supplier<Long>() {
                        @Override
                        public Long get() {
                            Method readMethod = pd.getReadMethod();
                            try {
                                return (Long) readMethod.invoke(dataSource.get());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };

                    // This get registered automatically via construction
                    // TODO reconsider metric namespace and descriptions
                    registerNewPollingMonitoredValue(String.format(metricNameFormat, pd.getName()), "TODO", propertySupplier, ValueSemantics.CONSTANT);
                }
            }

        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }


    public ByteCountingOutputStream wrapAsCountingOutputStream(OutputStream out, Counter existingCounter) {
        return new ByteCountingOutputStream(out, existingCounter);
    }

    public ByteCountingInputStream wrapAsCountingInputStream(InputStream is, Counter existingCounter) {
        return new ByteCountingInputStream(is, existingCounter);
    }

    @Override
    protected void doStart() {
        selfStartingMonitoringView.start();
    }

    @Override
    protected void doStop() {
        selfStartingMonitoringView.stop();
    }

    @Override
    protected void doClose() {
    }

    public MonitoredCounterBuilder forShard(ShardId shardId) {
        return new MonitoredCounterBuilder(shardId);
    }

    public MonitorableRegistry getMonitorableRegistry() {
        return monitorableRegistry;
    }

    public final class MonitoredCounterBuilder {


        private final ShardId shardId;

        public MonitoredCounterBuilder(ShardId shardId) {
            this.shardId = shardId;
        }


        public MonitoredCounter count(String op) {
            return createMoniteredCounter(String.format("elasticsearch.index[%s/%s].%s.count", shardId.getIndex(), shardId.id(), op), String.format("# %s Operations performed by the engine for a given shard", StringUtils.capitalize(op)));
        }
    }

    public EventTimer getEventTimerForGroup(String eventGroup) {
        return eventTimers.get(eventGroup);
    }


}
