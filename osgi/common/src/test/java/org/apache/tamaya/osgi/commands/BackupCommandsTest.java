/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.osgi.AbstractOSGITest;
import org.apache.tamaya.osgi.Backups;
import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * Created by atsti on 30.09.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class BackupCommandsTest extends AbstractOSGITest {
    @Test
    public void createBackup() throws Exception {
        String result = BackupCommands.createBackup(cm, "createBackup", false);
        assertNotNull(result);
        assertTrue(result.contains("createBackup"));
        assertTrue(result.contains("Backup created"));
        assertTrue(Backups.contains("createBackup"));
        // A backup with the given name already exists, so it fails
        result = BackupCommands.createBackup(cm, "createBackup", false);
        assertNotNull(result);
        assertTrue(result.contains("createBackup"));
        assertTrue(result.contains("Creating backup failed"));
        assertTrue(result.contains("already existing"));
        assertTrue(Backups.contains("createBackup"));
        // any existing backups gets overridden
        result = BackupCommands.createBackup(cm, "createBackup", true);
        assertNotNull(result);
        assertTrue(result.contains("createBackup"));
        assertTrue(result.contains("Backup created"));
        assertTrue(Backups.contains("createBackup"));
    }

    @Test
    public void deleteBackup() throws Exception {
        BackupCommands.createBackup(cm, "deleteBackup", false);
        assertTrue(Backups.contains("deleteBackup"));
        String result = BackupCommands.deleteBackup("deleteBackup");
        assertNotNull(result);
        assertTrue(result.contains("deleteBackup"));
        assertTrue(result.contains("Backup deleted"));
        assertFalse(Backups.contains("deleteBackup"));
    }

    @Test
    public void restoreBackup() throws Exception {
        BackupCommands.createBackup(cm, "restoreBackup", false);
        assertTrue(Backups.contains("restoreBackup"));
        String result = BackupCommands.restoreBackup(tamayaConfigPlugin, "restoreBackup");
        assertNotNull(result);
        assertTrue(result.contains("restoreBackup"));
        assertTrue(result.contains("Backup restored"));
        BackupCommands.deleteBackup("restoreBackup");
        assertFalse(Backups.contains("restoreBackup"));
        result = BackupCommands.restoreBackup(tamayaConfigPlugin, "restoreBackup");
        assertTrue(result.contains("Backup restore failed"));
        assertTrue(result.contains("no backup found"));
    }

    @Test
    public void listBackup() throws Exception {
        BackupCommands.createBackup(cm, "listBackup", false);
        String result = BackupCommands.listBackup("listBackup");
        result.concat("listBackup");
        result.contains("pid");
    }

    @Test
    public void printProps() throws Exception {
        Hashtable<String,Object> props = new Hashtable<>();
        props.put("k1", "v1");
        props.put("k2", "v2");
        String result = BackupCommands.printProps(props);
        assertTrue(result.contains("k1"));
        assertTrue(result.contains("k2"));
        assertTrue(result.contains("v1"));
        assertTrue(result.contains("v2"));
    }

}