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
package org.apache.tamaya.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by atsticks on 19.09.17.
 */
final class ConfigChanger {

    private static final Logger LOG = Logger.getLogger(TamayaConfigPlugin.class.getName());
    private static final OSGIConfigMapper DEFAULT_CONFIG_MAPPER = new DefaultOSGIConfigMapper();

    private BundleContext context;
    private ConfigurationAdmin cm;

    public ConfigChanger(BundleContext context){
        this.context = context;
        ServiceReference<ConfigurationAdmin> cmRef = context.getServiceReference(ConfigurationAdmin.class);
        this.cm = context.getService(cmRef);
    }

    public BundleContext getContext(){
        return context;
    }

    public ConfigurationAdmin getConfigurationAdmin(){
        return cm;
    }

    public void configure(String pid, Bundle bundle, OperationMode defaultOpMode) {
        OperationMode opMode = Objects.requireNonNull(defaultOpMode);
        if(bundle!=null) {
            String opVal = bundle.getHeaders().get("Tamaya-OperationMode");
            if (opVal != null) {
                opMode = OperationMode.valueOf(opVal.toUpperCase());
            }
        }
        LOG.finest("Evaluating Tamaya Config for PID: " + pid);
        if(bundle!=null) {
            ConfigHistory.configuring(pid, "bundle=" + bundle.getSymbolicName() + ", bundle-id=" + bundle.getBundleId() + ", operationMode=" + opMode);
        }else{
            ConfigHistory.configuring(pid, "trigger=Tamaya, operationMode=" + opMode);
        }
        org.apache.tamaya.Configuration tamayaConfig = configMapper().getConfiguration(pid);
        if (tamayaConfig == null) {
            LOG.finest("No Tamaya configuration for PID: " + pid);
            return;
        }
        try {
            // TODO Check for Bundle.getLocation() usage here...
            Configuration osgiConfig = cm.getConfiguration(pid, bundle!=null?bundle.getLocation():null);
            if(osgiConfig!=null){
                Dictionary<String, Object> dictionary = osgiConfig.getProperties();
                if(dictionary==null){
                    dictionary = new Hashtable<>();
                }else{
                    if(!InitialState.contains(pid)){
                        InitialState.set(pid, dictionary);
                    }
                }
                modifyConfiguration(pid, tamayaConfig, dictionary, opMode);
                if(!dictionary.isEmpty()) {
                    osgiConfig.update(dictionary);
                    LOG.info("Updated configuration for PID: " + pid + ": " + dictionary);
                }
            }
            ConfigHistory.configured(pid, "SUCCESS");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize configuration for PID: " + pid, e);
            ConfigHistory.configured(pid, "FAILED: " + e);
        }

    }

    public void modifyConfiguration(String pid, org.apache.tamaya.Configuration config, Dictionary<String, Object> dictionary, OperationMode opMode) {
        LOG.info(() -> "Updating configuration for PID: " + pid + "...");
        dictionary.put("tamaya.opMode", opMode.toString());
        ConfigHistory.propertySet(pid, "tamaya.opMode", opMode.toString(), null);
        dictionary.put("tamaya.modified.at", new Date().toString());
        ConfigHistory.propertySet(pid, "tamaya.modified.at", dictionary.get("tamaya.modified.at"), null);

        Map<String, Object> dictionaryMap = new HashMap<>();
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object value = dictionary.get(key);
            dictionaryMap.put(key, value);
        }
        for (Map.Entry<String, Object> dictEntry : dictionaryMap.entrySet()) {
            Object configuredValue = config.getOrDefault(dictEntry.getKey(), dictEntry.getValue().getClass(), null);
            switch (opMode) {
                case EXTEND:
                    break;
                case OVERRIDE:
                    if (configuredValue != null) {
                        LOG.info(() -> "Setting key " + dictEntry.getKey() + " to " + configuredValue);
                        ConfigHistory.propertySet(pid,dictEntry.getKey(), configuredValue, dictEntry.getValue());
                        dictionary.put(dictEntry.getKey(), configuredValue);
                    }
                    break;
                case UPDATE_ONLY:
                    if (configuredValue != null) {
                        LOG.info(() -> "Setting key " + dictEntry.getKey() + " to " + configuredValue);
                        ConfigHistory.propertySet(pid,dictEntry.getKey(), configuredValue, dictEntry.getValue());
                        dictionary.put(dictEntry.getKey(), configuredValue);

                    }
            }
        }
        for (Map.Entry<String, String> configEntry : config.getProperties().entrySet()) {
            Object dictValue = dictionary.get(configEntry.getKey());
            switch (opMode) {
                case EXTEND:
                    if(dictValue==null){
                        LOG.info(() -> "Setting key " + configEntry.getKey() + " to " + configEntry.getValue());
                        ConfigHistory.propertySet(pid,configEntry.getKey(), configEntry.getValue(), null);
                        dictionary.put(configEntry.getKey(), configEntry.getValue());
                    }
                    break;
                case OVERRIDE:
                    LOG.info(() -> "Setting key " + configEntry.getKey() + " to " + configEntry.getValue());
                    ConfigHistory.propertySet(pid,configEntry.getKey(), configEntry.getValue(), null);
                    dictionary.put(configEntry.getKey(), configEntry.getValue());
                    break;
                case UPDATE_ONLY:
                    if(dictValue!=null){
                        LOG.info(() -> "Setting key " + configEntry.getKey() + " to " + configEntry.getValue());
                        ConfigHistory.propertySet(pid,configEntry.getKey(), configEntry.getValue(), dictValue);
                        dictionary.put(configEntry.getKey(), configEntry.getValue());
                    }
                    break;
            }
        }
    }

    /**
     * Loads the configuration toor mapper using the OSGIConfigRootMapper OSGI service resolving mechanism. If no
     * such service is available it loads the default mapper.
     * @return the mapper to be used, bever null.
     */
    private OSGIConfigMapper configMapper() {
        OSGIConfigMapper mapper = null;
        if(context!=null) {
            ServiceReference<OSGIConfigMapper> ref = context.getServiceReference(OSGIConfigMapper.class);
            if (ref != null) {
                mapper = context.getService(ref);
            }
        }
        if(mapper==null){
            return DEFAULT_CONFIG_MAPPER;
        }
        return mapper;
    }

    public org.apache.tamaya.Configuration getTamayaConfiguration(String pid) {
        return configMapper().getConfiguration(pid);
    }
}
