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


import org.apache.tamaya.*;
import org.apache.tamaya.spi.*;

import javax.config.Config;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigSource;
import javax.config.spi.Converter;
import java.util.*;

/**
 * Utility class for adapting JavaConfig artifacts into Tamaya artifacts and vice versa.
 */
public final class JavaConfigAdapter{

    /**
     * Singleton constructor.
     */
    private JavaConfigAdapter(){}

    /**
     * Converts a Tamaya {@link Configuration} into a JavaConfig.io {@link javax.config.Config}.
     * @param config the Tamaya {@link Configuration} instance, not null.
     * @return the corresponding JavaConfig.io {@link Config} instance, never null.
     */
    public static Config toConfig(Configuration config){
        if(config instanceof TamayaConfiguration){
            return ((TamayaConfiguration)config).getConfig();
        }
        return new JavaConfig(config);
    }

    /**
     * Converts a JavaConfig {@link Config}s into Tamaya {@link Configuration}s.
     * @param config the JavaConfig {@link Config} instance, not null.
     * @return the corresponding Tamaya {@link Configuration} instance, never null.
     */
    public static Configuration toConfiguration(Config config){
        if(config instanceof JavaConfig){
            return ((JavaConfig)config).getConfiguration();
        }
        return new TamayaConfiguration(config);
    }

    /**
     * Converts a Tamaya {@link PropertySource}s into a JavaConfig.io {@link ConfigSource}.
     * @param propertySources the Tamaya {@link PropertySource} instances, not null.
     * @return the corresponding JavaConfig.io {@link ConfigSource} instance, never null.
     */
    public static List<ConfigSource> toConfigSources(Iterable<PropertySource> propertySources) {
        List<ConfigSource> configSources = new ArrayList<>();
        for(PropertySource ps:propertySources){
            configSources.add(toConfigSource(ps));
        }
        Collections.reverse(configSources);
        return configSources;
    }

    /**
     * Converts a JavaConfig {@link ConfigSource}s into Tamaya {@link PropertySource}s.
     * @param configSources the JavaConfig {@link ConfigSource} instances, not null.
     * @return the corresponding Tamaya {@link PropertySource} instances, never null.
     */
    public static List<PropertySource> toPropertySources(Iterable<ConfigSource> configSources) {
        List<PropertySource> propertySources = new ArrayList<>();
        for(ConfigSource cs:configSources){
            propertySources.add(toPropertySource(cs));
        }
        return propertySources;
    }

    /**
     * Converts a Tamaya {@link PropertySource} into a JavaConfig.io {@link ConfigSource}.
     * @param propertySource the Tamaya {@link PropertySource} instance, not null.
     * @return the corresponding JavaConfig.io {@link ConfigSource} instance, never null.
     */
    public static ConfigSource toConfigSource(PropertySource propertySource) {
        if(propertySource instanceof TamayaPropertySource){
            return ((TamayaPropertySource)propertySource).getConfigSource();
        }
        return new JavaConfigSource(propertySource);
    }

    /**
     * Converts a JavaConfig {@link ConfigSource} into a Tamaya {@link PropertySource}.
     * @param configSource the JavaConfig {@link ConfigSource} instance, not null.
     * @return the corresponding Tamaya {@link PropertySource} instance, never null.
     */
    public static PropertySource toPropertySource(ConfigSource configSource) {
        if(configSource instanceof JavaConfigSource){
            return ((JavaConfigSource)configSource).getPropertySource();
        }
        return new TamayaPropertySource(configSource);
    }

    /**
     * Converts a JavaConfig {@link Converter} into a Tamaya {@link PropertyConverter}.
     * @param converter the JavaConfig {@link Converter} instance, not null.
     * @param <T> the target type
     * @return the corresponding Tamaya {@link PropertyConverter} instance, never null.
     */
    public static <T> PropertyConverter<T> toPropertyConverter(Converter<T> converter) {
        if(converter instanceof JavaConfigConverter){
            return ((JavaConfigConverter)converter).getPropertyConverter();
        }
        return new TamayaPropertyConverter(converter);
    }

    /**
     * Converts a Tamaya {@link PropertyConverter} into a JavaConfig.io {@link Converter}.
     * @param converter the Tamaya {@link PropertyConverter} instance, not null.
     * @param <T> the target type
     * @return the corresponding JavaConfig.io {@link Converter} instance, never null.
     */
    public static <T> Converter<T> toConverter(PropertyConverter<T> converter) {
        if(converter instanceof TamayaPropertyConverter){
            return ((TamayaPropertyConverter)converter).getConverter();
        }
        return new JavaConfigConverter(converter);
    }

    /**
     * Converts a Tamaya {@link ConfigurationContextBuilder} into a JavaConfig.io {@link ConfigBuilder}.
     * @param builder the Tamaya {@link ConfigurationContextBuilder} instance, not null.
     * @return the corresponding JavaConfig.io {@link ConfigBuilder} instance, never null.
     */
    public static ConfigBuilder toConfigBuilder(ConfigurationBuilder builder) {
        return new JavaConfigBuilder(builder);
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
