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
package org.apache.tamaya.gogo.shell;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Objects;

public class HistoryCommands {

    private BundleContext context;

    private <T> T getService(Class<T> type){
        ServiceReference<T> cmRef = context.getServiceReference(type);
        return context.getService(cmRef);
    }

    public HistoryCommands(BundleContext context){
        this.context = Objects.requireNonNull(context);
    }

    @Descriptor("Deletes the history of configuration changes.")
    public void history_delete(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-p", "--pid"})
                                   @Descriptor("The PID.") String pid) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.HistoryCommands.clearHistory(pid));
    }

    @Descriptor("Deletes the full history of configuration changes.")
    public void history_delete_all() throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.HistoryCommands.clearHistory(null));
    }

    @Descriptor("Read the history of configuration changes.")
    public void history_get(@Parameter(absentValue = "", names={"-p", "--pid"})
                                @Descriptor("The PID.")String pid,
                            @Parameter(absentValue = "", names={"-t", "--eventtypes"})
                            @Descriptor("The comma separated Event types to filter, valid types are " +
                            "PROPERTY,BEGIN,END")String eventTypes) throws IOException {
        if(eventTypes.isEmpty()){
            System.out.println(org.apache.tamaya.osgi.commands.HistoryCommands.getHistory(pid, null));
        }else {
            System.out.println(org.apache.tamaya.osgi.commands.HistoryCommands.getHistory(pid, eventTypes.split(",")));
        }
    }

    @Descriptor("Get the maximum configuration change history size.")
    public void maxhistory_get() throws IOException {
        System.out.println(String.valueOf(org.apache.tamaya.osgi.commands.HistoryCommands.getMaxHistorySize()));
    }

    @Descriptor("Sets the maximum configuration change history size.")
    public void maxhistory_set(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-s", "--size"})
                                   @Descriptor("The maximum size of history entries stored.")int maxSize) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.HistoryCommands.setMaxHistorySize(maxSize));
    }

}