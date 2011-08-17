package com.aconex.elasticsearch.plugin.pcp;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.util.Collection;

import com.aconex.elasticsearch.pcp.ParfaitModule;
import com.aconex.elasticsearch.pcp.ParfaitService;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;

public class PCPPlugin extends AbstractPlugin {

    private static final String PCP_ENABLED = "pcp.enabled";
    private final Settings settings;

    public PCPPlugin(Settings settings) {
        this.settings = settings;
    }

    public String name() {
        return "pcp";
    }

    public String description() {
        return "Performance Co-Pilot (PCP) integration with elasticsearch";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        if (settings.getAsBoolean(PCP_ENABLED, true)) {
            modules.add(ParfaitModule.class);
        }
        return modules;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        if (settings.getAsBoolean(PCP_ENABLED, true)) {
            services.add(ParfaitService.class);
        }
        return services;
    }
}
