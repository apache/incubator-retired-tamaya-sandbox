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

import org.apache.tamaya.base.ConfigContext;
import org.apache.tamaya.base.ConfigContextSupplier;
import org.apache.tamaya.base.filter.Filter;
import org.apache.tamaya.metamodel.CachedFilter;
import org.apache.tamaya.metamodel.MetaConfig;
import org.apache.tamaya.metamodel.MetaContext;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;
import javax.config.spi.ConfigSource;
import javax.config.spi.Converter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.*;

/**
 * Created by atsticks on 06.12.16.
 */
public class IntegrationTest {

    @Test
    public void checkSystemLoads(){
        assertNotNull(ConfigProvider.getConfig());
        System.out.println(ConfigProvider.getConfig());
    }

    @Test
    public void testEmptyConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/empty-config.xml"));
        assertNotNull(config);
        assertTrue(!config.getPropertyNames().iterator().hasNext());
        assertTrue(!config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
        }
    }

    @Test
    public void testMetaContextConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/context-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getFilters().isEmpty());
        }
        MetaContext ctx = MetaContext.getInstance();
        assertFalse(ctx.getProperties().isEmpty());
        assertEquals(ctx.getId(), ctx.getProperty("_id"));
        assertEquals("NONE", ctx.getProperty("app"));
        assertEquals("DEV", ctx.getProperty("stage"));
        assertEquals(".", ctx.getProperty("configdir"));

    }

    @Test
    public void testDefaultConvertersConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/default-propertyconverters-test.xml"));
        assertNotNull(config);
        assertFalse(config.getConfigSources().iterator().hasNext());
        assertFalse(config.getPropertyNames().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertFalse(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
        }
    }

    @Test
    public void testDefaultPropertySourcesConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/default-propertysources-test.xml"));
        assertNotNull(config);
        assertTrue(config.getPropertyNames().iterator().hasNext());
        assertTrue(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
        }
    }

    @Test
    public void testDefaultPropertyFiltersConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/default-propertyfilters-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertFalse(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertFalse(context.getFilters().isEmpty());
        }
    }

    @Test
    public void testPropertyFiltersConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/propertyfilters-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertFalse(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertFalse(context.getFilters().isEmpty());
            assertEquals(1, context.getFilters().size());
            assertTrue(context.getFilters().get(0) instanceof CachedFilter);
        }
    }

    @Test
    public void testPropertyConvertersConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/propertyconverters-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertFalse(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertFalse(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
            assertEquals(1, context.getConverters().size());
            List<Converter> converters = context.getConverters(String.class);
            assertTrue(converters.get(0).getClass().equals(MyConverter.class));
        }
    }

    @Test
    public void testPropertySourcesConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/propertysources-test.xml"));
        assertNotNull(config);
        assertTrue(config.getPropertyNames().iterator().hasNext());
        assertTrue(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
            assertTrue(context.getConfigSources().iterator().next() instanceof MyConfigSource);
        }
    }

    @Test
    public void testPropertyFilterConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/propertyfilter-config-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertFalse(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertFalse(context.getFilters().isEmpty());
            assertEquals(1, context.getFilters().size());
            Filter filter = context.getFilters().get(0);
            assertNotNull(filter);
            assertTrue(filter instanceof MyFilter);
            MyFilter myFilter = (MyFilter)filter;
            assertEquals("my-filter-name", myFilter.getName());
            assertEquals("attrValue1", myFilter.getAttrValue());
            assertEquals("elemValue1", myFilter.getElemValue());
            assertEquals("overrideValue2", myFilter.getOverrideValue());
        }
    }

    @Test
    public void testPropertySourceConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/propertysource-config-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertTrue(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertTrue(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
            assertTrue(context.getConfigSources().iterator().hasNext());
            Iterator it = context.getConfigSources().iterator();
            it.next();
            assertFalse(it.hasNext());
        }
        ConfigSource ps = config.getConfigSources().iterator().next();
        assertNotNull(ps);
        assertTrue(ps instanceof MyConfigSource);
        MyConfigSource mySource = (MyConfigSource)ps;
        assertEquals("my-source-name", mySource.getName2());
        assertEquals("attrValue1", mySource.getAttrValue());
        assertEquals("elemValue1", mySource.getElemValue());
        assertEquals("overrideValue2", mySource.getOverrideValue());
    }

    @Test
    public void testPropertyConverterConfig(){
        Config config = MetaConfig.createConfig(getConfig("IntegrationTests/propertyconverter-config-test.xml"));
        assertNotNull(config);
        assertFalse(config.getPropertyNames().iterator().hasNext());
        assertFalse(config.getConfigSources().iterator().hasNext());
        if(config instanceof ConfigContextSupplier) {
            ConfigContext context = ((ConfigContextSupplier) config).getConfigContext();
            assertFalse(context.getConverters().isEmpty());
            assertTrue(context.getFilters().isEmpty());
            assertEquals(1, context.getConverters().size());
            Converter converter = context.getConverters().values().iterator()
                    .next().get(0);
            assertNotNull(converter);
            assertTrue(converter instanceof MyConverter);
            MyConverter myConverter = (MyConverter)converter;
            assertEquals("my-converter-name", myConverter.getName());
            assertEquals("attrValue1", myConverter.getAttrValue());
            assertEquals("elemValue1", myConverter.getElemValue());
            assertEquals("overrideValue2", myConverter.getOverrideValue());
        }
    }

    private URL getConfig(String resource) {
        return getClass().getClassLoader().getResource(resource);
    }

}
