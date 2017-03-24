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
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.PropertyValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.util.*;

/**
 * Microprofile {@link ConfigSource} implementation that wraps a {@link PropertySource} instance.
 */
final class MicroprofileConfigSourceProvider implements ConfigSourceProvider{

    private PropertySourceProvider delegate;

    MicroprofileConfigSourceProvider(PropertySourceProvider propertySourceProvider){
        this.delegate = Objects.requireNonNull(propertySourceProvider);
    }

    public PropertySourceProvider getPropertySourceProvider(){
        return this.delegate;
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

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        if(delegate instanceof TamayaPropertySourceProvider){
            return ((TamayaPropertySourceProvider)delegate).getConfigSourceProvider()
                    .getConfigSources(forClassLoader);
        }else {
            return MicroprofileAdapter.toConfigSources(delegate.getPropertySources());
        }
    }
}
