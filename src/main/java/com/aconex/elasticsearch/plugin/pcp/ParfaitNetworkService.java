package com.aconex.elasticsearch.plugin.pcp;

import com.aconex.elasticsearch.pcp.ParfaitService;
import com.google.common.base.Supplier;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.monitor.network.NetworkService;
import org.elasticsearch.monitor.network.NetworkStats;

public class ParfaitNetworkService extends AbstractLifecycleComponent<Void> {

    private final NetworkService networkService;
    private final ParfaitService parfaitService;

    @Inject
    public ParfaitNetworkService(Settings settings, NetworkService networkService, ParfaitService parfaitService) {
        super(settings);
        this.networkService = networkService;
        this.parfaitService = parfaitService;
        registerNetworkMetrics();
    }

    private void registerNetworkMetrics() {
        parfaitService.registerMetricsForStatsObject(NetworkStats.Tcp.class, new Supplier<Object>() {
            @Override
            public Object get() {
                return networkService.stats().tcp();
            }
        }, "elasticsearch.network.%s");

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
