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
package org.apache.tamaya.model;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.usagetracker.ConfigUsage;
import org.junit.Test;
import test.model.TestConfigAccessor;

import static org.junit.Assert.*;

/**
 * Created by Anatole on 09.08.2015.
 */
public class ConfigUsageStatsTest {

    @Test
    public void testUsageWhenEnabled(){
        ConfigUsage.enableUsageTracking(true);
        TestConfigAccessor.readConfiguration();
        Configuration config = Configuration.current();
        String info = ConfigUsage.getInfo();
        assertFalse(info.contains("java.version"));
        assertNotNull(info);
        config = TestConfigAccessor.readConfiguration();
        config.getProperties();
        TestConfigAccessor.readProperty(config, "java.locale");
        TestConfigAccessor.readProperty(config, "java.version");
        TestConfigAccessor.readProperty(config, "java.version");
        config.get("java.version");
        info = ConfigUsage.getInfo();
        System.out.println(info);
        assertTrue(info.contains("java.version"));
        assertNotNull(info);
        ConfigUsage.enableUsageTracking(false);
    }

    @Test
    public void testUsageWhenDisabled(){
        ConfigUsage.enableUsageTracking(false);
        ConfigUsage.clearStats();
        TestConfigAccessor.readConfiguration();
        Configuration config = Configuration.current();
        String info = ConfigUsage.getInfo();
        assertNotNull(info);
        assertFalse(info.contains("java.version"));
        config = TestConfigAccessor.readConfiguration();
        config.getProperties();
        TestConfigAccessor.readProperty(config, "java.locale");
        TestConfigAccessor.readProperty(config, "java.version");
        TestConfigAccessor.readProperty(config, "java.version");
        config.get("java.version");
        info = ConfigUsage.getInfo();
        assertFalse(info.contains("java.version"));
        assertNotNull(info);
        ConfigUsage.enableUsageTracking(false);
    }
}
