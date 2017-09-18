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

import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationPlugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tamaya plugin that updates/extends the component configurations managed
 * by {@link ConfigurationAdmin}, based on the configured {@link OperationMode}.
 */
public class TamayaConfigPlugin {
    static final String COMPONENTID = "TamayaConfigPlugin";
    /** the logger. */
    private static final Logger LOG = Logger.getLogger(TamayaConfigPlugin.class.getName());
    private static final OSGIConfigMapper DEFAULT_CONFIG_MAPPER = new DefaultOSGIConfigMapper();
    private static final String TAMAYA_DISABLED = "tamaya.disabled";
    private boolean disabled = false;

    /**
     * Operation mode applied to the config read.
     */
    public enum OperationMode{
        /** Only add properties not existing in the config. */
        EXTEND,
        /** Only override existing properties, but do not add any new ones. */
        OVERRIDE,
        /** Override existing properties and add new properties, but do not remove
         * properties not existing in Tamaya. */
        EXTEND_AND_OVERRIDE,
        /** Use Tamaya config only. */
        SYNCH
    }

    private BundleContext context;
    private OperationMode opMode = OperationMode.EXTEND_AND_OVERRIDE;
    private ConfigurationAdmin cm;

    /**
     * Create a new config.
     * @param context the OSGI context
     */
    TamayaConfigPlugin(BundleContext context) {
        this.context = context;
        ServiceReference<ConfigurationAdmin> cmRef = context.getServiceReference(ConfigurationAdmin.class);
        this.cm = context.getService(cmRef);
        initDefaultOpMode();
        initConfigs();
    }

    private void initConfigs() {
        // Check for matching bundles already installed...
        for(Bundle bundle:context.getBundles()){
            switch(bundle.getState()){
                case Bundle.ACTIVE:
                    configureBundle(bundle);
                    break;
            }
        }
    }

