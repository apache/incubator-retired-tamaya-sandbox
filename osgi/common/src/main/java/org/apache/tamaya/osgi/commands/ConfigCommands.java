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
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.osgi.OperationMode;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class implementing the available configuration related commands.
 */
public final class ConfigCommands {

    /** Singleton constructor. */
    private ConfigCommands(){}

    public static String getInfo(TamayaConfigPlugin configPlugin) throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        return config.toString() + "\n\n"
                + StringUtil.format("Default OperationMode:", 30) + configPlugin.getDefaultOperationMode() + '\n'
                + StringUtil.format("Default Disabled: ", 30) + configPlugin.isDefaultDisabled();
    }

    public static String readConfig(String section) {
        Configuration config = ConfigurationProvider.getConfiguration();
        if(section!=null){
            return config
                    .with(ConfigurationFunctions.section(section))
                    .query(ConfigurationFunctions.textInfo());
        }
        return config.query(ConfigurationFunctions.textInfo());
    }

    public static String readConfig(TamayaConfigPlugin configPlugin, String pid, String section) {
        Configuration config = null;
        if(pid!=null){
            config = configPlugin.getTamayaConfiguration(pid);
            if(config==null){
                return "No Tamaya Config found for PID: " + pid;
            }
        }else {
            config = ConfigurationProvider.getConfiguration();
        }
        if(section!=null){
            return config
                    .with(ConfigurationFunctions.section(section))
                    .query(ConfigurationFunctions.textInfo());
        }
        return config.query(ConfigurationFunctions.textInfo());
    }

    public static String getDefaultOpPolicy(TamayaConfigPlugin configPlugin) throws IOException {
        return String.valueOf(configPlugin.getDefaultOperationMode());
    }

    public static String setDefaultOpPolicy(TamayaConfigPlugin configPlugin, String policy) throws IOException {
        OperationMode opMode = OperationMode.valueOf(policy);
        configPlugin.setDefaultOperationMode(opMode);
        return "OperationMode="+opMode.toString();
    }

    public static String getProperty(String propertysource, String key, boolean extended) throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        if(propertysource!=null){
            PropertySource ps = config.getContext().getPropertySource(propertysource);
            if(ps==null){
                return "ERR: No such propertysource: " + propertysource;
            }else {
                PropertyValue val = ps.get(key);
                if(val==null){
                    return "ERR: PropertySource: " + propertysource + " - undefined key: " + key;
                }else {
                    if(extended) {
                        return StringUtil.format("PropertySource", 25) + StringUtil.format("Value", 25) + '\n' +
                                StringUtil.format(propertysource, 25) + StringUtil.format(val.getValue(), 55);
                    }else{
                        return val.getValue();
                    }
                }
            }
        }else{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(StringUtil.format("PropertySource", 25) + StringUtil.format("Value", 25));
            for(PropertySource ps:config.getContext().getPropertySources()){
                PropertyValue val = ps.get(key);
                if(val!=null){
                    if(extended) {
                        pw.println(StringUtil.format(propertysource, 25) + StringUtil.format(val.toString(), 55));
                    }else{
                        pw.println(StringUtil.format(propertysource, 25) + StringUtil.format(val.getValue(), 55));
                    }
                }
            }
            pw.flush();
            return sw.toString();
        }
    }

    public static String getPropertySource(String propertysource) throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        if(propertysource!=null){
            PropertySource ps = config.getContext().getPropertySource(propertysource);
            if(ps==null){
                return "No such propertysource: " + propertysource;
            }else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println(StringUtil.format("ID:", 20) + ps.getName());
                pw.println(StringUtil.format("Ordinal:", 20) + ps.getOrdinal());
                pw.println(StringUtil.format("Class:", 20) + ps.getClass().getName());
                pw.println("Properties:");
                pw.print(StringUtil.format("  Key", 20));
                pw.print(StringUtil.format("Value", 20));
                pw.print(StringUtil.format("Source", 20));
                pw.println(StringUtil.format("Meta-Entries", 20));
                pw.println(StringUtil.printRepeat("-", 80));
                for(PropertyValue pv:ps.getProperties().values()) {
                    pw.print("  " + StringUtil.format(pv.getKey(), 20));
                    pw.print(StringUtil.format(pv.getValue(), 20));
                    pw.print(StringUtil.format(pv.getSource(), 20));
                    pw.println(StringUtil.format(pv.getMetaEntries().toString(), 80));
                }
                pw.flush();
                return sw.toString();
            }
        }
        // Get a name of existing propertysources
        List<String> result = new ArrayList<>();
        for(PropertySource ps:config.getContext().getPropertySources()){
            result.add(ps.getName());
        }
        StringBuilder b = new StringBuilder("Please select a property source:\n");
        for(String name:result){
            b.append(name).append('\n');
        }
        return b.toString();
    }

    public static String getPropertySourceOverview() throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(StringUtil.format("ID", 20));
        pw.print(StringUtil.format("Ordinal", 20));
        pw.print(StringUtil.format("Class", 40));
        pw.println(StringUtil.format("Property Count", 5));
        pw.println(StringUtil.printRepeat("-", 80));
        for(PropertySource ps:config.getContext().getPropertySources()){
            pw.print(StringUtil.format(ps.getName(), 20));
            pw.print(StringUtil.format(String.valueOf(ps.getOrdinal()), 20));
            pw.print(StringUtil.format(ps.getClass().getName(), 40));
            pw.println(StringUtil.format(String.valueOf(ps.getProperties().size()), 5));
            pw.println("---");
        }
        pw.flush();
        return sw.toString();
    }

    public static String setDefaultDisabled(TamayaConfigPlugin configPlugin, boolean disabled) throws IOException {
        configPlugin.setDefaultDisabled(disabled);
        return disabled?"Tamaya is disabled by default.":"Tamaya is enabled by default.";
    }

    public static String setAutoUpdateEnabled(TamayaConfigPlugin configPlugin, boolean enabled) {
        configPlugin.setAutoUpdateEnabled(enabled);
        return "tamaya.autoUpdate="+enabled;
    }
}