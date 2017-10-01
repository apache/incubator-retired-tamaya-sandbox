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

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tamaya plugin that updates/extends the component configurations managed
 * by {@link ConfigurationAdmin}, based on the configured {@link OperationMode}.
 */
public class TamayaConfigPlugin implements BundleListener, ServiceListener{
    static final String COMPONENTID = "TamayaConfigPlugin";
    /** the logger. */
    private static final Logger LOG = Logger.getLogger(TamayaConfigPlugin.class.getName());
    public static final String TAMAYA_ENABLED = "tamaya-enabled";
    public static final String TAMAYA_AUTO_UPDATE_ENABLED = "tamaya.autoUpdateEnabled";
    public static final String TAMAYA_ROOT_KEY = "tamaya-root";
    private boolean enabledByDefault = false;
    private OperationMode defaultOpMode = OperationMode.OVERRIDE;

    private ConfigChanger configChanger;
    private boolean autoUpdateEnabled;

    @Override
    public void serviceChanged(ServiceEvent event) {
        switch(event.getType()){
            case ServiceEvent.REGISTERED:
            case ServiceEvent.MODIFIED:
                configureService(event);
                break;
            case ServiceEvent.UNREGISTERING:
                // unconfigure...? Corrently nothing here.
                break;
        }
    }


    /**
     * Create a new getConfig.
     * @param context the OSGI context
     */
    public TamayaConfigPlugin(BundleContext context) {
        configChanger = new ConfigChanger(context);
        Dictionary<String,Object> props = getPluginConfig();
        Backups.restore(props);
        ConfigHistory.restore(props);
        initDefaultEnabled(props);
        initAutoUpdateEnabled(props);
        initDefaultOpMode(props);
        initConfigs();
    }

    public void setAutoUpdateEnabled(boolean enabled){
        this.autoUpdateEnabled = enabled;
        setConfigValue(TAMAYA_AUTO_UPDATE_ENABLED, enabled);
    }

    public void setTamayaEnabledByDefault(boolean enabledByDefault){
        this.enabledByDefault = enabledByDefault;
        setConfigValue(TAMAYA_ENABLED, enabledByDefault);
    }

    public boolean isTamayaEnabledByDefault(){
        return enabledByDefault;
    }

    public OperationMode getDefaultOperationMode(){
        return defaultOpMode;
    }

    public void setDefaultOperationMode(OperationMode mode){
        this.defaultOpMode = Objects.requireNonNull(mode);
        setConfigValue(OperationMode.class.getSimpleName(), defaultOpMode.toString());
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        switch(event.getType()){
            case BundleEvent.STARTING:
            case BundleEvent.LAZY_ACTIVATION:
                configureBundle(event.getBundle());
                break;
        }
    }

    private void initConfigs() {
        // Check for matching bundles already installed...
        for(Bundle bundle:configChanger.getContext().getBundles()){
            switch(bundle.getState()){
                case Bundle.ACTIVE:
                    configureBundle(bundle);
                    break;
            }
        }
    }

    private void configureService(ServiceEvent event) {
        // Optional MANIFEST entries
        Bundle bundle = event.getServiceReference().getBundle();
        if(!isBundleEnabled(bundle)){
            return;
        }
        String pid = (String)event.getServiceReference().getProperty(Constants.SERVICE_PID);
        if(pid==null){
            LOG.finest("No service pid for: " + event.getServiceReference());
            return;
        }
        configChanger.configure(pid, event.getServiceReference().getBundle(), defaultOpMode, false, false);
        Dictionary<String,Object> props = getPluginConfig();
        Backups.save(props);
        ConfigHistory.save(props);
        setPluginConfig(props);
    }

    public Dictionary<String,Object> updateConfig(String pid) {
        return updateConfig(pid, defaultOpMode, false, false);
    }

    public Dictionary<String,Object> updateConfig(String pid, boolean dryRun) {
        return updateConfig(pid, defaultOpMode, false, dryRun);
    }

    public Dictionary<String,Object> updateConfig(String pid, OperationMode opMode, boolean explicitMode, boolean dryRun) {
        if(dryRun){
            return configChanger.configure(pid, null, opMode, explicitMode, true);
        }else {
            LOG.fine("Updating getConfig for pid...: " + pid);
            Dictionary<String,Object> result = configChanger.configure(pid, null, opMode, explicitMode, false);
            Dictionary<String,Object> props = getPluginConfig();
            Backups.save(props);
            ConfigHistory.save(props);
            setPluginConfig(props);
            return result;
        }
    }

    private void configureBundle(Bundle bundle) {
        if(!isBundleEnabled(bundle)){
            return;
        }
        String tamayaPid = bundle.getHeaders().get(TAMAYA_ROOT_KEY);
        String pid = tamayaPid!=null?tamayaPid:bundle.getSymbolicName();
        if(pid==null){
            pid = bundle.getLocation();
        }
        if(pid==null){
            LOG.finest(() -> "No PID/location for bundle " + bundle.getSymbolicName() + '('+bundle.getBundleId()+')');
            return;
        }
        configChanger.configure(pid, bundle, defaultOpMode, false, false);
        Dictionary<String,Object> props = getPluginConfig();
        Backups.save(props);
        ConfigHistory.save(props);
        setPluginConfig(props);
    }

