package com.aconex.elasticsearch.plugin.pcp;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.elasticsearch.common.settings.Settings;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PCPPluginTest {

    @Mock
    Settings settings;

    private PCPPlugin pcpPlugin;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        whenPcpEnabled(true);
        pcpPlugin = new PCPPlugin(settings);
    }

    @Test
    public void testModules() throws Exception {
        assertEquals(pcpPlugin.modules().size(), 1);

        whenPcpEnabled(false);
        assertEquals(pcpPlugin.modules().size(), 0);
    }

    private void whenPcpEnabled(boolean enabled) {
        when(settings.getAsBoolean(eq("pcp.enabled"), anyBoolean())).thenReturn(enabled);
    }

    @Test
    public void testServices() throws Exception {
        assertEquals(pcpPlugin.services().size(), 1);

        whenPcpEnabled(false);
        assertEquals(pcpPlugin.services().size(), 0);

    }
}
