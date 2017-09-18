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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;

@Command(scope = "tamaya", name = "property", description="Show the current Tamaya configuration.")
@Service
public class PropertyCommand implements Action{

    @Argument(index = 0, name = "key", description = "The target property source id.",
            required = false, multiValued = false)
    String key = null;

    @Option(name = "propertysource", aliases = "ps", description = "The target property source id.",
            required = false, multiValued = false)
    String propertysource = null;

    public Object execute() throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        if(propertysource!=null){
            PropertySource ps = config.getContext().getPropertySource(propertysource);
            if(ps==null){
                System.out.println("No such propertysource: " + propertysource);
            }else {
                PropertyValue val = ps.get(key);
                if(val==null){
                    System.out.println("PropertySource: " + propertysource + "\nUndefined key: " + key);
                }else {
                    System.out.println("PropertySource: " + propertysource + "\n" +
                            ps.get(key));
                }
            }
        }else{
            for(PropertySource ps:config.getContext().getPropertySources()){
                PropertyValue val = ps.get(key);
                if(val==null){
                    System.out.println("PropertySource: " + propertysource + "\nUndefined key: " + key);
                }else {
                    System.out.println("PropertySource: " + propertysource + "\n" +
                            ps.get(key));
                }
            }
        }
        return null;
    }

}