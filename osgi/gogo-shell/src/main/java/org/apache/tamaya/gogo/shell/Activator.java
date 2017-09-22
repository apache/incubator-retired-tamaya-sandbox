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
package org.apache.tamaya.gogo.shell;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * Activator that registers the Tamaya commands for the Felix Gogo console used
 * in Apache Felix and Equinox.
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    private ServiceRegistration<HistoryCommands> histReg;
    private ServiceRegistration<ConfigCommands> configReg;
    private ServiceRegistration<BackupCommands> backupReg;
    private ServiceRegistration<SettingsCommands> settingsReg;

    @Override
    public void start(BundleContext context) throws Exception {
        LOG.finest("Registering Tamaya commands...");
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("osgi.command.scope", "tamaya");
        props.put("osgi.command.function",
                new String[] {"config", "property_get",
                        "propertysource_get","propertysources_get"});
        configReg = context.registerService(
                ConfigCommands.class,
                new ConfigCommands(context), props);
        props.put("osgi.command.function",
                new String[] {"history_get","history_delete",
                "maxhistory_get", "maxhistory_set"});
        histReg = context.registerService(
                HistoryCommands.class,
                new HistoryCommands(context), props);
        props.put("osgi.command.function",
                new String[] {"backup_create","backup_delete",
                "backup_get"});
        backupReg = context.registerService(
                BackupCommands.class,
                new BackupCommands(context), props);
        props.put("osgi.command.function",
                new String[] {"tamaya_disable","tamaya_policy_get",
                "tamaya_policy_set","tamaya_info"});
        settingsReg = context.registerService(
                SettingsCommands.class,
                new SettingsCommands(context), props);
        LOG.info("Registered Tamaya commands.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOG.info("Unregistering Tamaya commands.");
        if (histReg != null) {
            histReg.unregister();
        }
        if (configReg != null) {
            configReg.unregister();
        }
        if (backupReg != null) {
            backupReg.unregister();
        }
        if (settingsReg != null) {
            settingsReg.unregister();
        }
    }

}
