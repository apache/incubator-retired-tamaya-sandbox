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


import org.apache.tamaya.*;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.*;

/**
 * Utility class for adapting microprofile artifacts into Tamaya artifacts and vice versa.
 */
public final class MicroprofileAdapter{

    /**
     * Singleton constructor.
     */
    private MicroprofileAdapter(){}

    /**
     * Converts a Tamaya {@link Configuration} into a Microprofile.io {@Config}.
     * @param config the Tamaya {@link Configuration} instance, not null.
     * @return the corresponding Microprofile.io {@Config} instance, never null.
     */
    public static Config toConfig(Configuration config){
        if(config instanceof TamayaConfiguration){
            return ((TamayaConfiguration)config).getConfig();
        }
        return new MicroprofileConfig(config);
    }

    /**
     * Converts a Microprofile {@link Config}s into Tamaya {@Configuration}s.
     * @param config the Microprofile {@link Config} instance, not null.
     * @return the corresponding Tamaya {@Configuration} instance, never null.
     */
    public static Configuration toConfiguration(Config config){
        if(config instanceof MicroprofileConfig){
            return ((MicroprofileConfig)config).getConfiguration();
        }
        return new TamayaConfiguration(config);
    }

    /**
     * Converts a Tamaya {@link PropertySource}s into a Microprofile.io {@ConfigSource}.
     * @param propertySources the Tamaya {@link PropertySource} instances, not null.
     * @return the corresponding Microprofile.io {@ConfigSource} instance, never null.
     */
    public static List<ConfigSource> toConfigSources(Iterable<PropertySource> propertySources) {
        List<ConfigSource> configSources = new ArrayList<>();
        for(PropertySource ps:propertySources){
            configSources.add(toConfigSource(ps));
        }
        return configSources;
    }

    /**
     * Converts a Microprofile {@link ConfigSource}s into Tamaya {@PropertySource}s.
     * @param configSources the Microprofile {@link ConfigSource} instances, not null.
     * @return the corresponding Tamaya {@PropertySource} instances, never null.
     */
    public static List<PropertySource> toPropertySources(Iterable<ConfigSource> configSources) {
        List<PropertySource> propertySources = new ArrayList<>();
        for(ConfigSource cs:configSources){
            propertySources.add(toPropertySource(cs));
        }
        return propertySources;
    }

    /**
     * Converts a Tamaya {@link PropertySource} into a Microprofile.io {@ConfigSource}.
     * @param propertySource the Tamaya {@link PropertySource} instance, not null.
     * @return the corresponding Microprofile.io {@ConfigSource} instance, never null.
     */
    public static ConfigSource toConfigSource(PropertySource propertySource) {
        if(propertySource instanceof TamayaPropertySource){
            return ((TamayaPropertySource)propertySource).getConfigSource();
        }
        return new MicroprofileConfigSource(propertySource);
    }

    /**
     * Converts a Microprofile {@link ConfigSource} into a Tamaya {@PropertySource}.
     * @param configSource the Microprofile {@link ConfigSource} instance, not null.
     * @return the corresponding Tamaya {@PropertySource} instance, never null.
     */
    public static PropertySource toPropertySource(ConfigSource configSource) {
        if(configSource instanceof MicroprofileConfigSource){
            return ((MicroprofileConfigSource)configSource).getPropertySource();
        }
        return new TamayaPropertySource(configSource);
    }

    /**
     * Converts a Microprofile {@link Converter} into a Tamaya {@PropertyConverter}.
     * @param converter the Microprofile {@link Converter} instance, not null.
     * @return the corresponding Tamaya {@PropertyConverter} instance, never null.
     */
    public static <T> PropertyConverter<T> toPropertyConverter(Converter<T> converter) {
        if(converter instanceof MicroprofileConverter){
            return ((MicroprofileConverter)converter).getPropertyConverter();
        }
        return new TamayaPropertyConverter(converter);
    }

    /**
     * Converts a Tamaya {@link PropertyConverter} into a Microprofile.io {@Converter}.
     * @param converter the Tamaya {@link PropertyConverter} instance, not null.
     * @return the corresponding Microprofile.io {@Converter} instance, never null.
     */
    public static <T> Converter<T> toConverter(PropertyConverter<T> converter) {
        if(converter instanceof TamayaPropertyConverter){
            return ((TamayaPropertyConverter)converter).getConverter();
        }
        return new MicroprofileConverter(converter);
    }

    /**
     * Converts a Tamaya {@link ConfigurationContextBuilder} into a Microprofile.io {@ConfigBuilder}.
     * @param builder the Tamaya {@link ConfigurationContextBuilder} instance, not null.
     * @return the corresponding Microprofile.io {@ConfigBuilder} instance, never null.
     */
    public static ConfigBuilder toConfigBuilder(ConfigurationContextBuilder builder) {
        return new MicroprofileConfigBuilder(builder);
    }

    /**
     * Converts the given Tamaya key, value map into a corresponding String based map, hereby
     * omitting all meta-entries.
     * @param properties the Tamaya key, value map, not null.
     * @return the corresponding String based map, never null.
     */
    public static Map<String, String> toStringMap(Map<String, PropertyValue> properties) {
        Map<String, String> valueMap = new HashMap<>(properties.size());
        for(Map.Entry<String,PropertyValue> en:properties.entrySet()){
            if(en.getValue().getValue()!=null) {
                valueMap.put(en.getKey(), en.getValue().getValue());
            }
        }
        return valueMap;
    }

    /**
     * Converts the given String based key, value map into a corresponding String,PropertyValue
     * based map.
     * @param properties the String based key, value map, not null.
     * @param source the source of the entries, not null.
     * @return the corresponding String,PropertyValue based map, never null.
     */
    public static Map<String, PropertyValue> toPropertyValueMap(Map<String, String> properties, String source) {
        Map<String, PropertyValue> valueMap = new HashMap<>(properties.size());
        for(Map.Entry<String,String> en:properties.entrySet()){
            valueMap.put(en.getKey(), PropertyValue.of(en.getKey(), en.getValue(), source));
        }
        return valueMap;
    }

}
