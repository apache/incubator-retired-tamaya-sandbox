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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsticks on 06.12.16.
 */
public class IntegrationTest {

    @Test
    public void checkSystemLoads(){
        Configuration defaultConfig = Configuration.current();
        assertThat(defaultConfig).isNotNull();
        
        MetaConfiguration.configure();
        Configuration defaultMetaConfig = Configuration.current();
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
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
        MetaContext ctx = MetaContext.getInstance();
        assertThat(ctx.getProperties()).isNotEmpty();
        assertThat(ctx.getId()).isEqualTo(ctx.getProperty("_id"));
        assertThat("NONE").isEqualTo(ctx.getProperty("app"));
        assertThat("DEV").isEqualTo(ctx.getProperty("stage"));
        assertThat(".").isEqualTo(ctx.getProperty("configdir"));
    }

    @Test
    public void testDefaultConvertersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/default-propertyconverters-test.xml"));
        assertThat(config).isNotNull()
            .isEqualTo(Configuration.createConfigurationBuilder()
                        .addDefaultPropertyConverters()
                        .build());
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isNotEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
    }

    @Test
    public void testDefaultPropertySourcesConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/default-propertysources-test.xml"));
        assertThat(config).isNotNull()
            .isEqualTo(Configuration.createConfigurationBuilder()
                    .addDefaultPropertySources().build());
        assertThat(config.getProperties()).isNotEmpty();
        assertThat(config.getContext().getPropertySources()).isNotEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
    }

    @Test
    public void testDefaultPropertyFiltersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/default-propertyfilters-test.xml"));
        assertThat(config).isNotNull()
            .isEqualTo(Configuration.createConfigurationBuilder()
                        .addDefaultPropertyFilters().build());
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isNotEmpty();
    }

    @Test
    public void testPropertyFiltersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyfilters-test.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isNotEmpty().hasSize(1);
        assertThat(config.getContext().getPropertyFilters().get(0)).isInstanceOf(CachedFilter.class);
    }

    @Test
    public void testPropertyConvertersConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyconverters-test.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isNotEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).hasSize(1);
        List<PropertyConverter<Object>> converters = config.getContext().getPropertyConverters(TypeLiteral.of(String.class));
        assertThat(converters.get(0)).isInstanceOf(MyConverter.class);
    }

    @Test
    public void testPropertySourcesConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertysources-test.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isNotEmpty();
        assertThat(config.getContext().getPropertySources()).isNotEmpty().hasSize(2);
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
        assertThat(config.getContext().getPropertySources().get(0)).isInstanceOf(MyPropertySource.class);
    }

    @Test
    public void testPropertyFilterConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyfilter-config-test.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isNotEmpty().hasSize(1);
        PropertyFilter filter = config.getContext().getPropertyFilters().get(0);
        assertThat(filter).isNotNull().isInstanceOf(MyFilter.class);
        MyFilter myFilter = (MyFilter)filter;
        assertThat("my-filter-name").isEqualTo(myFilter.getName());
        assertThat("attrValue1").isEqualTo(myFilter.getAttrValue());
        assertThat("elemValue1").isEqualTo(myFilter.getElemValue());
        assertThat("overrideValue2").isEqualTo(myFilter.getOverrideValue());
    }

    @Test
    public void testPropertySourceConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertysource-config-test.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isNotEmpty().hasSize(1);
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
        PropertySource ps = config.getContext().getPropertySources().get(0);
        assertThat(ps).isNotNull().isInstanceOf(MyPropertySource.class);
        MyPropertySource mySource = (MyPropertySource)ps;
        assertThat("my-source-name").isEqualTo(mySource.getName2());
        assertThat("attrValue1").isEqualTo(mySource.getAttrValue());
        assertThat("elemValue1").isEqualTo(mySource.getElemValue());
        assertThat("overrideValue2").isEqualTo(mySource.getOverrideValue());
    }

    @Test
    public void testPropertyConverterConfig(){
        Configuration config = MetaConfiguration.createConfiguration(getConfig("IntegrationTests/propertyconverter-config-test.xml"));
        assertThat(config).isNotNull();
        assertThat(config.getProperties()).isEmpty();
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isNotEmpty().hasSize(1);
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
        PropertyConverter<?> converter = config.getContext().getPropertyConverters().values().iterator()
                .next().get(0);
        assertThat(converter).isNotNull().isInstanceOf(MyConverter.class);
        MyConverter myConverter = (MyConverter)converter;
        assertThat("my-converter-name").isEqualTo(myConverter.getName());
        assertThat("attrValue1").isEqualTo(myConverter.getAttrValue());
        assertThat("elemValue1").isEqualTo(myConverter.getElemValue());
        assertThat("overrideValue2").isEqualTo(myConverter.getOverrideValue());
    }

    private URL getConfig(String resource) {
        return getClass().getClassLoader().getResource(resource);
    }

}
