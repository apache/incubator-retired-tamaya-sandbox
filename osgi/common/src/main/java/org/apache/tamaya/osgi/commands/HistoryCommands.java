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

import org.apache.tamaya.osgi.ConfigHistory;
import org.apache.tamaya.osgi.TamayaConfigPlugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Utility class implementing the available change history related commands.
 */
public final class HistoryCommands{

    /** Singleton constructor. */
    private HistoryCommands(){}

    public static String clearHistory(String pid) throws IOException {
        int size = ConfigHistory.history(pid).size();
        ConfigHistory.clearHistory(pid);
        return "Deleted history for PID: " + pid;
    }

    public static String getHistory(String pid, String... events) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        List<ConfigHistory> history = ConfigHistory.history(pid);
        history = filterTypes(history, events);
        pw.print(StringUtil.format("Type", 10));
        pw.print(StringUtil.format("PID", 30));
        pw.print(StringUtil.format("Key", 30));
        pw.print(StringUtil.format("Value", 40));
        pw.println(StringUtil.format("Previous Value", 40));
        pw.println(StringUtil.printRepeat("-", 140));
        for(ConfigHistory h:history){
            pw.print(StringUtil.format(h.getType().toString(), 10));
            pw.print(StringUtil.format(h.getPid(), 30));
            pw.print(StringUtil.format(h.getKey(), 30));
            pw.print(StringUtil.format(String.valueOf(h.getValue()), 40));
            pw.println(String.valueOf(h.getPreviousValue()));
        }
        pw.flush();
        return sw.toString();
    }

    public static String getMaxHistorySize(){
        return String.valueOf(ConfigHistory.getMaxHistory());
    }

    public static String setMaxHistorySize(int maxSize){
        ConfigHistory.setMaxHistory(maxSize);
        return "tamaya-max-history-size="+maxSize;
    }

    private static List<ConfigHistory> filterTypes(List<ConfigHistory> history, String... eventTypes) {
        if(eventTypes==null || eventTypes.length==0){
            return history;
        }
        List<ConfigHistory> result = new ArrayList<>();
        Set<ConfigHistory.TaskType> types = new HashSet<>();
        for(String tt:eventTypes) {
            types.add(ConfigHistory.TaskType.valueOf(tt));
        }
        for(ConfigHistory h:history){
            if(types.contains(h.getType())){
                result.add(h);
            }
        }
        return result;
    }


}