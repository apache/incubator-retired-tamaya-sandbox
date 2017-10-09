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
package org.apache.tamaya.microprofile;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 24.03.17.
 */
public class MicroprofileConfigBuilderTest {

    private ConfigSource testSource = new ConfigSource() {
        @Override
        public Map<String, String> getProperties() {
            Map<String,String> map = new HashMap<>();
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
            return map;
        }

        @Override
        public String getValue(String propertyName) {
            if("timestamp".equals(propertyName)){
                return String.valueOf(System.currentTimeMillis());
            }
            return null;
        }

        @Override
        public String getName() {
            return "test";
        }
    };

    @Test
    public void testBuildEmptyConfig(){
        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();
        assertNotNull(builder);
        Config config = builder.build();
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertFalse(config.getConfigSources().iterator().hasNext());
    }

    @Test
    public void testBuildConfig(){
        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();
        assertNotNull(builder);
        builder.withSources(testSource);
        Config config = builder.build();
        assertNotNull(config);
        assertTrue(config.getPropertyNames().iterator().hasNext());
        assertTrue(config.getConfigSources().iterator().hasNext());
        assertNotNull(config.getValue("timestamp", String.class));
        ConfigSource src = config.getConfigSources().iterator().next();
        assertNotNull(src);
        assertEquals(src, testSource);
    }

    @Test
    public void testBuildDefaultConfig(){
        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();
        assertNotNull(builder);
        builder.addDefaultSources();
        Config config = builder.build();
        assertNotNull(config);
        assertTrue(config.getPropertyNames().iterator().hasNext());
        assertTrue(config.getConfigSources().iterator().hasNext());
        assertNotNull(config.getValue("java.home", String.class));
        ConfigSource src = config.getConfigSources().iterator().next();
        assertNotNull(src);
    }

}
