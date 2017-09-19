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
import java.util.*;
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
    private static final String TAMAYA_DISABLED = "tamaya.disabled";
    private boolean disabled = false;
    private OperationMode defaultOpMode = OperationMode.OVERRIDE;

    private ConfigChanger configChanger;

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
     * Create a new config.
     * @param context the OSGI context
     */
    TamayaConfigPlugin(BundleContext context) {
        configChanger = new ConfigChanger(context);
        initDefaultEnabled();
        initDefaultOpMode();
        initConfigs();
    }

    public void setDefaultDisabled(boolean disabled){
        this.disabled = disabled;
        setConfigValue(TAMAYA_DISABLED, disabled);
    }

    public boolean isDefaultDisabled(){
        return disabled;
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
//            case BundleEvent.UPDATED:
//              TODO add checks for preventing endlee loop for updates here...
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
        String pid = (String)event.getServiceReference().getProperty("service.pid");
        if(pid==null){
            LOG.finest("No service pid for: " + event.getServiceReference());
            return;
        }
        configChanger.configure(pid, event.getServiceReference().getBundle(), defaultOpMode);
    }

    private void configureBundle(Bundle bundle) {
        if(!isBundleEnabled(bundle)){
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
        configChanger.configure(pid, bundle, defaultOpMode);
    }


    public boolean isBundleEnabled(Bundle bundle){
        // Optional MANIFEST entries
        String enabledTamaya = bundle.getHeaders().get("Tamaya-Enabled");
        String disabledTamaya = bundle.getHeaders().get("Tamaya-Disabled");

        if(Boolean.parseBoolean(disabledTamaya)){
            LOG.finest("Bundle is disabled for Tamaya: " + bundle.getSymbolicName());
            return false;
        }
        if(enabledTamaya != null && !Boolean.parseBoolean(enabledTamaya)){
            LOG.finest("Bundle is disabled for Tamaya: " + bundle.getSymbolicName());
            return false;
        }
        if(disabled){
            LOG.finest("tamaya.disabled=false: not configuring bundle: " + bundle.getSymbolicName());
            return false;
        }
        return true;
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

    private void initDefaultOpMode() {
        String opVal = (String)getConfigValue(OperationMode.class.getName());
        if(opVal!=null){
            try{
                defaultOpMode = OperationMode.valueOf(opVal);
            }catch(Exception e){
                LOG.warning("Invalid OperationMode: " + opVal +", using default: " + defaultOpMode);
            }
        }
    }


    void setConfigValue(String key, Object value){
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
            config = configChanger.getConfigurationAdmin().getConfiguration(COMPONENTID);
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

}
