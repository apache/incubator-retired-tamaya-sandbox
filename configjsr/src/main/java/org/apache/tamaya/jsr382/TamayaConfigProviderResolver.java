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

import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.ConfigurationBuilder;

import javax.config.Config;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigProviderResolver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by atsticks on 23.03.17.
 */
public class TamayaConfigProviderResolver extends ConfigProviderResolver {

    private Map<ClassLoader, Config> configs = new ConcurrentHashMap<>();

    @Override
    public Config getConfig() {
        return getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        Config config = this.configs.get(loader);
        if(config==null){
            ConfigurationBuilder builder = Configuration.createConfigurationBuilder();
            builder.setClassLoader(loader);
            builder.addDefaultPropertyConverters();
            JavaConfigBuilderAdapter javaConfigBuilder = new JavaConfigBuilderAdapter(builder);
            javaConfigBuilder.addDefaultSources();
            javaConfigBuilder.addDiscoveredSources();
            config = javaConfigBuilder.build();
            this.configs.put(loader, config);
        }
        return config;
    }

    @Override
    public ConfigBuilder getBuilder() {
        return new JavaConfigBuilderAdapter(Configuration.createConfigurationBuilder());
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        if(configs.containsKey(classLoader)){
            Logger.getLogger(getClass().getName())
                    .warning("Replacing existing config for classloader: " + classLoader);
//            throw new IllegalArgumentException("Already a config registered with classloader: " + classLoader);
        }
        this.configs.put(classLoader, config);
    }

    @Override
    public void releaseConfig(Config config) {
        for(Map.Entry<ClassLoader, Config> en: this.configs.entrySet()){
            if(en.getValue().equals(config)){
                this.configs.remove(en.getKey());
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "TamayaConfigProviderResolver{" +
                "configs=" + configs.size() +
                '}';
    }
}
