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
package org.apache.tamaya.metamodel.internal;

import org.apache.tamaya.metamodel.MetaConfiguration;
import org.apache.tamaya.spi.*;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.spisupport.DefaultConfiguration;
import org.apache.tamaya.spisupport.DefaultConfigurationContextBuilder;

import javax.annotation.Priority;
import java.util.Comparator;
import java.util.Objects;

/**
 * ConfigurationContext that uses {@link MetaConfiguration} to configure the
 * Tamaya configuration context.
 */
@Priority(10)
public class DSLLoadingConfigurationProviderSpi implements ConfigurationProviderSpi{

    private volatile Configuration config;
    private final Object LOCK = new Object();

    @Override
    public ConfigurationContextBuilder getConfigurationContextBuilder() {
        return ServiceContextManager.getServiceContext().create(ConfigurationContextBuilder.class);
    }

    @Override
    public void setConfiguration(Configuration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public boolean isConfigurationSettable() {
        return true;
    }

    @Override
    public void setConfigurationContext(ConfigurationContext context){
        this.config = Objects.requireNonNull(createConfiguration(context));
    }

    @Override
    public boolean isConfigurationContextSettable() {
        return true;
    }

    @Override
    public Configuration getConfiguration() {
        checkInitialized();
        return config;
    }

    @Override
    public Configuration createConfiguration(ConfigurationContext context) {
        return new DefaultConfiguration(context);
    }

    @Override
    public ConfigurationContext getConfigurationContext() {
        checkInitialized();
        return config.getContext();
    }

    private void checkInitialized() {
        if(config==null){
            synchronized (LOCK) {
                if(config==null){
                    MetaConfiguration.configure();
                }
                if(config==null){
                    // load defaults
                    this.config = new DefaultConfiguration(
                            new DefaultConfigurationContextBuilder()
                                .addDefaultPropertyConverters()
                                .addDefaultPropertyFilters()
                                .addDefaultPropertySources()
                                .sortPropertyFilter(
                                        (Comparator<PropertyFilter>)
                                                DefaultConfigurationContextBuilder.DEFAULT_PROPERTYFILTER_COMPARATOR)
                                .sortPropertySources(DefaultConfigurationContextBuilder.DEFAULT_PROPERTYSOURCE_COMPARATOR)
                                .build());
                }
            }
        }
    }

}