    private void configureBundle(Bundle bundle) {
        // Optional MANIFEST entries
        String enabledTamaya = bundle.getHeaders().get("Tamaya-Enabled");
        String disabledTamaya = bundle.getHeaders().get("Tamaya-Disabled");

        if(Boolean.parseBoolean(disabledTamaya)){
            LOG.finest("Bundle is disabled for Tamaya: " + bundle.getSymbolicName());
            return;
        }
        if(enabledTamaya != null && !Boolean.parseBoolean(enabledTamaya)){
            LOG.finest("Bundle is disabled for Tamaya: " + bundle.getSymbolicName());
            return;
        }
        if(disabled){
            LOG.finest("tamaya.disabled=false: not configuring bundle: " + bundle.getSymbolicName());
            return;
        }

        String tamayaPid = bundle.getHeaders().get("Tamaya-PID");
        String pid = tamayaPid!=null?tamayaPid:bundle.getSymbolicName();
        if(pid==null){
            pid = bundle.getLocation();
        }
        if(pid==null){
            LOG.finest(() -> "No PID/location for bundle " + bundle.getSymbolicName() + '('+bundle.getBundleId()+')');
            return;
        }
        LOG.finest("Evaluating Tamaya Config for PID: " + pid);
        org.apache.tamaya.Configuration tamayaConfig = configMapper().getConfiguration(pid);
        if (tamayaConfig == null) {
            LOG.finest("No Tamaya configuration for PID: " + pid);
            return;
        }
        try {
            // TODO Check for Bundle.getLocation() usage here...
            Configuration osgiConfig = cm.getConfiguration(pid, bundle.getLocation());
            if(osgiConfig!=null){
                Dictionary<String, Object> dictionary = osgiConfig.getProperties();
                if(dictionary==null){
                    dictionary = new Hashtable<>();
                }
                modifyConfiguration(pid, tamayaConfig, dictionary);
                if(!dictionary.isEmpty()) {
                    osgiConfig.update(dictionary);
                    LOG.info("Updated configuration for PID: " + pid + ": " + dictionary);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize configuration for PID: " + pid, e);
        }
    }

    private void initDefaultOpMode() {
        String opVal = (String)getConfigValue(OperationMode.class.getName());
        if(opVal!=null){
            try{
                opMode = OperationMode.valueOf(opVal);
            }catch(Exception e){
                LOG.warning("Invalid OperationMode: " + opMode +", using default: " + opMode);
            }
        }
    }

    private void initDefaultEnabled() {
        String disabledVal = (String)getConfigValue("tamaya.disabled");
        if(disabledVal==null){
            disabledVal = System.getProperty("tamaya.disabled");
        }
        if(disabledVal!=null){
            this.disabled = Boolean.parseBoolean(disabledVal);
            if(this.disabled) {
                LOG.info("Tamaya Config is disabled by default. Add Tamaya-Enabled to your bundle manifests to enable it.");
            }else{
                LOG.info("Tamaya Config is enabled by default. Add Tamaya-Disabled to your bundle manifests to disable it.");
            }
        }
    }


    void setConfigValue(String key, Object value){
        Configuration config = null;
        try {
            config = cm.getConfiguration(COMPONENTID);
            Dictionary<String, Object> props = null;
            if (config != null
                    && config.getProperties() != null) {
                props = config.getProperties();
            } else {
                props = new Hashtable<String, Object>();
            }
            Object val = props.get(key);
            if(val==null) {
                props.put(key, value);
                config.update(props);
            }
            LOG.finest("Updated Tamaya Plugin config: "+key + "=" + value);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error writing Tamaya config.", e);
        }
    }

    Object getConfigValue(String key){
        Configuration config = null;
        try {
            config = cm.getConfiguration(COMPONENTID);
            Dictionary<String, Object> props = null;
            if (config != null
                    && config.getProperties() != null) {
                props = config.getProperties();
            }
            if(props!=null){
                return props.get(key);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error reading Tamaya config.", e);
        }
        return null;
    }

    public OperationMode getOperationMode(){
        return opMode;
    }

    public void setDefaultDisabled(boolean disabled){
        this.disabled = disabled;
        setConfigValue(TAMAYA_DISABLED, disabled);
    }

    public boolean isDefaultDisabled(){
        return disabled;
    }

    public void setOperationMode(OperationMode mode){
        this.opMode = Objects.requireNonNull(mode);
        setConfigValue(OperationMode.class.getSimpleName(), opMode.toString());
    }

    public void modifyConfiguration(String target, org.apache.tamaya.Configuration config, Dictionary<String, Object> dictionary) {
        LOG.info(() -> "Updating configuration for " + target + "...");
        dictionary.put("tamaya.opMode", getOperationMode().toString());
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
            switch (opMode) {
                case OVERRIDE:
                case EXTEND_AND_OVERRIDE:
                    if (configuredValue != null) {
                        LOG.info(() -> "Setting key " + dictEntry.getKey() + " to " + configuredValue);
                        dictionary.put(dictEntry.getKey(), configuredValue);
                    }
                    break;
                case SYNCH:
                    if (configuredValue != null) {
                        LOG.info(() -> "Setting key " + dictEntry.getKey() + " to " + configuredValue);
                        dictionary.put(dictEntry.getKey(), configuredValue);
                    } else {
                        LOG.info(() -> "Removing key " + dictEntry.getKey());
                        dictionary.remove(dictEntry.getKey());
                    }
            }
        }
        for (Map.Entry<String, String> configEntry : config.getProperties().entrySet()) {
            Object dictValue = dictionary.get(configEntry.getKey());
            switch (opMode) {
                case EXTEND:
                case EXTEND_AND_OVERRIDE:
                    LOG.info(() -> "Setting key " + configEntry.getKey() + " to " + configEntry.getValue());
                    dictionary.put(configEntry.getKey(), configEntry.getValue());
                    break;
                case SYNCH:
                    if (dictValue != null) {
                        LOG.info(() -> "Setting key " + configEntry.getKey() + " to " + configEntry.getValue());
                        dictionary.put(configEntry.getKey(), configEntry.getValue());
                    } else {
                        LOG.info(() -> "Removing key " + configEntry.getKey());
                        dictionary.remove(configEntry.getKey());
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


}
