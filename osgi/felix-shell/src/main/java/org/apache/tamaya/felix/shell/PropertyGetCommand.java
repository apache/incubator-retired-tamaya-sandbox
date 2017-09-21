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
package org.apache.tamaya.felix.shell;

import org.apache.felix.scr.annotations.Service;
import org.apache.tamaya.osgi.commands.ConfigCommands;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

@Component(
        immediate = true,
        property = {
                "osgi.command.scope=tamaya:property",
                "osgi.command.function=property"
        },
        service=PropertyGetCommand.class
)
@Service
public class PropertyGetCommand{

    public String property(String propertysource, String key, boolean extended) throws IOException {
        return ConfigCommands.getProperty(propertysource, key, extended);
    }

    public String property(String propertysource, String key) throws IOException {
        return property(propertysource, key, false);
    }

    public String property(String key) throws IOException {
        return property(null, key, false);
    }

}