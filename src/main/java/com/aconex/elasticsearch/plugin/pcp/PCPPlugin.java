package com.aconex.elasticsearch.plugin.pcp;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.util.Collection;

import com.aconex.elasticsearch.pcp.ParfaitModule;
import com.aconex.elasticsearch.pcp.ParfaitService;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

public class PCPPlugin extends AbstractPlugin {
    public String name() {
        return "pcp";
    }

    public String description() {
        return "Performance Co-Pilot (PCP) integration with elasticsearch";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(ParfaitModule.class);
        //if (settings.getAsBoolean("memcached.enabled", true)) {
        //}
        return modules;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        services.add(ParfaitService.class);
        //if (settings.getAsBoolean("memcached.enabled", true)) {
        //}
        return services;
    }
}
