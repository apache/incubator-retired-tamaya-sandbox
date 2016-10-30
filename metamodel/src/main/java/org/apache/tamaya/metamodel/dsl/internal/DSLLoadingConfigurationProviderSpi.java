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
package org.apache.tamaya.metamodel.dsl.internal;

import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.metamodel.dsl.TamayaConfigurator;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.ConfigurationProviderSpi;
import org.apache.tamaya.spi.ServiceContextManager;

import javax.annotation.Priority;

/**
 * ConfigurationContext that uses {@link TamayaConfigurator} to configure the
 * Tamaya runtime context.
 */
@Priority(10)
public class DSLLoadingConfigurationProviderSpi implements ConfigurationProviderSpi{

    private boolean configured;

    private ConfigurationContext context = ConfigurationProvider.getConfigurationContextBuilder().build();
    private Configuration config = ConfigurationProvider.createConfiguration(context);


    @Override
    public ConfigurationContextBuilder getConfigurationContextBuilder() {
        return ServiceContextManager.getServiceContext().getService(ConfigurationContextBuilder.class);
    }

    @Override
    public void setConfigurationContext(ConfigurationContext context){
        // TODO think on a SPI or move event part into API...
        this.config = ConfigurationProvider.createConfiguration(context);
        this.context = context;
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
    public ConfigurationContext getConfigurationContext() {
        checkInitialized();
        return context;
    }

    private void checkInitialized() {
        if(!configured){
            synchronized (context) {
                TamayaConfigurator.getInstance().configure();
                configured = true;
            }
        }
    }
}
