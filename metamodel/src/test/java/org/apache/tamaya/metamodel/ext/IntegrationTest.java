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
import org.apache.tamaya.metamodel.MapFilter;
import org.apache.tamaya.metamodel.MetaConfiguration;
import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by atsticks on 06.12.16.
 */
public class IntegrationTest {

    @Test
    public void checkSystemLoads(){
        assertNotNull(ConfigurationProvider.getConfiguration());
        System.out.println(ConfigurationProvider.getConfiguration());
    }

    @Test
    public void testEmptyConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/empty-config.xml"));
        assertNotNull(config);
        assertTrue(config.getProperties().isEmpty());
        assertTrue(config.getContext().getPropertySources().isEmpty());
        assertTrue(config.getContext().getPropertyConverters().isEmpty());
        assertTrue(config.getContext().getPropertyFilters().isEmpty());
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
        assertEquals(config.getContext(),
                ConfigurationProvider.getConfigurationContextBuilder()
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
        assertEquals(config.getContext(),
                ConfigurationProvider.getConfigurationContextBuilder()
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
        assertEquals(config.getContext(),
                ConfigurationProvider.getConfigurationContextBuilder()
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

    private URL getConfig(String resource) {
        return getClass().getClassLoader().getResource(resource);
    }

}
