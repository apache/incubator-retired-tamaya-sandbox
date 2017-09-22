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

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Objects;

public class ConfigCommands {

    private BundleContext context;

    private <T> T getService(Class<T> type){
        ServiceReference<T> cmRef = context.getServiceReference(type);
        return context.getService(cmRef);
    }

    public ConfigCommands(BundleContext context){
        this.context = Objects.requireNonNull(context);
    }

    @Descriptor("Shows the current Tamaya configuration.")
    public void tm_config(@Parameter(absentValue = "", names={"-s", "--section"})
                       @Descriptor("The section start expression to filter.") String section,
                       @Parameter(absentValue = "", names={"-p", "--pid"})
                       @Descriptor("The pid to filter (required).") String pid) throws IOException {
        if(pid.isEmpty()){
            System.out.println(org.apache.tamaya.osgi.commands.ConfigCommands.readConfig(section));
        }else {
            System.out.println(org.apache.tamaya.osgi.commands.ConfigCommands.readConfig(getService(TamayaConfigPlugin.class), pid, section));
        }
    }


    @Descriptor("Gets the detailed property values.")
    public void tm_property(@Parameter(absentValue = "", names={"-ps", "--propertysource"})
                                 @Descriptor("The property source name")String propertysource,
                             @Parameter(absentValue = Parameter.UNSPECIFIED, names={"-k", "--key"})
                                @Descriptor("The property key")String key,
                             @Parameter(absentValue = "false", names={"-e", "--extended"})
                                 @Descriptor("Show extended info, default=false")Boolean extended) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.ConfigCommands.getProperty(propertysource, key, extended.booleanValue()));
    }

    @Descriptor("Get details of a property source.")
    public void tm_propertysource(@Parameter(absentValue = "", names={"-ps", "--propertysource"})
                                       @Descriptor("The property source name, empty returns a list of possible values")String propertysource) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.ConfigCommands.getPropertySource(propertysource));
    }

    @Descriptor("Show details of all registered property sources.")
    public void tm_propertysources() throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.ConfigCommands.getPropertySourceOverview());
    }
}