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
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;

@Command(scope = "tamaya", name = "cm-config", description="Show the current Tamaya configuration.")
@Service
public class EvaluateCMConfigCommand implements Action{

    @Argument(index = 0, name = "pid", description = "The component's PID.",
            required = true, multiValued = false)
    String pid = null;

    @Argument(index = 1, name = "location", description = "The component's configuration location.",
            required = false, multiValued = false)
    String location = null;

    @Reference
    private ConfigurationAdmin cm;

    public Object execute() throws IOException {
        Configuration config = cm.getConfiguration(pid, location);
        System.out.println("OSGI Configuration for PID: " + pid);
        System.out.println("----------------------------------------------------------");
        if(config!=null){
            System.out.println("PID: " + config.getPid());
            System.out.println("Factory-PID: " + config.getFactoryPid());
            System.out.println("Location: " + config.getBundleLocation());
            System.out.println("Change Count: " + config.getChangeCount());
            System.out.println("Properties:");
            System.out.println(config.getProperties());
        }else{
            System.out.println("No OSGI Config found for PID: " + config.getPid());
        }
        return null;
    }

}