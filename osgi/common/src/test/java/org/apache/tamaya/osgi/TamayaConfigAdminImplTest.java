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
import org.osgi.service.cm.Configuration;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 10.12.16.
 */
public class TamayaConfigAdminImplTest {

    private TamayaConfigAdminImpl configAdmin = new TamayaConfigAdminImpl(null);

    @Test
    public void createFactoryConfiguration() throws Exception {
        Configuration config = configAdmin.createFactoryConfiguration("tamaya");
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertEquals(config.getProperties().size(), 4);
        assertEquals(config.getProperties().get("my.testProperty1"), "success1");
    }

    @Test
    public void createFactoryConfigurationWithLocation() throws Exception {
        Configuration config = configAdmin.createFactoryConfiguration("tamaya", "location");
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertEquals(config.getProperties().size(), 4);
        assertEquals(config.getProperties().get("my.testProperty2"), "success2");
    }

    @Test
    public void getConfiguration() throws Exception {
        Configuration config = configAdmin.getConfiguration("tamaya");
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertEquals(config.getProperties().size(), 4);
        assertEquals(config.getProperties().get("my.testProperty3"), "success3");
    }

    @Test
    public void getConfigurationWithLocation() throws Exception {
        Configuration config = configAdmin.getConfiguration("tamaya", "location");
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertEquals(config.getProperties().size(), 4);
        assertEquals(config.getProperties().get("my.testProperty4"), "success4");
    }

    @Test
    public void listConfigurations() throws Exception {
        Configuration[] configs = configAdmin.listConfigurations(".*");
        assertNotNull(configs);
    }

}