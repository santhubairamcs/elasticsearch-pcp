package com.aconex.elasticsearch.pcp;

import java.util.concurrent.ConcurrentMap;

import com.custardsource.parfait.MonitoredCounter;
import com.custardsource.parfait.timing.EventMetricCollector;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.ShardId;

class ProfiledShardBasedMethodCounter implements MethodInterceptor {
    private final String eventGroup;
    private final EventMetricCollector collector;
    private final String action;
    private final ConcurrentMap<ShardId, MonitoredCounter> counterMap;

    ProfiledShardBasedMethodCounter(final ParfaitService parfaitService, String eventGroup, final String action) {
        this.eventGroup = eventGroup;
        this.action = action;
        this.collector = parfaitService.getEventTimerForGroup(eventGroup).getCollector();
        this.counterMap = new MapMaker().makeComputingMap(new Function<ShardId, MonitoredCounter>() {
            @Override public MonitoredCounter apply(ShardId from) {
                return parfaitService.forShard(from).count(action);
            }
        });
    }

    @Override public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        collector.startTiming(eventGroup, action);
        try {
            Engine engine = (Engine)methodInvocation.getThis();
            counterMap.get(engine.shardId()).inc();
            return methodInvocation.proceed();
        } finally {
            collector.stopTiming();
        }
    }
}
