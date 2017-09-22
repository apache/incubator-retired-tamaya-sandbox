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
import org.apache.tamaya.osgi.InitialState;
import org.apache.tamaya.osgi.commands.BackupCommands;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;

@Command(scope = "tamaya", name = "tm_backup_create", description="Creates a backup of a current OSGI configuration.")
@Service
public class BackupCreateCommand implements Action{

    @Argument(index = 0, name = "pid", description = "The target pid to backup.",
            required = true, multiValued = false)
    String pid;

    @Option(name = "--force", aliases = "-f", description = "Forces to (over)write a backup, even if one already exists.",
            required = false, multiValued = false)
    boolean replace;

    @org.apache.karaf.shell.api.action.lifecycle.Reference
    ConfigurationAdmin cm;

    @Override
    public Object execute() throws IOException {
        System.out.println(BackupCommands.createBackup(cm, pid, replace));
        return null;
    }

}