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
import org.apache.tamaya.osgi.OperationMode;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.apache.tamaya.osgi.commands.ConfigCommands;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Objects;

public class SettingsCommands {

    private BundleContext context;

    public SettingsCommands(BundleContext context){
        this.context = Objects.requireNonNull(context);
    }

    private <T> T getService(Class<T> type){
        ServiceReference<T> cmRef = context.getServiceReference(type);
        return context.getService(cmRef);
    }

    @Descriptor("Allows to disable/enable Tamaya configuration by default.")
    public void tm_enable(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-e", "--enable"})
                                   @Descriptor("if true Tamaya is enabled by default (default=false)") boolean enabled) throws IOException {
        System.out.println(ConfigCommands.setDefaultEnabled(getService(TamayaConfigPlugin.class), enabled));
    }

    @Descriptor("Access if Tamaya is currently enabled by default to change OSGI configuration.")
    public void tm_enabled(){
        System.out.println(ConfigCommands.getDefaultEnabled(getService(TamayaConfigPlugin.class)));
    }

    @Descriptor("Get the default Tamaya configuration policy.")
    public void tm_policy() throws IOException {
        System.out.println(ConfigCommands.getDefaultOpPolicy(getService(TamayaConfigPlugin.class)));
    }

    @Descriptor("Set the default Tamaya configuration policy.")
    public void tm_policy_set(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-p", "--policy"})
                                      @Descriptor("The policy to apply (required), one of: EXTEND, OVERRIDE, UPDATE_ONLY") OperationMode policy) throws IOException {
        System.out.println(ConfigCommands.setDefaultOpPolicy(getService(TamayaConfigPlugin.class), policy.toString()));
    }

    @Descriptor("Get info about the current Tamaya configuration settings.")
    public void tm_info() throws IOException {
        System.out.println(ConfigCommands.getInfo(getService(TamayaConfigPlugin.class)));
    }

    @Descriptor("Flag if Tamaya is automatically triggering OSGI config updates, when according " +
            "Tamaya configuration changes.")
    public void tm_propagate_updates() throws IOException {
        System.out.println(getService(TamayaConfigPlugin.class).isAutoUpdateEnabled());
    }

    @Descriptor("Configure if Tamaya is automatically triggering OSGI config updates, when according " +
            "Tamaya configuration changes.")
    public void tm_propagate_updates_set(boolean enabled) throws IOException {
        System.out.println(ConfigCommands.setAutoUpdateEnabled(getService(TamayaConfigPlugin.class), enabled));
    }
}