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
package org.apache.tamaya.osgi.updater;

import org.apache.tamaya.events.ConfigEvent;
import org.apache.tamaya.events.ConfigEventListener;
import org.apache.tamaya.events.ConfigurationChange;
import org.apache.tamaya.osgi.Policy;
import org.apache.tamaya.osgi.commands.TamayaConfigService;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.logging.Logger;

/**
 * Tamaya plugin that updates/extends the component configurations managed
 * by {@link ConfigurationAdmin}, based on the configured {@link Policy}.
 */
final class EventListener implements ConfigEventListener{
    private static final Logger LOG = Logger.getLogger(EventListener.class.getName());
    private BundleContext context;

    public EventListener(BundleContext context){
        this.context = context;
    }


    @Override
    public void onConfigEvent(ConfigEvent<?> event) {
        LOG.finest("Tamya Config change triggered: " + event);
        Set<String> changedPids = new HashSet<>();
        ConfigurationChange cc = (ConfigurationChange)event;
        for(PropertyChangeEvent evt: cc.getChanges()){
            String key = evt.getPropertyName();
            String pid = getPid(key);
            if(pid!=null){
                changedPids.add(pid);
            }
        }
        if(changedPids.isEmpty()){
            LOG.finest("Tamya Config change not targeting OSGI.");
            return;
        }
        LOG.finest("Tamya Config change for pids: " + changedPids);
        // Reload the given pids
        ServiceReference<TamayaConfigService> pluginRef = context.getServiceReference(TamayaConfigService.class);
        TamayaConfigService tamayaPlugin = context.getService(pluginRef);
        for(String pid:changedPids) {
            tamayaPlugin.updateConfig(pid);
        }
    }

    private String getPid(String key) {
        int index0 = key.indexOf("[");
        int index1 = key.indexOf("]");
        if(index0 >=0 && (index1 - index0) > 0){
            return key.substring(index0+1,index1);
        }
        return null;
    }
}
