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
import org.apache.tamaya.osgi.Policy;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Utility class implementing the available configuration related commands.
 */
public final class ConfigCommands {

    /** Singleton constructor. */
    private ConfigCommands(){}

    public static String getInfo(TamayaConfigPlugin configPlugin) throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        return config.toString() + "\n\n"
                + StringUtil.format("Default Policy:", 30) + configPlugin.getDefaultOperationMode() + '\n'
                + StringUtil.format("Default Enabled: ", 30) + configPlugin.isTamayaEnabledByDefault();
    }

    public static String readTamayaConfig(String section, String filter) {
        Configuration config = ConfigurationProvider.getConfiguration();
        if(section!=null){
            config = config
                    .with(ConfigurationFunctions.section(section, true));
        }
        if(filter!=null){
            config = config.with(ConfigurationFunctions.section(filter, false));
        }
        return "Tamaya Configuration\n" +
                "--------------------\n" +
                "Section:     "+section +"\n" +
                (filter!=null?"Filter:      "+filter + "\n":"") +
                config.query(ConfigurationFunctions.textInfo());
    }

    public static String readTamayaConfig4PID(String pid, String filter) {
        return readTamayaConfig("["+pid+"]", filter);
    }

    public static String applyTamayaConfiguration(TamayaConfigPlugin configPlugin, String pid, String operationMode, boolean dryRun){
        Dictionary<String,Object> config = null;
        if(operationMode!=null){
            config = configPlugin.updateConfig(pid, Policy.valueOf(operationMode), true, dryRun);
            return  "Full configuration\n" +
                    "------------------\n" +
                    "PID           : " + pid + "\n" +
                    "Policy : "+ operationMode + "\n" +
                    "Applied       : " + !dryRun + "\n" +
                    printOSGIConfig(pid, config);
        }else{
            config = configPlugin.updateConfig(pid, dryRun);
            return  "Full configuration\n" +
                    "------------------\n" +
                    "PID           : " + pid + "\n" +
                    "Policy : "+ configPlugin.getDefaultOperationMode() + "\n" +
                    "Applied       : " + !dryRun + "\n" +
                    printOSGIConfig(pid, config);
        }
    }

    public static String readOSGIConfiguration(TamayaConfigPlugin configPlugin, String pid, String section) {
        Dictionary<String,Object> config = configPlugin.getOSGIConfiguration(pid, section);
        return printOSGIConfig(pid, config);
    }

    private static String printOSGIConfig(String pid, Dictionary<String,Object> config){
        if(config.isEmpty()){
            return "No Config present for PID: " + pid;
        }
        StringBuilder b = new StringBuilder();
        b.append("OSGI Configuration for PID: ").append(pid).append('\n');
        b.append("-----------------------------------------------------\n");
        TreeMap<String,String> result = new TreeMap<>();
        Enumeration<String> keys = config.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            result.put(key, String.valueOf(config.get(key)));
        }
        for(Map.Entry<String,String> en:result.entrySet()){
            b.append(StringUtil.format(en.getKey(), 40));
            b.append(StringUtil.format(en.getValue(), 40));
            b.append('\n');
        }
        return b.toString();
    }

    public static String getDefaultOpPolicy(TamayaConfigPlugin configPlugin) throws IOException {
        return String.valueOf(configPlugin.getDefaultOperationMode());
    }

    public static String setDefaultOpPolicy(TamayaConfigPlugin configPlugin, String policy) throws IOException {
        Policy opMode = Policy.valueOf(policy);
        configPlugin.setDefaultOperationMode(opMode);
        return "Policy="+opMode.toString();
    }

    public static String getProperty(String propertysource, String key, boolean extended) throws IOException {
        Configuration config = ConfigurationProvider.getConfiguration();
        if(propertysource!=null){
            PropertySource ps = config.getContext().getPropertySource(propertysource);
            if(ps==null){
                return "ERR: No such Property Source: " + propertysource;
            }else {
                PropertyValue val = ps.get(key);
                if(val==null){
                    return "ERR: Property Source: " + propertysource + " - undefined key: " + key;
                }else {
                    if(extended) {
                        return StringUtil.format("Property Source", 25) + StringUtil.format("Value", 25) + '\n' +
                                StringUtil.format(propertysource, 25) + StringUtil.format(val.getValue(), 55);
                    }else{
                        return val.getValue();
                    }
                }
            }
        }else{
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(StringUtil.format("Property Source", 25) + StringUtil.format("Value", 25));
            for(PropertySource ps:config.getContext().getPropertySources()){
                PropertyValue val = ps.get(key);
                if(val!=null){
                    if(extended) {
                        pw.println(StringUtil.format("", 25) + StringUtil.format(val.toString(), 55));
                    }else{
                        pw.println(StringUtil.format("", 25) + StringUtil.format(val.getValue(), 55));
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
                return "No such Property Source: " + propertysource;
            }else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println("Property Source");
                pw.println("---------------");
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
        pw.println("Property Sources");
        pw.println("----------------");
        pw.print(StringUtil.format("ID", 30));
        pw.print(StringUtil.format("Ordinal", 20));
        pw.print(StringUtil.format("Class", 40));
        pw.println(StringUtil.format("Property Count", 5));
        pw.println(StringUtil.printRepeat("-", 80));
        for(PropertySource ps:config.getContext().getPropertySources()){
            pw.print(StringUtil.format(ps.getName(), 30));
            pw.print(StringUtil.format(String.valueOf(ps.getOrdinal()), 20));
            pw.print(StringUtil.format(ps.getClass().getName(), 40));
            pw.println(StringUtil.format(String.valueOf(ps.getProperties().size()), 5));
            pw.println("---");
        }
        pw.flush();
        return sw.toString();
    }

    public static String setDefaultEnabled(TamayaConfigPlugin configPlugin, boolean enabled) throws IOException {
        configPlugin.setTamayaEnabledByDefault(enabled);
        return TamayaConfigPlugin.TAMAYA_ENABLED_PROP+"="+enabled;
    }

    public static String getDefaultEnabled(TamayaConfigPlugin configPlugin) {
        return String.valueOf(configPlugin.isTamayaEnabledByDefault());
    }

    public static String setAutoUpdateEnabled(TamayaConfigPlugin configPlugin, boolean enabled) {
        configPlugin.setAutoUpdateEnabled(enabled);
        return TamayaConfigPlugin.TAMAYA_AUTO_UPDATE_ENABLED_PROP+"="+enabled;
    }

    public static String getAutoUpdateEnabled(TamayaConfigPlugin configPlugin) {
        return String.valueOf(configPlugin.isAutoUpdateEnabled());
    }
}