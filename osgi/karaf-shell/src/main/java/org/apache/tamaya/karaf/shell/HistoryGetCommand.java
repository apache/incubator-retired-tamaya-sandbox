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
import org.apache.tamaya.osgi.commands.HistoryCommands;

import java.io.IOException;
import java.util.List;

@Command(scope = "tamaya", name = "tm_history", description="Gets the history of changes Tamaya applied to the OSGI configuration.")
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
        System.out.println(HistoryCommands.getHistory(pid, eventTypes));
        return null;
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