    public boolean isBundleEnabled(Bundle bundle){
        // Optional MANIFEST entries
        String bundleEnabledVal = bundle.getHeaders().get(TAMAYA_ENABLED);
        if(bundleEnabledVal==null && !enabledByDefault){
            LOG.finest("tamaya.enabled=false: not configuring bundle: " + bundle.getSymbolicName());
            return false;
        }
        if(bundleEnabledVal != null && !Boolean.parseBoolean(bundleEnabledVal)){
            LOG.finest("Bundle is explcitly disabled for Tamaya: " + bundle.getSymbolicName());
            return false;
        }
        if(bundleEnabledVal != null && Boolean.parseBoolean(bundleEnabledVal)){
            LOG.finest("Bundle is explicitly enabled for Tamaya: " + bundle.getSymbolicName());
            return true;
        }
        return true;
    }

    private void initAutoUpdateEnabled(Dictionary<String,Object> props) {
        Object enabledVal = props.get(TAMAYA_AUTO_UPDATE_ENABLED);
        if(enabledVal!=null){
            this.autoUpdateEnabled = Boolean.parseBoolean(enabledVal.toString());
        }
        if(this.autoUpdateEnabled) {
            LOG.info("Tamaya Automatic Config Updating is enabled.");
        }else{
            LOG.info("Tamaya Automatic Config Updating is enabledByDefault.");
        }
    }

    private void initDefaultEnabled(Dictionary<String,Object> props) {
        Object disabledVal = props.get(TAMAYA_ENABLED);
        if(disabledVal==null && System.getProperty(TAMAYA_ENABLED)!=null){
            disabledVal = Boolean.parseBoolean(System.getProperty(TAMAYA_ENABLED));
        }
        if(disabledVal!=null){
            this.enabledByDefault = Boolean.parseBoolean(disabledVal.toString());
        }
        if(this.enabledByDefault) {
            LOG.info("Tamaya Config is enabledByDefault by default. Add Tamaya-Enabled to your bundle manifests to enable it.");
        }else{
            LOG.info("Tamaya Config is enabled by default. Add Tamaya-Disabled to your bundle manifests to disable it.");
        }
    }

    private void initDefaultOpMode(Dictionary<String,Object> props) {
        String opVal = (String)props.get(OperationMode.class.getSimpleName());
        if(opVal!=null){
            try{
                defaultOpMode = OperationMode.valueOf(opVal);
            }catch(Exception e){
                LOG.warning("Invalid OperationMode: " + opVal +", using default: " + defaultOpMode);
            }
        }
    }

    Dictionary<String, Object> getPluginConfig(){
        Configuration config = null;
        try {
            config = configChanger.getConfigurationAdmin().getConfiguration(COMPONENTID);
            Dictionary<String, Object> props = null;
            if (config != null
                    && config.getProperties() != null) {
                props = config.getProperties();
            } else {
                props = new Hashtable<String, Object>();
            }
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("No Tamaya plugin config.", e);
        }
    }

    void setPluginConfig(Dictionary<String, Object> props){
        Configuration config = null;
        try {
            config = configChanger.getConfigurationAdmin().getConfiguration(COMPONENTID);
            if (config != null) {
                config.update(props);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to write Tamaya plugin config.", e);
        }
    }

    void setConfigValue(String key, Object value){
        try {
            Dictionary<String, Object> props = getPluginConfig();
            if(props!=null) {
                props.put(key, value);
                setPluginConfig(props);
                LOG.finest("Updated Tamaya Plugin value: " + key + "=" + value);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error writing Tamaya config value: " + key, e);
        }
    }

    Object getConfigValue(String key){
        try {
            Dictionary<String, Object> props = getPluginConfig();
            if(props!=null){
                return props.get(key);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error reading Tamaya config value.", e);
        }
        return null;
    }


    public org.apache.tamaya.Configuration getTamayaConfiguration(String root) {
        return configChanger.getTamayaConfiguration(root);
    }

    public boolean isAutoUpdateEnabled() {
        return this.autoUpdateEnabled;
    }

    public boolean restoreBackup(String pid)throws IOException{
        Dictionary<String,Object> config = (Dictionary<String,Object>) Backups.get(pid);
        if(config==null){
            return false;
        }
        this.configChanger.restoreBackup(pid, config);
        return true;
    }

    public Dictionary<String, Object> getOSGIConfiguration(String pid, String section) {
        try {
            Configuration config = configChanger.getConfigurationAdmin().getConfiguration(pid);
            Dictionary<String, Object> props = null;
            if (config == null
                    || config.getProperties() == null) {
                return null;
            }
            props = config.getProperties();
            if(section!=null){
                return filter(props, section);
            }
            return props;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error reading OSGI config for PID: " + pid, e);
            return null;
        }
    }

    private Dictionary<String, Object> filter(Dictionary<String, Object> props, String section) {
        Hashtable<String, Object> result = new Hashtable<>();
        Enumeration<String> keys = props.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            if(key.startsWith(section)){
                result.put(key, props.get(key));
            }
        }
        return result;
    }

}
