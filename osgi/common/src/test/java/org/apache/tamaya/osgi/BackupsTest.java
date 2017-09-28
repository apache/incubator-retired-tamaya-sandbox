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
package org.apache.tamaya.osgi;

import org.junit.Test;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Created by atsticks on 26.09.17.
 */
public class BackupsTest {


    private Dictionary<String,Object> createConfig(String pid){
        Hashtable<String,Object> config = new Hashtable<>();
        config.put("test.id", pid);
        return config;
    }
    @Test
    public void setGet() throws Exception {
        Dictionary<String,Object> cfg = createConfig("set");
        Backups.set("set", cfg);
        assertEquals(Backups.get("set"), cfg);
    }

    @Test
    public void remove() throws Exception {
        Dictionary<String,Object> cfg = createConfig("remove");
        Backups.set("remove", cfg);
        assertEquals(Backups.get("remove"), cfg);
        Backups.remove("remove");
        assertEquals(Backups.get("remove"), null);
    }

    @Test
    public void removeAll() throws Exception {
        Dictionary<String,Object> cfg = createConfig("remove");
        Backups.set("remove", cfg);
        assertEquals(Backups.get("remove"), cfg);
        Backups.removeAll();
        assertEquals(Backups.get("remove"), null);
    }

    @Test
    public void get1() throws Exception {
    }

    @Test
    public void getPids() throws Exception {
        Dictionary<String,Object> cfg = createConfig("getPids");
        Backups.set("getPids", cfg);
        Set<String> pids = Backups.getPids();
        assertNotNull(pids);
        assertTrue(pids.contains("getPids"));
        Backups.removeAll();
        pids = Backups.getPids();
        assertNotNull(pids);
        assertTrue(pids.isEmpty());
    }

    @Test
    public void contains() throws Exception {
        Dictionary<String,Object> cfg = createConfig("contains");
        Backups.set("contains", cfg);
        assertTrue(Backups.contains("contains"));
        assertFalse(Backups.contains("foo"));
        Backups.removeAll();
        assertFalse(Backups.contains("contains"));
        assertFalse(Backups.contains("foo"));
    }

    @Test
    public void saveRestore() throws Exception {
        Dictionary<String,Object> store = new Hashtable<>();
        Dictionary<String,Object> cfg = createConfig("contains");
        Backups.set("saveRestore", cfg);
        Backups.save(store);
        Backups.removeAll();
        assertFalse(Backups.contains("saveRestore"));
        Backups.restore(store);
        assertTrue(Backups.contains("saveRestore"));
        assertEquals(Backups.get("saveRestore"), cfg);
    }

}