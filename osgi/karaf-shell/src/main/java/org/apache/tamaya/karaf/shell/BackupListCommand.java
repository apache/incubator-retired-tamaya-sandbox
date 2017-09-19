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
import org.apache.tamaya.osgi.InitialState;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

@Command(scope = "tamaya", name = "backup-list", description="Gets the OSGI configuration before Tamya applied changes.")
@Service
public class BackupListCommand implements Action{

    @Argument(index = 0, name = "pid", description = "Allows to filter on the given PID.",
            required = false, multiValued = false)
    String pid;

    @Override
    public Object execute() throws IOException {
        if(pid!=null){
            Dictionary<String, ?> props = InitialState.get(pid);
            if(props==null){
                System.out.println("No backup found: " + pid);
            }else{
                System.out.println("PID: " + pid);
                printProps(props);
            }
        }else {
            for(Map.Entry<String, Dictionary<String,?>> en: InitialState.get().entrySet()){
                System.out.println("PID: " + en.getKey());
                printProps(en.getValue());
            }
        }
        return null;
    }

    public static void printProps(Dictionary<String, ?> props) {
        System.out.print(StringUtil.format("  Key", 50));
        System.out.println(StringUtil.format("  Value", 50));
        System.out.println("  " + StringUtil.printRepeat("-", 100));
        Enumeration<String> keys = props.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            System.out.print("  " + StringUtil.format(key, 50));
            System.out.println("  " + StringUtil.format(String.valueOf(props.get(key)), 50));
        }
        System.out.println();
    }

}