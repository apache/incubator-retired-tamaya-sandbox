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

import org.apache.tamaya.osgi.Backups;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;


/**
 * Utility class implementing the available backup related commands.
 */
public final class BackupCommands {

    /** Singleton constructor. */
    private BackupCommands(){}

    public static String createBackup(ConfigurationAdmin cm, String pid, boolean force) throws IOException {
        Configuration cfg = cm.getConfiguration(pid);
        if(cfg!=null){
            Dictionary<String,?> props = cfg.getProperties();
            if(props!=null){
                if(force || !Backups.contains(pid)){
                    Backups.set(pid, props);
                    return "Backup created, PID = " + pid + '\n' +
                    printProps(props);
                }
            }
        }
        return "No Config found, PID = " + pid;
    }

    public static String deleteBackup(String pid) throws IOException {
        if("*".equals(pid)){
            Backups.removeAll();
            return "All Backups deleted.";
        }else {
            Backups.remove(pid);
            return "Backup deleted: " + pid;
        }
    }

    public static String restoreBackup(TamayaConfigPlugin plugin, String pid) throws IOException {
        StringBuilder b = new StringBuilder("Restored Configurations:\n")
                .append("------------------------\n");
        if("*".equals(pid)){
            for(String current: Backups.getPids()){
                try{
                    if(plugin.restoreBackup(current)){
                        b.append(current).append(" -> restored.\n");
                    }else{
                        b.append(current).append(" -> no backup found.\n");
                    }
                }catch(Exception e){
                    b.append(current).append(" -> failed: " + e).append('\n');
                }
            }
            return b.toString();
        }else {
            try{
                if(plugin.restoreBackup(pid)){
                    return pid + " -> restored.\n";
                }else{
                    return pid + " -> no backup found.\n";
                }
            }catch(Exception e){
                return pid + " -> failed: " + e + '\n';
            }
        }
    }

    public static String listBackup(String pid) throws IOException {
        if(pid!=null){
            Dictionary<String, ?> props = Backups.get(pid);
            if(props==null){
                return "No backup found: " + pid;
            }else{
                return "PID: " + pid + '\n' +
                        printProps(props);
            }
        }else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for(Map.Entry<String, Dictionary<String,?>> en: Backups.get().entrySet()){
                pw.println("PID: " + en.getKey());
                pw.println(printProps(en.getValue()));
            }
            return sw.toString();
        }
    }

    public static String printProps(Dictionary<String, ?> props) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(StringUtil.format("  Key", 50));
        pw.println(StringUtil.format("  Value", 50));
        pw.println("  " + StringUtil.printRepeat("-", 100));
        Enumeration<String> keys = props.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            pw.print("  " + StringUtil.format(key, 50));
            pw.println("  " + StringUtil.format(String.valueOf(props.get(key)), 50));
        }
        pw.println();
        pw.flush();
        return sw.toString();
    }

}