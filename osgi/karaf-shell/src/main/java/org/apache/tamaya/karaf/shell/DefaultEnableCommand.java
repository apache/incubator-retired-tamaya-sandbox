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
package org.apache.tamaya.karaf.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.tamaya.osgi.Policy;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.apache.tamaya.osgi.commands.ConfigCommands;

import java.io.IOException;
import java.util.List;

@Command(scope = "tamaya", name = "tm_enable", description="Enables or disable Tamaya by default for all bundles/services (default: enabled=false)." +
        " Disabling still allows to explicitly enable bundles using 'tamaya-enable' manifest or OSGI config entries.")
@Service
public class DefaultEnableCommand implements Action{

    @Reference
    private TamayaConfigPlugin configPlugin;

    @Argument(index = 0, name = "enabled", description = "The boolean value to enabled/disable Tamaya by default.",
            required = true, multiValued = false)
    boolean enabled;

    @Override
    public Object execute() throws IOException {
        return(ConfigCommands.setDefaultEnabled(configPlugin, enabled));
    }

    @Service
    public static final class OperationModeCompleter implements Completer {

        @Override
        public int complete(Session session, CommandLine commandLine, List<String> candidates) {
            StringsCompleter delegate = new StringsCompleter();
            for(Policy mode: Policy.values()) {
                delegate.getStrings().add(mode.toString());
            }
            return delegate.complete(session, commandLine, candidates);
        }
    }

}