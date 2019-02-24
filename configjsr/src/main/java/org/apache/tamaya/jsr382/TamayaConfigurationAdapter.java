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
import org.apache.tamaya.ConfigurationSnapshot;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spisupport.DefaultConfigurationSnapshot;

import javax.config.Config;
import java.util.*;

/**
 * Configuration implementation that wraps a Javaconfig {@link Config} instance.
 */
class TamayaConfigurationAdapter implements Configuration {

    private Config delegate;
    private ConfigurationContext context;

    /**
     * Creates a new adapter based on the given {@link Config}.
     *
     * @param config the config, not null.
     */
    public TamayaConfigurationAdapter(Config config) {
        this.delegate = Objects.requireNonNull(config);
        this.context = JavaConfigAdapterFactory.toConfigurationContext(config);
    }

    /**
     * Get the underlying config.
     *
     * @return the underlying config, not null.
     */
    public Config getConfig() {
        return delegate;
    }

    @Override
    public String get(String key) {
        return this.delegate.getOptionalValue(key, String.class).orElse(null);
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        return this.delegate.getOptionalValue(key, String.class).orElse(defaultValue);
    }

    @Override
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        return this.delegate.getOptionalValue(key, type).orElse(defaultValue);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return this.delegate.getOptionalValue(key, type).orElseThrow(
                () -> new NoSuchElementException("Missing key: " + key));
    }

    @Override
    public <T> T get(String key, TypeLiteral<T> type) {
        return this.delegate.getOptionalValue(key, type.getRawType()).orElseThrow(
                () -> new NoSuchElementException("Missing key: " + key));
    }

    @Override
    public <T> T getOrDefault(String key, TypeLiteral<T> type, T defaultValue) {
        return delegate.getOptionalValue(key, type.getRawType()).orElse(defaultValue);
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        for (String key : this.delegate.getPropertyNames()) {
            properties.put(key, delegate.getValue(key, String.class));
        }
        return properties;
    }

    @Override
    public ConfigurationSnapshot getSnapshot(Iterable<String> keys) {
        return new DefaultConfigurationSnapshot(this, keys);
    }

    @Override
    public ConfigurationContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "TamayaConfigurationAdapter{" +
                "delegate=" + delegate +
                '}';
    }
}
