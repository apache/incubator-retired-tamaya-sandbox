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
package org.apache.tamaya.osgi.injection;

import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Class that monitors services and configures them if they have {@code Tamaya-Config = true} in the
 * service settings.
 */
public class TamayaOSGIInjector {

    public static final String TAMAYA_INJECTION_ENABLED_MANIFEST = "Tamaya-Config-Inject";
    public static final String TAMAYA_INJECTION_ENABLED_PROP = "tamaya-config-inject";
    private static final Logger log = Logger.getLogger(TamayaConfigPlugin.class.getName());
    private BundleContext context;
    private ConfigurationAdmin cm;
    private ServiceTracker<Object, Object> tracker;
    private static final Map<String, OSGIConfigurationInjector> INJECTORS = new ConcurrentHashMap<>();

    public TamayaOSGIInjector(BundleContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public void start(){
        tracker = new ServiceTracker<Object, Object>(context, Object.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                log.info("Checking service for configuration: " + reference);
                Object service =  super.addingService(reference);
                if(isInjectionEnabled(reference)) {
                    String pid = (String)reference.getProperty(Constants.SERVICE_PID);
                    if(pid==null){
                        pid = reference.getBundle().getSymbolicName();
                    }
                    OSGIConfigurationInjector injector = getInjector(pid, reference.getBundle().getLocation());
                    injector.configure(service);
                }
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<Object> reference, Object service) {
                super.modifiedService(reference, service);
                if(isInjectionEnabled(reference)) {
                    String pid = (String)reference.getProperty(Constants.SERVICE_PID);
                    if(pid==null){
                        pid = reference.getBundle().getSymbolicName();
                    }
                    OSGIConfigurationInjector injector = getInjector(pid, reference.getBundle().getLocation());
                    injector.configure(service);
                }
            }
        };
        tracker.open(true);
    }

    public void stop(){
        if(tracker!=null){
            tracker.close();
            tracker = null;
        }
    }

    private OSGIConfigurationInjector getInjector(String pid, String location){
        String key = location==null?pid.trim():pid.trim()+"::"+location.trim();
        OSGIConfigurationInjector injector = INJECTORS.get(key);
        if(injector==null){
            injector = new OSGIConfigurationInjector(cm, pid, location);
            INJECTORS.put(key, injector);
        }
        return injector;
    }

    public static boolean isInjectionEnabled(ServiceReference reference){
        String enabledVal = (String)reference.getProperty(TAMAYA_INJECTION_ENABLED_PROP);
        if(enabledVal!=null){
            return Boolean.parseBoolean(enabledVal);
        }
        enabledVal = reference.getBundle().getHeaders().get(TAMAYA_INJECTION_ENABLED_MANIFEST);
        if(enabledVal!=null){
            return Boolean.parseBoolean(enabledVal);
        }
        return false;
    }
}
