/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.vertx;

import io.vertx.core.AbstractVerticle;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.ConfigurationInjection;

/**
 * Base verticle class that adds some convenience methods for accessing configuration.
 * The class also performs configuration injection using {@link ConfigurationInjection}.
 */
public abstract class AbstractConfiguredVerticle extends AbstractVerticle{

    private Configuration configuration;

    public AbstractConfiguredVerticle() {
        configure();
    }

    public Configuration getConfiguration(){
        if(configuration==null){
            return ConfigurationProvider.getConfiguration();
        }
        return configuration;
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    protected void configure(){
        ConfigurationInjection.getConfigurationInjector().configure(this, getConfiguration());
    }

    protected final String getConfigProperty(String key){
        return getConfiguration().get(key);
    }

    protected final String getConfigPropertyOrDefault(String key, String defaultValue){
        String val = getConfiguration().get(key);
        if(val==null){
            return defaultValue;
        }
        return val;
    }

    protected final <T> T getConfigProperty(String key, Class<T> type){
        return getConfiguration().get(key, type);
    }

    protected final <T> T getConfigPropertyOrDefault(String key, Class<T> type, T defaultValue){
        T val = getConfiguration().get(key, type);
        if(val==null){
            return defaultValue;
        }
        return val;
    }
}
