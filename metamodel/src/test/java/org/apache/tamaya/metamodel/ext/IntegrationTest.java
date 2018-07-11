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
package org.apache.tamaya.metamodel.ext;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.metamodel.CachedFilter;
import org.apache.tamaya.metamodel.MetaConfiguration;
import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Created by atsticks on 06.12.16.
 */
public class IntegrationTest {

    @Test
    public void checkSystemLoads(){
        Configuration defaultConfig = ConfigurationProvider.getConfiguration();
        assertThat(defaultConfig).isNotNull();
        
        MetaConfiguration.configure();
        Configuration defaultMetaConfig = ConfigurationProvider.getConfiguration();
        assertThat(defaultMetaConfig).isNotNull();
        
        assertThat(defaultConfig).isNotEqualTo(defaultMetaConfig);
    }

    @Test
    public void testEmptyConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/empty-config.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
    }

    @Test
    public void testMetaContextConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/context-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        MetaContext ctx = MetaContext.getInstance();
        assertFalse(ctx.getProperties().isEmpty());
        assertEquals(ctx.getId(), ctx.getProperty("_id"));
        assertEquals("NONE", ctx.getProperty("app"));
        assertEquals("DEV", ctx.getProperty("stage"));
        assertEquals(".", ctx.getProperty("configdir"));

    }

    @Test
    public void testDefaultConvertersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/default-propertyconverters-test.xml"));
        assertNotNull(config);
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getProperties().isEmpty());
        assertFalse(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(config,
                ConfigurationProvider.getConfigurationBuilder()
                        .addDefaultPropertyConverters()
                        .build());
    }

    @Test
    public void testDefaultPropertySourcesConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/default-propertysources-test.xml"));
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertFalse(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(config,
                ConfigurationProvider.getConfigurationBuilder()
                        .addDefaultPropertySources()
                        .build());
    }

    @Test
    public void testDefaultPropertyFiltersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/default-propertyfilters-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertFalse(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(config,
                ConfigurationProvider.getConfigurationBuilder()
                        .addDefaultPropertyFilters()
                        .build());

    }

    @Test
    public void testPropertyFiltersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyfilters-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertFalse(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(1, config.getContext().getPropertyFilters().size());
        assertTrue(config.getContext().getPropertyFilters().get(0) instanceof CachedFilter);
    }

    @Test
    public void testPropertyConvertersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyconverters-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertFalse(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(1, config.getContext().getPropertyConverters().size());
        List<PropertyConverter<Object>> converters = config.getContext().getPropertyConverters(TypeLiteral.of(String.class));
        assertTrue(converters.get(0).getClass().equals(MyConverter.class));
    }

    @Test
    public void testPropertySourcesConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertysources-test.xml"));
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertFalse(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(2, config.getContext().getPropertySources().size());
        assertTrue(config.getContext().getPropertySources().get(0) instanceof MyPropertySource);
    }

    @Test
    public void testPropertyFilterConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyfilter-config-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertFalse(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(1, config.getContext().getPropertyFilters().size());
        PropertyFilter filter = config.getContext().getPropertyFilters().get(0);
        assertNotNull(filter);
        assertTrue(filter instanceof MyFilter);
        MyFilter myFilter = (MyFilter)filter;
        assertEquals("my-filter-name", myFilter.getName());
        assertEquals("attrValue1", myFilter.getAttrValue());
        assertEquals("elemValue1", myFilter.getElemValue());
        assertEquals("overrideValue2", myFilter.getOverrideValue());
    }

    @Test
    public void testPropertySourceConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertysource-config-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertFalse(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(1, config.getContext().getPropertySources().size());
        PropertySource ps = config.getContext().getPropertySources().get(0);
        assertNotNull(ps);
        assertTrue(ps instanceof MyPropertySource);
        MyPropertySource mySource = (MyPropertySource)ps;
        assertEquals("my-source-name", mySource.getName2());
        assertEquals("attrValue1", mySource.getAttrValue());
        assertEquals("elemValue1", mySource.getElemValue());
        assertEquals("overrideValue2", mySource.getOverrideValue());
    }

    @Test
    public void testPropertyConverterConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyconverter-config-test.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertFalse(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
        assertEquals(1, config.getContext().getPropertyConverters().size());
        PropertyConverter<?> converter = config.getContext().getPropertyConverters().values().iterator()
                .next().get(0);
        assertNotNull(converter);
        assertTrue(converter instanceof MyConverter);
        MyConverter myConverter = (MyConverter)converter;
        assertEquals("my-converter-name", myConverter.getName());
        assertEquals("attrValue1", myConverter.getAttrValue());
        assertEquals("elemValue1", myConverter.getElemValue());
        assertEquals("overrideValue2", myConverter.getOverrideValue());
    }

    private URL getConfig(String resource) {
        return getClass().getClassLoader().getResource(resource);
    }

}
