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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Anatole on 09.08.2015.
 */
public class ConfigUsageTest {

    @Test
    public void testUsageWhenEnabled(){
        ConfigUsage.getInstance().enableUsageTracking(true);
        TestConfigAccessor.readConfiguration();
        Configuration config = Configuration.current();
        String info = ConfigUsage.getInstance().getInfo();
        assertThat(info.contains("java.version")).isFalse();
        assertThat(info).isNotNull();
        config = TestConfigAccessor.readConfiguration();
        config.getProperties();
        TestConfigAccessor.readProperty(config, "java.locale");
        TestConfigAccessor.readProperty(config, "java.version");
        TestConfigAccessor.readProperty(config, "java.version");
        config.get("java.version");
        info = ConfigUsage.getInstance().getInfo();
        System.out.println(info);
        assertThat(info.contains("java.version")).isTrue();
        assertThat(info).isNotNull();
        ConfigUsage.getInstance().enableUsageTracking(false);
    }

    @Test
    public void testUsageWhenDisabled(){
        ConfigUsage.getInstance().enableUsageTracking(false);
        ConfigUsage.getInstance().clearStats();
        TestConfigAccessor.readConfiguration();
        Configuration config = Configuration.current();
        String info = ConfigUsage.getInstance().getInfo();
        assertThat(info).isNotNull();
        assertThat(info.contains("java.version")).isFalse();
        config = TestConfigAccessor.readConfiguration();
        config.getProperties();
        TestConfigAccessor.readProperty(config, "java.locale");
        TestConfigAccessor.readProperty(config, "java.version");
        TestConfigAccessor.readProperty(config, "java.version");
        config.get("java.version");
        info = ConfigUsage.getInstance().getInfo();
        assertThat(info.contains("java.version")).isFalse();
        assertThat(info).isNotNull();
        ConfigUsage.getInstance().enableUsageTracking(false);
    }
}
