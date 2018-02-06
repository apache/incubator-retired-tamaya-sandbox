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
package org.apache.tamaya.meta;


import org.apache.tamaya.meta.spi.MetaPropertyMapping;
import org.apache.tamaya.base.ServiceContextManager;

import javax.config.Config;
import javax.config.spi.ConfigSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;


/**
 * Accessor singleton for Meta-entries.
 */
public final class MetaPropertyMapper {

    /** The logger used. */
    private final static Logger LOG = Logger.getLogger(MetaPropertyMapper.class.getName());


    /**
     * Private singleton constructor.
     */
    private MetaPropertyMapper(){}

    /**
     * Checks if the given key is a meta-entry.
     * @param key the config key, not null.
     * @return true, if the entry is a meta-entry.
     */
    public static boolean isMetaEntry(String key){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        return mapping.getMetaEntryFilter(null).test(key);
    }

    /**
     * Get all meta-entries of the given config instance.
     * @param config the config, not null.
     * @return the meta entries found.
     */
    public static Map<String,String> getMetaEntries(Config config){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        Map<String,String> result = new HashMap<>();
        Predicate<String> filter = mapping.getMetaEntryFilter(null);
        config.getPropertyNames().forEach(k -> {
            if(filter.test(k)){
                result.put(k, config.getValue(k, String.class));
            }
        });
        return result;
    }

    /**
     * Get all meta-entries of the given config source and entry of the given configuration.
     * @param config the config, not null
     * @param configSource the configSource name, not null.
     * @return the meta entries found.
     */
    public static Map<String,String> getMetaEntriesForConfigSource(Config config, String configSource){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        Map<String,String> result = new HashMap<>();
        Predicate<String> filter = mapping.getMetaEntryFilter(null);
        for(ConfigSource cs:config.getConfigSources()){
            if(cs.getName().equals(configSource)){
                cs.getProperties().forEach((key, value) -> {
                    if (filter.test(key)) {
                        result.put(key, value);
                    }
                });
            }
        }
        return result;
    }

    /**
     * Get all meta-entries of the given config instance and entry.
     * @param config the config, not null
     * @param key the target key, not null.
     * @return the meta entries found.
     */
    public static Map<String,String> getMetaEntries(Config config, String key){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        Map<String,String> result = new HashMap<>();
        Predicate<String> filter = mapping.getMetaEntryFilter(key);
        config.getPropertyNames().forEach(k -> {
            if(filter.test(k)){
                result.put(k, config.getValue(k, String.class));
            }
        });
        return result;
    }

    /**
     * Get the given metaentry.
     * @param config the config, not null
     * @param key the target key, not null.
     * @param metaEntry the meta entry key, not null.
     * @return the meta entry value.
     */
    public static String getMetaEntry(Config config, String key, String metaEntry){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        return config.getValue(mapping.getKey(key, metaEntry), String.class);
    }

    /**
     * Get the given metaentry.
     * @param config the config, not null
     * @param key the target key, not null.
     * @param metaEntry the meta entry key, not null.
     * @param targetType  the target type, not null.
     * @return the meta entry value.
     */
    public static <T> T getMetaEntry(Config config, String key, String metaEntry, Class<T> targetType){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        return config.getValue(mapping.getKey(key, metaEntry), targetType);
    }

    /**
     * Get the given (optional) metaentry.
     * @param config the config, not null
     * @param key the target key, not null.
     * @param metaEntry the meta entry key, not null.
     * @return the meta entry value.
     */
    public static Optional<String> getOptionalMetaEntry(Config config, String key, String metaEntry){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        return config.getOptionalValue(mapping.getKey(key, metaEntry), String.class);
    }

    /**
     * Get the given (optional) metaentry.
     * @param config the config, not null
     * @param key the target key, not null.
     * @param metaEntry the meta entry key, not null.
     * @param targetType  the target type, not null.
     * @return the meta entry value.
     */
    public static <T> Optional<T> getOptionalMetaEntry(Config config, String key, String metaEntry, Class<T> targetType) {
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        return config.getOptionalValue(mapping.getKey(key, metaEntry), targetType);
    }

    /**
     * Get the corresponding meta key for a given key. This method can be used by property sources to add
     * the correct meta keys, hereby using the correct metadata representation key layout.
     * @param entryKey the key of the config entry, not null.
     * @param metaKey the metadata key, not null.
     * @return the key to be used to store/reference the given metadata.
     */
    public static String getMetaKey(String entryKey, String metaKey){
        MetaPropertyMapping mapping = ServiceContextManager.getServiceContext().getService(MetaPropertyMapping.class);
        return mapping.getKey(entryKey, metaKey);
    }


}

