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
import org.apache.tamaya.inject.ConfigurationInjection;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.util.Optional;

/**
 * Base verticle class that adds some convenience methods for accessing configuration.
 * The class also performs configuration injection using {@link ConfigurationInjection}.
 */
public abstract class AbstractConfiguredVerticle extends AbstractVerticle{

    private Config configuration;

    public AbstractConfiguredVerticle() {
        configure();
    }

    public Config getConfiguration(){
        if(configuration==null){
            return ConfigProvider.getConfig();
        }
        return configuration;
    }

    public void setConfiguration(Config configuration){
        this.configuration = configuration;
    }

    protected void configure(){
        ConfigurationInjection.getConfigurationInjector().configure(this, getConfiguration());
    }

    protected final String getConfigValue(String key){
        return getConfiguration().getValue(key, String.class);
    }

    protected final Optional<String> getOptionalConfigValue(String key){
        return getConfiguration().getOptionalValue(key, String.class);
    }

    protected final <T> T getConfigValue(String key, Class<T> type){
        return getConfiguration().getValue(key, type);
    }

    protected final <T> Optional<T> getOptionalConfigValue(String key, Class<T> type){
        return getConfiguration().getOptionalValue(key, type);
    }
}
