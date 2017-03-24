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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Property source implementation that wraps a Microprofile {@link ConfigSource} instance.
 */
final class TamayaPropertySource implements PropertySource{

    private ConfigSource delegate;

    TamayaPropertySource(ConfigSource configSource){
        this.delegate = Objects.requireNonNull(configSource);
    }

    public ConfigSource getConfigSource(){
        return this.delegate;
    }

    @Override
    public int getOrdinal() {
        return delegate.getOrdinal();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public PropertyValue get(String key) {
        return PropertyValue.of(key, delegate.getValue(key),getName());
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        return toValueMap(delegate.getProperties());
    }

    private Map<String, PropertyValue> toValueMap(Map<String, String> properties) {
        Map<String, PropertyValue> valueMap = new HashMap<>(properties.size());
        for(Map.Entry<String,String> en:properties.entrySet()){
            valueMap.put(en.getKey(), PropertyValue.of(en.getKey(), en.getValue(), getName()));
        }
        return valueMap;
    }

    @Override
    public boolean isScannable() {
        return true;
    }
}
