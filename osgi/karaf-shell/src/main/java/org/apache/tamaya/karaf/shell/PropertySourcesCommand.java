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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;

@Command(scope = "tamaya", name = "propertysources", description="Get a list of currently registered propertysources.")
@Service
public class PropertySourcesCommand implements Action{

    public Object execute() throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        System.out.print(StringUtil.format("ID", 20));
        System.out.print(StringUtil.format("Ordinal", 20));
        System.out.print(StringUtil.format("Class", 40));
        System.out.println(StringUtil.format("Property Count", 5));
        System.out.println(StringUtil.printRepeat("-", 80));
        for(PropertySource ps:config.getContext().getPropertySources()){
            System.out.print(StringUtil.format(ps.getName(), 20));
            System.out.print(StringUtil.format(String.valueOf(ps.getOrdinal()), 20));
            System.out.print(StringUtil.format(ps.getClass().getName(), 40));
            System.out.println(StringUtil.format(String.valueOf(ps.getProperties().size()), 5));
            System.out.println("---");
        }
        return null;
    }

}