package com.aconex.elasticsearch.pcp;

import com.custardsource.parfait.MonitoredCounter;
import com.custardsource.parfait.timing.EventMetricCollector;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

class ProfiledMethodCounter implements MethodInterceptor {
    private final String eventGroup;
    private final MonitoredCounter counter;
    private final EventMetricCollector collector;
    private final String action;

    ProfiledMethodCounter(EventMetricCollector collector, MonitoredCounter counter, String eventGroup, String action) {
        this.counter = counter;
        this.eventGroup = eventGroup;
        this.action = action;
        this.collector = collector;
    }

    @Override public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        collector.startTiming(eventGroup, action);
        try {
            counter.inc();
            return methodInvocation.proceed();
        } finally {
            collector.stopTiming();
        }
    }
}
