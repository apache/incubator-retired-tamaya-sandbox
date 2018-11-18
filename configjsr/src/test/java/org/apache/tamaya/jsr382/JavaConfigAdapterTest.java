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

import org.apache.tamaya.*;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.propertysource.BuildablePropertySource;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigAccessor;
import javax.config.ConfigProvider;
import javax.config.ConfigSnapshot;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigSource;
import javax.config.spi.Converter;
import java.util.*;

import static org.junit.Assert.*;

public class JavaConfigAdapterTest {
    @Test
    public void toConfig() throws Exception {
        Configuration config = Configuration.current();
        Config mpConfig = JavaConfigAdapterFactory.toConfig(config);
        assertNotNull(mpConfig);
        assertEquals(config.getProperties().keySet(), mpConfig.getPropertyNames());
    }

    @Test
    public void toConfigWithTamayaConfiguration() throws Exception {
        Configuration configuration = new MyConfiguration();
        JavaConfigAdapter config = new JavaConfigAdapter(configuration);
        TamayaConfigurationAdapter tamayaConfiguration = new TamayaConfigurationAdapter(config);

        Config result = JavaConfigAdapterFactory.toConfig(tamayaConfiguration);

        Assertions.assertThat(result).isNotNull()
                  .isInstanceOf(JavaConfigAdapter.class)
                  .isSameAs(config);
    }

    @Test
    public void toConfiguration() throws Exception {
        Config mpConfig = ConfigProvider.getConfig();
        Configuration config = JavaConfigAdapterFactory.toConfiguration(mpConfig);
        assertNotNull(config);
        assertEquals(mpConfig.getPropertyNames(), config.getProperties().keySet());
    }

    @Test
    public void toConfigurationWithNoneJavaConfigConfig() throws Exception {
        Config config = new MyConfig();
        Configuration result = JavaConfigAdapterFactory.toConfiguration(config);

        Assertions.assertThat(result).isNotNull()
                  .isInstanceOf(TamayaConfigurationAdapter.class);
    }

    @Test
    public void toConfigSources() throws Exception {
        BuildablePropertySource testPropertySource = BuildablePropertySource.builder()
                .withSource("toConfigSources")
                .withSimpleProperty("string0", "value0")
                .withSimpleProperty("int0", "0")
                .build();
        List<PropertySource> tamayaSources = new ArrayList<>();
        tamayaSources.add(testPropertySource);
        List<ConfigSource> configSources = JavaConfigAdapterFactory.toConfigSources(tamayaSources);
        assertNotNull(configSources);
        assertEquals(tamayaSources.size(), configSources.size());
        compare(testPropertySource, configSources.get(0));
    }

    private void compare(PropertySource tamayaSource, ConfigSource mpSource) {
        assertEquals(mpSource.getName(),tamayaSource.getName());
        assertEquals(mpSource.getOrdinal(), tamayaSource.getOrdinal());
        assertEquals(mpSource.getProperties().keySet(), tamayaSource.getProperties().keySet());
        for(String key:mpSource.getPropertyNames()){
            assertEquals(mpSource.getValue(key), tamayaSource.get(key).getValue());
        }
    }

    @Test
    public void toPropertySources() throws Exception {
        BuildableConfigSource configSource = BuildableConfigSource.builder()
                .withSource("toConfigSources")
                .withProperty("string0", "value0")
                .withProperty("int0", "0")
                .build();
        List<ConfigSource> configSources = new ArrayList<>();
        configSources.add(configSource);
        List<PropertySource> propertySources = JavaConfigAdapterFactory.toPropertySources(configSources);
        assertNotNull(propertySources);
        assertEquals(propertySources.size(), configSources.size());
        compare(propertySources.get(0), configSource);
    }

    @Test
    public void toConfigSource() throws Exception {
        BuildablePropertySource tamayaSource = BuildablePropertySource.builder()
                .withSource("toConfigSource")
                .withSimpleProperty("string0", "value0")
                .withSimpleProperty("int0", "0")
                .build();
        ConfigSource configSource = JavaConfigAdapterFactory.toConfigSource(tamayaSource);
        assertNotNull(configSource);
        compare(tamayaSource, configSource);
    }

    @Test
    public void toPropertySource() throws Exception {
        BuildableConfigSource configSource = BuildableConfigSource.builder()
                .withSource("toConfigSource")
                .withProperty("string0", "value0")
                .withProperty("int0", "0")
                .build();
        PropertySource tamayaSource = JavaConfigAdapterFactory.toPropertySource(configSource);
        assertNotNull(configSource);
        compare(tamayaSource, configSource);
    }

    @Test
    public void toPropertyConverter() throws Exception {
        PropertyConverter<String> tamayaConverter = JavaConfigAdapterFactory.toPropertyConverter(new UppercaseConverter());
        assertNotNull(tamayaConverter);
        assertEquals("ABC", tamayaConverter.convert("aBC", null));
    }

    @Test
    public void toConverter() throws Exception {
        Converter<String> mpConverter = JavaConfigAdapterFactory.toConverter(new UppercasePropertyConverter());
        assertNotNull(mpConverter);
        assertEquals("ABC", mpConverter.convert("aBC"));
    }

    @Test
    public void toConfigBuilder() throws Exception {
        ConfigBuilder builder = JavaConfigAdapterFactory.toConfigBuilder(ConfigurationProvider.getConfigurationBuilder());
        assertNotNull(builder);
    }

    @Test
    public void toStringMap() throws Exception {
        Map<String,PropertyValue> props = new HashMap<>();
        props.put("a", PropertyValue.of("a","b", "toStringMap"));
        Map<String, String> mpProps = JavaConfigAdapterFactory.toStringMap(props);
        assertNotNull(mpProps);
        assertEquals(props.keySet(), mpProps.keySet());
        assertEquals(mpProps.get("a"), "b");
    }

    @Test
    public void toPropertyValueMap() throws Exception {
        Map<String,String> props = new HashMap<>();
        props.put("a", "b");
        Map<String, PropertyValue> tamayaProps = JavaConfigAdapterFactory.toPropertyValueMap(props, "toPropertyValueMap");
        assertNotNull(tamayaProps);
        assertEquals(tamayaProps.keySet(), props.keySet());
        assertEquals(tamayaProps.get("a").getValue(), "b");
        assertEquals("toPropertyValueMap", tamayaProps.get("a").getSource());
    }

    static class MyConfig implements Config {
        @Override
        public <T> T getValue(String s, Class<T> aClass) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> Optional<T> getOptionalValue(String s, Class<T> aClass) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public ConfigAccessor<String> access(String propertyName) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public ConfigSnapshot snapshotFor(ConfigAccessor<?>... configValues) {
            return null;
        }

        @Override
        public Iterable<String> getPropertyNames() {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public Iterable<ConfigSource> getConfigSources() {
            return Collections.emptyList();
        }
    }

    static class MyConfiguration implements Configuration {
        @Override
        public String get(String key) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public String getOrDefault(String key, String defaultValue) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T get(String key, TypeLiteral<T> type) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public <T> T getOrDefault(String key, TypeLiteral<T> type, T defaultValue) {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public Map<String, String> getProperties() {
            throw new RuntimeException("Not implemented yet!");
        }

        @Override
        public ConfigurationContext getContext() {
            return ConfigurationContext.EMPTY;
        }

        @Override
        public ConfigurationSnapshot getSnapshot(Iterable<String> keys) {
            throw new RuntimeException("Not implemented yet!");
        }
    }

}