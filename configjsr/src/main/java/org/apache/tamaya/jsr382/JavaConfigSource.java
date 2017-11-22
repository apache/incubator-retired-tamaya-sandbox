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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import javax.config.spi.ConfigSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Javaconfig {@link ConfigSource} implementation that wraps a {@link PropertySource} instance.
 */
public class JavaConfigSource implements ConfigSource{

    private PropertySource delegate;

    public JavaConfigSource(PropertySource propertySource){
        this.delegate = Objects.requireNonNull(propertySource);
    }

    public PropertySource getPropertySource(){
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
    public String getValue(String key) {
        PropertyValue value = delegate.get(key);
        if(value!=null){
            return value.getValue();
        }
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return toMap(delegate.getProperties());
    }

    private Map<String, String> toMap(Map<String, PropertyValue> properties) {
        Map<String, String> valueMap = new HashMap<>(properties.size());
        for(Map.Entry<String,PropertyValue> en:properties.entrySet()){
            if(en.getValue().getValue()!=null) {
                valueMap.put(en.getKey(), en.getValue().getValue());
            }
        }
        return valueMap;
    }

}
