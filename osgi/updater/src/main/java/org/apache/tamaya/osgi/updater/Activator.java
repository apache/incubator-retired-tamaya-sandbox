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

import org.apache.tamaya.events.ConfigEventManager;
import org.apache.tamaya.events.ConfigurationChange;
import org.apache.tamaya.osgi.commands.TamayaConfigService;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Activator that registers the Tamaya based Service Class for {@link ConfigurationAdmin},
 * using a default service priority of {@code 0}. This behaviour is configurable based on OSGI properties:
 * <ul>
 *     <li><p><b>org.tamaya.integration.osgi.cm.ranking, type: int</b> allows to configure the OSGI service ranking for
 *     Tamaya based ConfigurationAdmin instance. The default ranking used is 10.</p></li>
 *     <li><p><b>org.tamaya.integration.osgi.cm.override, type: boolean</b> allows to configure if Tamaya should
 *     register its ConfigAdmin service. Default is true.</p></li>
 * </ul>
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    private EventListener listener;

    private static final long DELAY = 5000L;
    private static final long PERIOD = 5000L;
    private Timer updateTimer = new Timer("Tamaya OSGI update monitor.", true);

    @Override
    public void start(BundleContext context) throws Exception {
        listener = new EventListener(context);
        ConfigEventManager.addListener(listener, ConfigurationChange.class);
        LOG.info("Registered Tamaya getConfig trigger for OSGI.");
        ServiceReference<TamayaConfigService> pluginRef = context.getServiceReference(TamayaConfigService.class);
        TamayaConfigService tamayaPlugin = context.getService(pluginRef);
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ConfigEventManager.enableChangeMonitoring(tamayaPlugin.isAutoUpdateEnabled());
            }
        }, DELAY, PERIOD);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        updateTimer.cancel();
        if (listener != null) {
            ConfigEventManager.removeListener(this.listener, ConfigurationChange.class);
            LOG.info("Unregistered Tamaya getConfig trigger for OSGI.");
            ConfigEventManager.enableChangeMonitoring(false);
        }
    }

}
