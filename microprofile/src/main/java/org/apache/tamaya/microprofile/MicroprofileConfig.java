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
package org.apache.tamaya.microprofile;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.PropertySource;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.*;

/**
 * Microprofile {@link ConfigSource} implementation that wraps a {@link PropertySource} instance.
 */
final class MicroprofileConfig implements Config {

    private Configuration delegate;

    MicroprofileConfig(Configuration delegate){
        this.delegate = Objects.requireNonNull(delegate);
    }

    public Configuration getConfiguration(){
        return this.delegate;
    }


    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        T value = null;
        try{
            value = delegate.get(propertyName, propertyType);
        }catch(ConfigException e){
            if(e.toString().contains("Unparseable")){
                throw new IllegalArgumentException("Invalid type: " + propertyType.getName());
            }
        }
        if(value == null){
            throw new NoSuchElementException("No such config property: " + propertyName);
        }
        return value;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        return Optional.ofNullable(delegate.get(propertyName, propertyType));
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return delegate.getProperties().keySet();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return MicroprofileAdapter.toConfigSources(delegate.getContext().getPropertySources());
    }

    @Override
    public String toString() {
        return "MicroprofileConfig{" +
                "delegate=" + delegate +
                '}';
    }
}
