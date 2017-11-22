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
package org.apache.tamaya.jsr382;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.config.Config;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigProviderResolver;
import javax.config.spi.ConfigSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 24.03.17.
 */
public class JavaConfigConfigBuilderTest {

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

    @Test
    public void addDiscoveredSourcesAddsAllConfigSources() throws Exception {
        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();

        Config config = builder.addDiscoveredSources()
                               .addDefaultSources().build();

        Iterable<ConfigSource> iterable = config.getConfigSources();

        List<String> name = StreamSupport.stream(iterable.spliterator(), false)
                                         .map(ConfigSource::getName)
                                         .collect(Collectors.toList());

        Assertions.assertThat(name).hasSize(4)
                  .containsExactlyInAnyOrder("paris",
                                             "SystemPropertySource",
                                             "environment-properties",
                                             "META-INF/JavaConfig-config.properties");
    }

    @Test
    public void addDiscoveredSourcesAddsAllConfigSourceProviders() throws Exception {
     //   throw new RuntimeException("Not implemented yet!");
    }
}
