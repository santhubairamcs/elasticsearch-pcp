package com.aconex.elasticsearch.pcp;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import com.custardsource.parfait.ValueSemantics;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

public class ParfaitJvmService extends AbstractLifecycleComponent<Void> {

    private final ParfaitService parfaitService;

    @Inject
    public ParfaitJvmService(Settings settings, ParfaitService parfaitService) {
        super(settings);
        this.parfaitService = parfaitService;

        registerMemoryMetrics();
        registerGCMetrics();
    }

    private void registerGCMetrics() {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        for (final GarbageCollectorMXBean collectorMXBean : garbageCollectorMXBeans) {
            String name = "elasticsearch.jvm.memory." + collectorMXBean.getName().toLowerCase();
            String description = Joiner.on(",").join(collectorMXBean.getMemoryPoolNames());

            parfaitService.registerNewPollingMonitoredValue(name + ".count", description, new Supplier<Long>() {
                @Override
                public Long get() {
                    return collectorMXBean.getCollectionCount();
                }
            }, ValueSemantics.MONOTONICALLY_INCREASING);
            parfaitService.registerNewPollingMonitoredValue(name + ".time", description, new Supplier<Long>() {
                public Long get() {
                    return collectorMXBean.getCollectionTime();
                }
            }, ValueSemantics.MONOTONICALLY_INCREASING);
        }
    }

    private void registerMemoryMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        final MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        parfaitService.registerMetricsForStatsObject(MemoryUsage.class, heapMemoryUsage, "elasticsearch.jvm.memory.%s");
    }



    @Override
    protected void doStart() throws ElasticSearchException {

    }

    @Override
    protected void doStop() throws ElasticSearchException {

    }

    @Override
    protected void doClose() throws ElasticSearchException {

    }
}
