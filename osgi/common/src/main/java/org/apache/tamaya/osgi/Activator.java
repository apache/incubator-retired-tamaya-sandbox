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
import org.osgi.service.component.annotations.Reference;

import java.util.Dictionary;
import java.util.Hashtable;
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

    private static final Integer DEFAULT_RANKING = 100000;

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    private ServiceRegistration<?> registration;


    @Override
    public void start(BundleContext context) throws Exception {
        ServiceReference<ConfigurationAdmin> cmRef = context.getServiceReference(ConfigurationAdmin.class);
        ConfigurationAdmin cm = context.getService(cmRef);
        Configuration configuration = cm.getConfiguration("tamaya-osgi", null);
        Dictionary<String, Object> props = null;
        if (configuration != null
                && configuration.getProperties() != null) {
            props = configuration.getProperties();
        } else {
            props = new Hashtable<>();
        }
        String ranking = context.getProperty(Constants.SERVICE_RANKING);
        if (ranking == null) {
            ranking = System.getProperty(Constants.SERVICE_RANKING);
        }
        if (ranking == null) {
            ranking = DEFAULT_RANKING.toString();
            LOG.fine("Using default ranking for Tamaya OSGI Config plugin: " + DEFAULT_RANKING);
        } else {
            ranking = Integer.valueOf(ranking).toString();
            LOG.fine("Using custom ranking for Tamaya OSGI Config plugin: " + ranking);
        }
        props.put(Constants.SERVICE_RANKING, DEFAULT_RANKING);
        TamayaConfigPlugin plugin = new TamayaConfigPlugin(context);
        LOG.info("Registering Tamaya OSGI Config plugin with ranking: " + ranking);
        registration = context.registerService(
                TamayaConfigPlugin.class,
                plugin, props);
        LOG.info("Registered Tamaya OSGI Config plugin.");
        configuration.update(props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
    }

}
