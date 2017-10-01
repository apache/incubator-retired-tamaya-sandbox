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

import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by atsticks on 19.09.17.
 */
final class ConfigChanger {

    private static final Logger LOG = Logger.getLogger(TamayaConfigPlugin.class.getName());

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

    public Dictionary<String, Object> configure(String pid, Bundle bundle, Policy policy, boolean opModeExplicit, boolean dryRun) {
        try {
            String root = '[' + pid + ']';
            // TODO Check for Bundle.getLocation() usage here...
            Configuration osgiConfig = cm.getConfiguration(pid, bundle!=null?bundle.getLocation():null);
            Policy opMode = Objects.requireNonNull(policy);
            // Check manifest config
            if(bundle!=null) {
                if(!opModeExplicit) {
                    String opVal = bundle.getHeaders().get(TamayaConfigPlugin.TAMAYA_POLICY_MANIFEST);
                    if (opVal != null) {
                        opMode = Policy.valueOf(opVal.toUpperCase());
                    }
                }
                String customRoot = bundle.getHeaders().get(TamayaConfigPlugin.TAMAYA_CUSTOM_ROOT_MANIFEST);
                if(customRoot!=null){
                    root = customRoot;
                }
            }
            // Check for dynamic OSGI overrides
            if(osgiConfig!=null){
                Dictionary<String,Object> props = osgiConfig.getProperties();
                if(props!=null){
                    if(!opModeExplicit) {
                        String opVal = (String) props.get(TamayaConfigPlugin.TAMAYA_POLICY_PROP);
                        if (opVal != null) {
                            opMode = Policy.valueOf(opVal.toUpperCase());
                        }
                    }
                    String customRoot = (String)props.get(TamayaConfigPlugin.TAMAYA_CUSTOM_ROOT_PROP);
                    if(customRoot!=null){
                        root = customRoot;
                    }
                }else{
                    props = new Hashtable<>();
                }
                if(!dryRun && !Backups.contains(pid)){
                    Backups.set(pid, props);
                    LOG.finest("Stored OSGI configuration backup for PID: " + pid);
                }
                LOG.finest("Evaluating Tamaya Config for PID: " + pid);
                org.apache.tamaya.Configuration tamayaConfig = getTamayaConfiguration(root);
                if (tamayaConfig == null) {
                    LOG.finest("No Tamaya configuration for root: " + root);
                }else {
                    if(dryRun){
                        modifyConfiguration(pid, tamayaConfig, props, opMode);
                    }else {
                        try {
                            if (bundle != null) {
                                ConfigHistory.configuring(pid, "bundle=" + bundle.getSymbolicName() + ", opMode=" + opMode);
                            } else {
                                ConfigHistory.configuring(pid, "trigger=Tamaya, opMode=" + opMode);
                            }
                            modifyConfiguration(pid, tamayaConfig, props, opMode);
                            if (!props.isEmpty()) {
                                osgiConfig.update(props);
                                LOG.info("Updated configuration for PID: " + pid + ": " + props);
                                ConfigHistory.configured(pid, "SUCCESS");
                            }
                        }catch(Exception e){
                            LOG.log(Level.WARNING, "Failed to update configuration for PID: " + pid, e);
                            ConfigHistory.configured(pid, "FAILED: " + e);
                        }
                    }
                }
                return props;
            }
            return null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize configuration for PID: " + pid, e);
            return null;
        }
    }

    public void modifyConfiguration(String pid, org.apache.tamaya.Configuration config, Dictionary<String, Object> dictionary, Policy opMode) {
        LOG.info(() -> "Updating configuration for PID: " + pid + "...");
        dictionary.put("tamaya.modified.at", new Date().toString());

        Map<String, Object> dictionaryMap = new HashMap<>();
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object value = dictionary.get(key);
            dictionaryMap.put(key, value);
        }
        for (Map.Entry<String, Object> dictEntry : dictionaryMap.entrySet()) {
            Object configuredValue = config.getOrDefault(dictEntry.getKey(), dictEntry.getValue().getClass(), null);
            if (configuredValue != null) {
                if(configuredValue.equals(dictEntry.getValue())){
                    continue;
                }
                switch (opMode) {
                    case EXTEND:
                        break;
                    case OVERRIDE:
                        LOG.info(() -> "Setting key " + dictEntry.getKey() + " to " + configuredValue);
                        ConfigHistory.propertySet(pid,dictEntry.getKey(), configuredValue, dictEntry.getValue());
                        dictionary.put(dictEntry.getKey(), configuredValue);
                        break;
                    case UPDATE_ONLY:
                        LOG.info(() -> "Setting key " + dictEntry.getKey() + " to " + configuredValue);
                        ConfigHistory.propertySet(pid,dictEntry.getKey(), configuredValue, dictEntry.getValue());
                        dictionary.put(dictEntry.getKey(), configuredValue);
                }
            }
        }
        for (Map.Entry<String, String> configEntry : config.getProperties().entrySet()) {
            Object dictValue = dictionary.get(configEntry.getKey());
            if(dictValue!=null && dictValue.equals(configEntry.getValue())){
                continue;
            }
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

    public org.apache.tamaya.Configuration getTamayaConfiguration(String root) {
        if (root != null) {
            return ConfigurationProvider.getConfiguration()
                    .with(ConfigurationFunctions.section(root, true));
        }
        return null;
    }

    public void restoreBackup(String pid, Dictionary<String, Object> config)throws IOException{
        Configuration osgiConfig = cm.getConfiguration(pid);
        if(osgiConfig!=null){
            config.put(TamayaConfigPlugin.TAMAYA_ENABLED_PROP, "false");
            osgiConfig.update(Objects.requireNonNull(config));
        }
    }
}
