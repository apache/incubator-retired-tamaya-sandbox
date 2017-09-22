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

import java.io.IOException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.apache.tamaya.osgi.commands.ConfigCommands;
import org.apache.tamaya.osgi.commands.HistoryCommands;

@Command(scope = "tamaya", name = "tm_info", description="Show he current Tamaya status.")
@Service
public class InfoCommand  implements Action {

    @Reference
    private TamayaConfigPlugin configPlugin;

    public Object execute() throws IOException {
        System.out.println(ConfigCommands.getInfo(configPlugin));
        return null;
    }

}