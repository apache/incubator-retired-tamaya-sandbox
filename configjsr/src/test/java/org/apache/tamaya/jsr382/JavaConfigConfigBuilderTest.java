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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(builder).isNotNull();
        Config config = builder.build();
        assertThat(config).isNotNull();
        assertThat(config.getPropertyNames().iterator().hasNext()).isFalse();
        assertThat(config.getConfigSources().iterator().hasNext()).isFalse();
    }

    @Test
    public void testBuildConfig(){
        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();
        assertThat(builder).isNotNull();
        builder.withSources(testSource);
        Config config = builder.build();
        assertThat(config).isNotNull();
        assertThat(config.getPropertyNames().iterator().hasNext()).isTrue();
        assertThat(config.getConfigSources().iterator().hasNext()).isTrue();
        assertThat(config.getValue("timestamp", String.class)).isNotNull();
        ConfigSource src = config.getConfigSources().iterator().next();
        assertThat(src).isNotNull();
        assertThat(src).isEqualTo(testSource);
    }

    @Test
    public void testBuildDefaultConfig(){
        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();
        assertThat(builder).isNotNull();
        builder.addDefaultSources();
        Config config = builder.build();
        assertThat(config).isNotNull();
        assertThat(config.getPropertyNames().iterator().hasNext()).isTrue();
        assertThat(config.getConfigSources().iterator().hasNext()).isTrue();
        assertThat(config.getValue("java.home", String.class)).isNotNull();
        ConfigSource src = config.getConfigSources().iterator().next();
        assertThat(src).isNotNull();
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

        assertThat(name).hasSize(4)
                  .containsExactlyInAnyOrder("paris",
                                             "SystemPropertySource",
                                             "environment-properties",
                                             "META-INF/javaconfig.properties");
    }

    @Test
    public void addDiscoveredSourcesAddsAllConfigSourceProviders() throws Exception {
     //   throw new RuntimeException("Not implemented yet!");
    }
}
