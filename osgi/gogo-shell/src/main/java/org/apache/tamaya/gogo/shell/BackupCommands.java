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
import org.apache.tamaya.osgi.commands.TamayaConfigService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Objects;

public class BackupCommands {

    private BundleContext context;

    public BackupCommands(BundleContext context){
        this.context = Objects.requireNonNull(context);
    }

    private <T> T getService(Class<T> type){
        ServiceReference<T> cmRef = context.getServiceReference(type);
        return context.getService(cmRef);
    }


    @Descriptor("Creates an OSGI ConfigAdmin configuration backup for a PID.")
    public void tm_backup_create(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-p", "--pid"})
                                  @Descriptor("The PID (requred)") String pid,
                                 @Parameter(absentValue = Parameter.UNSPECIFIED, names={"-f", "--force"})
                                  @Descriptor("If set any existing backup will be overriden, default is false.") Boolean force) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.BackupCommands.createBackup(
                getService(TamayaConfigService.class),
                getService(ConfigurationAdmin.class), pid, force));
    }

    @Descriptor("Deletes an OSGI ConfigAdmin configuration backup for a PID.")
    public void tm_backup_delete(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-p", "--pid"})
                                  @Descriptor("The target PID") String pid) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.BackupCommands.deleteBackup(
                getService(TamayaConfigService.class),
                pid));
    }

    @Descriptor("Restores an OSGI ConfigAdmin configuration backup for a PID and disabled Tamaya for the given PID.")
    public void tm_backup_restore(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-p", "--pid"})
                                 @Descriptor("The target PID") String pid) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.BackupCommands.restoreBackup(
                getService(TamayaConfigService.class), pid));
    }

    @Descriptor("Shows the contents of the OSGI ConfigAdmin configuration backup for a PID.")
    public void tm_backup_get(@Parameter(absentValue = Parameter.UNSPECIFIED, names={"-p", "--pid"})
                               @Descriptor("The PID (requred)") String pid) throws IOException {
        System.out.println(org.apache.tamaya.osgi.commands.BackupCommands.listBackup(
                getService(TamayaConfigService.class),
                Objects.requireNonNull(pid)));
    }

}