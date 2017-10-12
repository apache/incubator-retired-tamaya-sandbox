/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.osgi.AbstractOSGITest;
import org.apache.tamaya.osgi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigCommandsTest extends AbstractOSGITest{
    @Test
    public void getInfo() throws Exception {
        String result = ConfigCommands.getInfo(tamayaConfigPlugin);
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("Property Sources"));
        assertTrue(result.contains("Property Converter"));
        assertTrue(result.contains("Property Filter"));
        assertTrue(result.contains("ConfigurationContext"));
        assertTrue(result.contains("Configuration"));
    }

    @Test
    public void readTamayaConfig() throws Exception {
        String result = ConfigCommands.readTamayaConfig("java", null);
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains(".version"));
        assertTrue(result.contains("Section"));
        assertTrue(result.contains("java"));
        result = ConfigCommands.readTamayaConfig("java", "version");
        assertNotNull(result);
        assertFalse(result.contains(".version"));
        assertTrue(result.contains("Section"));
        assertTrue(result.contains("java"));
        assertTrue(result.contains("Filter"));
        assertTrue(result.contains("version"));
        assertFalse(result.contains("java.vendor"));
        System.out.println("readTamayaConfig: " + result);
    }

    @Test
    public void readTamayaConfig4PID() throws Exception {
        String result = ConfigCommands.readTamayaConfig4PID("test", null);
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("Configuration"));
        assertTrue(result.contains("test"));
    }

    @Test
    public void applyTamayaConfiguration() throws Exception {
        String result = ConfigCommands.applyTamayaConfiguration(tamayaConfigPlugin, "applyTamayaConfiguration", Policy.OVERRIDE.toString(), true);
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("OSGI Configuration for PID"));
        assertTrue(result.contains("applyTamayaConfiguration"));
        assertTrue(result.contains("OVERRIDE"));
        assertTrue(result.contains("Applied"));
        assertTrue(result.contains("false"));
    }

    @Test
    public void readOSGIConfiguration() throws Exception {
        String result = ConfigCommands.readOSGIConfiguration(tamayaConfigPlugin, "readOSGIConfiguration", "java");
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("OSGI Configuration for PID"));
        assertTrue(result.contains("readOSGIConfiguration"));
        assertTrue(result.contains("java.home"));
    }

    @Test
    public void getDefaultOpPolicy() throws Exception {
        Policy mode = tamayaConfigPlugin.getDefaultPolicy();
        String result = ConfigCommands.getDefaultOpPolicy(tamayaConfigPlugin);
        assertNotNull(result);
        assertTrue(result.contains(mode.toString()));
    }

    @Test
    public void setDefaultOpPolicy() throws Exception {
        String result = ConfigCommands.setDefaultOpPolicy(tamayaConfigPlugin, Policy.EXTEND.toString());
        assertNotNull(result);
        assertTrue(result.contains("EXTEND"));
        assertEquals(tamayaConfigPlugin.getDefaultPolicy(), Policy.EXTEND);
        result = ConfigCommands.setDefaultOpPolicy(tamayaConfigPlugin, Policy.UPDATE_ONLY.toString());
        assertNotNull(result);
        assertTrue(result.contains("UPDATE_ONLY"));
        assertEquals(tamayaConfigPlugin.getDefaultPolicy(), Policy.UPDATE_ONLY);
    }

    @Test
    public void getProperty() throws Exception {
        String result = ConfigCommands.getProperty("system-properties", "java.version", false);
        assertNotNull(result);
        System.out.println(result);
        assertEquals(result, System.getProperty("java.version"));
        result = ConfigCommands.getProperty("system-properties", "java.version", true);
        assertNotNull(result);
    }

    @Test
    public void getPropertySource() throws Exception {
        String result = ConfigCommands.getPropertySource("system-properties");
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("Property Source"));
        assertTrue(result.contains("ID"));
        assertTrue(result.contains("system-properties"));
        assertTrue(result.contains("Ordinal"));
        assertTrue(result.contains("java.version"));
    }

    @Test
    public void getPropertySourceOverview() throws Exception {
        String result = ConfigCommands.getPropertySourceOverview();
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("Ordinal"));
        assertTrue(result.contains("Class"));
        assertTrue(result.contains("Ordinal"));
        assertTrue(result.contains("ID"));
        assertTrue(result.contains("Ordinal"));
        assertTrue(result.contains("system-properties"));
        assertTrue(result.contains("environment-properties"));
        assertTrue(result.contains("CLI"));
    }

    @Test
    public void setDefaultEnabled() throws Exception {
        String result = ConfigCommands.setDefaultEnabled(tamayaConfigPlugin, true);
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains(TamayaConfigService.TAMAYA_ENABLED_PROP+"=true"));
        assertTrue(tamayaConfigPlugin.isTamayaEnabledByDefault());
        result = ConfigCommands.setDefaultEnabled(tamayaConfigPlugin, false);
        assertNotNull(result);
        assertTrue(result.contains(TamayaConfigService.TAMAYA_ENABLED_PROP+"=false"));
        assertFalse(tamayaConfigPlugin.isTamayaEnabledByDefault());
    }

    @Test
    public void getDefaultEnabled() throws Exception {
        tamayaConfigPlugin.setTamayaEnabledByDefault(true);
        String result = ConfigCommands.getDefaultEnabled(tamayaConfigPlugin);
        System.out.println(result);
        tamayaConfigPlugin.setTamayaEnabledByDefault(false);
        result = ConfigCommands.getDefaultEnabled(tamayaConfigPlugin);
        assertNotNull(result);
        assertTrue(result.equals("false"));
    }

    @Test
    public void setAutoUpdateEnabled() throws Exception {
        String result = ConfigCommands.setAutoUpdateEnabled(tamayaConfigPlugin, true);
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result.contains("true"));
        assertTrue(result.contains(TamayaConfigService.TAMAYA_AUTO_UPDATE_ENABLED_PROP));
        assertTrue(tamayaConfigPlugin.isAutoUpdateEnabled());
        result = ConfigCommands.setAutoUpdateEnabled(tamayaConfigPlugin, false);
        assertNotNull(result);
        assertTrue(result.contains("false"));
        assertTrue(result.contains(TamayaConfigService.TAMAYA_AUTO_UPDATE_ENABLED_PROP));
        assertFalse(tamayaConfigPlugin.isAutoUpdateEnabled());
    }

    @Test
    public void getAutoUpdateEnabled() throws Exception {
        tamayaConfigPlugin.setAutoUpdateEnabled(true);
        String result = ConfigCommands.getAutoUpdateEnabled(tamayaConfigPlugin);
        System.out.println(result);
        assertTrue(result.contains("true"));
        tamayaConfigPlugin.setAutoUpdateEnabled(false);
        result = ConfigCommands.getAutoUpdateEnabled(tamayaConfigPlugin);
        assertNotNull(result);
        assertTrue(result.contains("false"));
    }

}