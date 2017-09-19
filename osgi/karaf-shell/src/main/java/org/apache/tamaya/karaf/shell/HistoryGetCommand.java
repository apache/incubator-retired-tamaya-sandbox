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

import org.apache.karaf.shell.api.action.*;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.apache.tamaya.osgi.ConfigHistory;
import org.apache.tamaya.osgi.TamayaConfigPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Command(scope = "tamaya", name = "history-get", description="Gets the history of changes Tamaya applied to the OSGI configuration.")
@Service
public class HistoryGetCommand implements Action{

    @Reference
    private TamayaConfigPlugin configPlugin;

    @Argument(index = 0, name = "pid", description = "Allows to filter on the given PID.",
            required = false, multiValued = false)
    String pid;

    @Option(name = "--type", aliases = "-t", description = "Allows to filter the events types shown.",
            required = false, multiValued = true)
    @Completion(FilterCompleter.class)
    private String[] eventTypes;

    @Override
    public Object execute() throws IOException {
        List<ConfigHistory> history = ConfigHistory.history(pid);
        history = filterTypes(history);
        System.out.print(StringUtil.format("Typ", 10));
        System.out.print(StringUtil.format("PID", 30));
        System.out.print(StringUtil.format("Key", 30));
        System.out.print(StringUtil.format("Value", 40));
        System.out.println(StringUtil.format("Previous Value", 40));
        System.out.println(StringUtil.printRepeat("-", 140));
        for(ConfigHistory h:history){
            System.out.print(StringUtil.format(h.getType().toString(), 10));
            System.out.print(StringUtil.format(h.getPid(), 30));
            System.out.print(StringUtil.format(h.getKey(), 30));
            System.out.print(StringUtil.format(String.valueOf(h.getValue()), 40));
            System.out.println(String.valueOf(h.getPreviousValue()));
        }
        return null;
    }

    private List<ConfigHistory> filterPid(List<ConfigHistory> history) {
        if(pid==null){
            return history;
        }
        List<ConfigHistory> result = new ArrayList<>();
        for(ConfigHistory h:history){
            if(h.getPid().equals(pid)){
                result.add(h);
            }
        }
        return result;
    }

    private List<ConfigHistory> filterTypes(List<ConfigHistory> history) {
        if(eventTypes==null){
            return history;
        }
        List<ConfigHistory> result = new ArrayList<>();
        Set<ConfigHistory.TaskType> types = new HashSet<>();
        for(String tVal:eventTypes) {
            types.add(ConfigHistory.TaskType.valueOf(tVal));
        }
        for(ConfigHistory h:history){
            if(types.contains(h.getType())){
                result.add(h);
            }
        }
        return result;
    }

    @Service
    public static final class FilterCompleter implements Completer {

        @Override
        public int complete(Session session, CommandLine commandLine, List<String> candidates) {
            StringsCompleter delegate = new StringsCompleter();
            for(ConfigHistory.TaskType taskType:ConfigHistory.TaskType.values()) {
                delegate.getStrings().add(taskType.toString());
            }
            return delegate.complete(session, commandLine, candidates);
        }
    }

}