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
package org.apache.tamaya.collections;

import org.apache.tamaya.meta.MetaProperties;
import org.apache.tamaya.base.ConfigValueCombinationPolicy;

import javax.annotation.Priority;
import javax.config.ConfigProvider;
import javax.config.spi.ConfigSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PropertyValueCombinationPolicy that allows to configure a PropertyValueCombinationPolicy
 * for each key individually, by adding a configured entry of the form
 * {@code _key.combination-policy=collect|override|fqPolicyClassName}.
 */
@Priority(100)
public class AdaptiveCombinationPolicy implements ConfigValueCombinationPolicy {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AdaptiveCombinationPolicy.class.getName());

    /**
     * Collecting combination policy using (optional) {@code item-separator} parameter for determining the separator
     * to combine multiple config entries found.
     */
    private static final ConfigValueCombinationPolicy COLLECTING_POLICY = new ConfigValueCombinationPolicy(){
        @Override
        public String collect(String currentValue, String key, ConfigSource propertySource) {
            // check for default collection combination policies for lists, sets, maps etc.
            String newValue = propertySource.getValue(key);
            if(newValue!=null){
                if(currentValue==null){
                    return newValue;
                }
                final String separator = MetaProperties.getOptionalMetaEntry(ConfigProvider.getConfig(),
                        key, "item-separator").orElse(",");
                return currentValue + separator + newValue;
            }else{
                if(currentValue!=null){
                    return currentValue;
                }
                return null;
            }
        }
    };

    /** Cache for loaded custom combination policies. */
    private Map<Class, ConfigValueCombinationPolicy> configuredPolicies = new ConcurrentHashMap<>();

    @Override
    public String collect(String currentValue, String key, ConfigSource propertySource){
        if(MetaProperties.isMetaEntry(key)){
            String newValue = propertySource.getValue(key);
            if(newValue!=null){
                return newValue;
            }
            return currentValue;
        }
        String adaptiveCombinationPolicyClass  = MetaProperties.getOptionalMetaEntry(
                ConfigProvider.getConfig(),
                key, "combination-policy").orElse("override");
        ConfigValueCombinationPolicy combinationPolicy = null;
        switch(adaptiveCombinationPolicyClass){
            case "collect":
            case "COLLECT":
                if(LOG.isLoggable(Level.FINEST)){
                    LOG.finest("Using collecting combination policy for key: " + key + "");
                }
                combinationPolicy = COLLECTING_POLICY;
                break;
            case "override":
            case "OVERRIDE":
                if(LOG.isLoggable(Level.FINEST)){
                    LOG.finest("Using default (overriding) combination policy for key: " + key + "");
                }
                combinationPolicy = ConfigValueCombinationPolicy.DEFAULT_OVERRIDING_POLICY;
                break;
            default:
                try{
                    Class<ConfigValueCombinationPolicy> clazz = (Class<ConfigValueCombinationPolicy>)
                            Class.forName(adaptiveCombinationPolicyClass);
                    combinationPolicy = configuredPolicies.get(clazz);
                    if(combinationPolicy==null){
                        combinationPolicy = clazz.newInstance();
                        configuredPolicies.put(clazz, combinationPolicy);
                    }
                    if(LOG.isLoggable(Level.FINEST)){
                        LOG.finest("Using custom combination policy "+adaptiveCombinationPolicyClass+" for " +
                                "key: " + key + "");
                    }
                } catch(Exception e){
                    LOG.log(Level.SEVERE, "Error loading configured PropertyValueCombinationPolicy for " +
                            "key: " + key + ", using default (overriding) policy.", e);
                    combinationPolicy = ConfigValueCombinationPolicy.DEFAULT_OVERRIDING_POLICY;
                }
        }
        return combinationPolicy.collect(currentValue, key, propertySource);
    }
}
