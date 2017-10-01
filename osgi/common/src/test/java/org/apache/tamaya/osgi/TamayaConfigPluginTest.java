/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 10.12.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TamayaConfigPluginTest extends  AbstractOSGITest{

    @Test
    public void pluginLoaded() throws Exception {
        assertNotNull(bundleContext.getService(bundleContext.getServiceReference(TamayaConfigPlugin.class)));
    }

    @Test
    public void testOperationMode() throws Exception {
        Policy om = tamayaConfigPlugin.getDefaultOperationMode();
        tamayaConfigPlugin.setDefaultOperationMode(Policy.EXTEND);
        assertEquals(Policy.EXTEND, tamayaConfigPlugin.getDefaultOperationMode());
        tamayaConfigPlugin.setDefaultOperationMode(Policy.OVERRIDE);
    }

    @Test
    public void testAutoUpdate() throws Exception {
        boolean autoUpdate = tamayaConfigPlugin.isAutoUpdateEnabled();
        tamayaConfigPlugin.setAutoUpdateEnabled(!autoUpdate);
        assertEquals(tamayaConfigPlugin.isAutoUpdateEnabled(),!autoUpdate);
        tamayaConfigPlugin.setAutoUpdateEnabled(autoUpdate);
        assertEquals(tamayaConfigPlugin.isAutoUpdateEnabled(),autoUpdate);
    }

    @Test
    public void testDefaulEnabled() throws Exception {
        boolean enabled = tamayaConfigPlugin.isTamayaEnabledByDefault();
        tamayaConfigPlugin.setTamayaEnabledByDefault(!enabled);
        assertEquals(tamayaConfigPlugin.isTamayaEnabledByDefault(),!enabled);
        tamayaConfigPlugin.setTamayaEnabledByDefault(enabled);
        assertEquals(tamayaConfigPlugin.isTamayaEnabledByDefault(),enabled);
    }

    @Test
    public void testSetPluginConfig() throws Exception {
        Dictionary<String,Object> config = new Hashtable<>();
        tamayaConfigPlugin.setPluginConfig(config);
        assertEquals(tamayaConfigPlugin.getPluginConfig(), config);
    }

    @Test
    public void testSetGetConfigValue() throws Exception {
        Dictionary<String,Object> config = new Hashtable<>();
        String val = (String)tamayaConfigPlugin.getConfigValue("foo");
        tamayaConfigPlugin.setConfigValue("bar", "foo");
        assertEquals(tamayaConfigPlugin.getConfigValue("bar"), "foo");
    }

    @Test
    public void getTMUpdateConfig() throws Exception {
        org.apache.tamaya.Configuration config = tamayaConfigPlugin.getTamayaConfiguration("java.");
        assertNotNull(config);
        assertNull(config.get("jlkjllj"));
        assertEquals(config.get("home"),System.getProperty("java.home"));
    }

    @Test
    public void getUpdateConfig() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.updateConfig(TamayaConfigPlugin.COMPONENTID);
        assertNotNull(config);
        assertEquals(config.get("java.home"), System.getProperty("java.home"));
    }

    @Test
    public void getUpdateConfig_DryRun() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.updateConfig(TamayaConfigPlugin.COMPONENTID, true);
        assertNotNull(config);
        assertEquals(config.get("java.home"), System.getProperty("java.home"));
    }

    @Test
    public void getUpdateConfig_Explicit_DryRun() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.updateConfig(TamayaConfigPlugin.COMPONENTID, Policy.EXTEND, true, true);
        assertNotNull(config);
        assertEquals(config.get("java.home"), System.getProperty("java.home"));
    }

    @Test
    public void getPluginConfig() throws Exception {
        Dictionary<String, Object> config = tamayaConfigPlugin.getPluginConfig();
        assertNotNull(config);
        assertEquals(config, super.getProperties(TamayaConfigPlugin.COMPONENTID));
    }

    @Test
    public void getDefaultOperationMode() throws Exception {
        Policy om = tamayaConfigPlugin.getDefaultOperationMode();
        assertNotNull(om);
        Dictionary<String,Object> pluginConfig = super.getProperties(TamayaConfigPlugin.COMPONENTID);
        pluginConfig.put(Policy.class.getSimpleName(), Policy.UPDATE_ONLY.toString());
        TamayaConfigPlugin plugin = new TamayaConfigPlugin(bundleContext);
        om = plugin.getDefaultOperationMode();
        assertNotNull(om);
        assertEquals(om, Policy.UPDATE_ONLY);
        pluginConfig.put(Policy.class.getSimpleName(), Policy.OVERRIDE.toString());
        plugin = new TamayaConfigPlugin(bundleContext);
        om = plugin.getDefaultOperationMode();
        assertNotNull(om);
        assertEquals(om, Policy.OVERRIDE);
    }

    @Test
    public void testConfiguration_Override() throws Exception {
        assertNotNull(cm);
        tamayaConfigPlugin.updateConfig("tamaya", Policy.OVERRIDE, true, false);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        // Override should add additional values
        assertEquals(config.getProperties().get("my.testProperty1"), "success1");
        assertEquals(config.getProperties().get("my.testProperty2"), "success2");
        assertEquals(config.getProperties().get("my.testProperty3"), "success3");
        assertEquals(config.getProperties().get("my.testProperty4"), "success4");
        // Extend should also update any existing values...
        assertEquals(config.getProperties().get("java.version"), "Java2000");
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Override_ImplicitlyConfigured() throws Exception {
        assertNotNull(cm);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        Dictionary<String,Object> props = config.getProperties();
        props.put(TamayaConfigPlugin.TAMAYA_POLICY_PROP, "OVERRIDE");
        config.update(props);
        tamayaConfigPlugin.updateConfig("tamaya", Policy.UPDATE_ONLY, false, false);
        config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        // Override should add additional values
        assertEquals(config.getProperties().get("my.testProperty1"), "success1");
        assertEquals(config.getProperties().get("my.testProperty2"), "success2");
        assertEquals(config.getProperties().get("my.testProperty3"), "success3");
        assertEquals(config.getProperties().get("my.testProperty4"), "success4");
        // Extend should also update any existing values...
        assertEquals(config.getProperties().get("java.version"), "Java2000");
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Extend() throws Exception {
        assertNotNull(cm);
        tamayaConfigPlugin.updateConfig("tamaya", Policy.EXTEND, true, false);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        assertEquals(config.getProperties().get("my.testProperty1"), "success1");
        assertEquals(config.getProperties().get("my.testProperty2"), "success2");
        assertEquals(config.getProperties().get("my.testProperty3"), "success3");
        assertEquals(config.getProperties().get("my.testProperty4"), "success4");
        // Extend should not update any existing values...
        assertEquals(config.getProperties().get("java.version"), System.getProperty("java.version"));
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Update_Only() throws Exception {
        assertNotNull(cm);
        tamayaConfigPlugin.updateConfig("tamaya", Policy.UPDATE_ONLY, true, false);
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        assertEquals(config.getProperties().get("my.testProperty1"), null);
        assertEquals(config.getProperties().get("my.testProperty2"), null);
        assertEquals(config.getProperties().get("my.testProperty3"), null);
        assertEquals(config.getProperties().get("my.testProperty4"), null);
        // Update only should update any existing values...
        assertEquals(config.getProperties().get("java.version"), "Java2000");
        tamayaConfigPlugin.restoreBackup("tamaya");
    }

    @Test
    public void testConfiguration_Override_Dryrun() throws Exception {
        assertNotNull(cm);
        Dictionary<String,Object> result = tamayaConfigPlugin.updateConfig("tamaya", Policy.OVERRIDE, true, true);
        assertNotNull(result);
        // Override should add additional values
        assertEquals(result.get("my.testProperty1"), "success1");
        assertEquals(result.get("my.testProperty2"), "success2");
        assertEquals(result.get("my.testProperty3"), "success3");
        assertEquals(result.get("my.testProperty4"), "success4");
        // Extend should also update any existing values...
        assertEquals(result.get("java.version"), "Java2000");

        // DryRun: should not have been changged anything on OSGI level...
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        assertEquals(config.getProperties().get("my.testProperty1"), null);
        assertEquals(config.getProperties().get("my.testProperty2"), null);
        assertEquals(config.getProperties().get("my.testProperty3"), null);
        assertEquals(config.getProperties().get("my.testProperty4"), null);
        assertEquals(config.getProperties().get("java.version"), System.getProperty("java.version"));
    }

    @Test
    public void testConfiguration_Extend_Dryrun() throws Exception {
        assertNotNull(cm);
        Dictionary<String,Object> result = tamayaConfigPlugin.updateConfig("tamaya", Policy.EXTEND, true, true);
        assertNotNull(result);
        assertEquals(result.get("my.testProperty1"), "success1");
        assertEquals(result.get("my.testProperty2"), "success2");
        assertEquals(result.get("my.testProperty3"), "success3");
        assertEquals(result.get("my.testProperty4"), "success4");
        // Extend should not update any existing values...
        assertEquals(result.get("java.version"), System.getProperty("java.version"));

        // DryRun: should not have been changged anything on OSGI level...
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        assertEquals(config.getProperties().get("my.testProperty1"), null);
        assertEquals(config.getProperties().get("my.testProperty2"), null);
        assertEquals(config.getProperties().get("my.testProperty3"), null);
        assertEquals(config.getProperties().get("my.testProperty4"), null);
        assertEquals(config.getProperties().get("java.version"), System.getProperty("java.version"));
    }

    @Test
    public void testConfiguration_Update_Only_Dryrun() throws Exception {
        assertNotNull(cm);
        Dictionary<String,Object> result = tamayaConfigPlugin.updateConfig("tamaya", Policy.UPDATE_ONLY, true, true);
        assertNotNull(result);
        assertTrue(result.size() > 4);
        assertEquals(result.get("my.testProperty1"), null);
        assertEquals(result.get("my.testProperty2"), null);
        assertEquals(result.get("my.testProperty3"), null);
        assertEquals(result.get("my.testProperty4"), null);
        // Update only should update any existing values...
        assertEquals(result.get("java.version"), "Java2000");

        // DryRun: should not have been changged anything on OSGI level...
        org.osgi.service.cm.Configuration config = cm.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertTrue(config.getProperties().size() > 4);
        assertEquals(config.getProperties().get("my.testProperty1"), null);
        assertEquals(config.getProperties().get("my.testProperty2"), null);
        assertEquals(config.getProperties().get("my.testProperty3"), null);
        assertEquals(config.getProperties().get("my.testProperty4"), null);
        assertEquals(config.getProperties().get("java.version"), System.getProperty("java.version"));
    }

}