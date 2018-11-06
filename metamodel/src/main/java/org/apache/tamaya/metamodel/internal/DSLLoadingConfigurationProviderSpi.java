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
import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.*;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spisupport.*;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Priority;
import java.util.Objects;

/**
 * ConfigurationContext that uses {@link MetaConfiguration} to configure the
 * Tamaya configuration context.
 */
@Priority(10)
@Component
public class DSLLoadingConfigurationProviderSpi implements ConfigurationProviderSpi{

    // TODO make this provider multi classloader aware...
    private volatile Configuration config;
    private final Object LOCK = new Object();

    @Override
    public ConfigurationBuilder getConfigurationBuilder() {
        return new DefaultConfigurationBuilder();
    }

    @Override
    public void setConfiguration(Configuration config, ClassLoader classLoader) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public boolean isConfigurationSettable(ClassLoader classLoader) {
        return true;
    }

    @Override
    public Configuration getConfiguration(ClassLoader classLoader) {
        checkInitialized();
        return config;
    }

    @Override
    public Configuration createConfiguration(ConfigurationContext context) {
        return new DefaultConfiguration(context);
    }

    private void checkInitialized() {
        if(config==null){
            synchronized (LOCK) {
                if(config==null){
                    // load defaults
                    this.config = new DefaultConfigurationBuilder()
                                .addDefaultPropertyConverters()
                                .addDefaultPropertyFilters()
                                .addDefaultPropertySources()
                                .sortPropertyFilter(PropertyFilterComparator.getInstance())
                                .sortPropertySources(PropertySourceComparator.getInstance())
                                .build();
                }
            }
        }
    }

}
