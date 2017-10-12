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

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Enumeration;


/**
 * Utility class implementing the available backup related commands.
 */
public final class BackupCommands {

    /** Singleton constructor. */
    private BackupCommands(){}

    public static String createBackup(TamayaConfigService service, ConfigurationAdmin cm, String pid, boolean force) throws IOException {
        Configuration cfg = cm.getConfiguration(pid);
        if(cfg!=null){
            Dictionary<String,?> props = cfg.getProperties();
            if(props!=null){
                if(force && service.getBackup(pid)!=null) {
                    service.deleteBackup(pid);
                }
                if(service.createBackup(pid)){
                    return "Backup created, PID = " + pid + '\n' +  printProps(props);
                }else{
                    return "Creating backup failed. Backup already existing, PID = " + pid;
                }
            }
        }
        return "Creating backup failed. No Config found, PID = " + pid;
    }

    public static String deleteBackup(TamayaConfigService service, String pid) throws IOException {
        if("*".equals(pid)){
            for(String current: service.getBackupPids()){
                service.deleteBackup(current);
            }
            return "All Backups deleted.";
        }else {
            service.deleteBackup(pid);
            return "Backup deleted: " + pid;
        }
    }

    public static String restoreBackup(TamayaConfigService plugin, String pid) throws IOException {
        StringBuilder b = new StringBuilder("Restored Configurations:\n")
                .append("------------------------\n");
        if("*".equals(pid)){
            for(String current: plugin.getBackupPids()){
                try{
                    if(plugin.restoreBackup(current)){
                        b.append(current).append(" -> restored.\n");
                    }else{
                        b.append(current).append(" -> no backup found.\n");
                    }
                }catch(Exception e){
                    b.append(current).append(" -> failed: ").append(e).append('\n');
                }
            }
            return b.toString();
        }else {
            try{
                if(plugin.restoreBackup(pid)){
                    return "Backup restored for PID: "+pid+"\n";
                }else{
                    return "Backup restore failed for PID "+pid+": no backup found.\n";
                }
            }catch(Exception e){
                return "Backup restore failed for PID "+pid+", error: " + e + '\n';
            }
        }
    }

    public static String listBackup(TamayaConfigService plugin, String pid) throws IOException {
        if(pid!=null){
            Dictionary<String, ?> props = plugin.getBackup(pid);
            if(props==null){
                return "No backup found: " + pid;
            }else{
                return "PID: " + pid + '\n' +
                        printProps(props);
            }
        }else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for(String current: plugin.getBackupPids()){
                pw.println("PID: " + current);
                pw.println(printProps(plugin.getBackup(current)));
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