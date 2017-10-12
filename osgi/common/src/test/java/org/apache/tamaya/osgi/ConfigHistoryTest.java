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
import java.util.List;

import static org.junit.Assert.*;


/**
 * Created by atsticks on 26.09.17.
 */
public class ConfigHistoryTest {
    @Test
    public void configuring() throws Exception {
        ConfigHistory en = ConfigHistory.configuring("configuring", "configuring_test");
        assertNotNull(en);
        assertEquals(en.getPid(), "configuring");
        assertEquals(en.getType(), ConfigHistory.TaskType.BEGIN);
        assertEquals(en.getValue(), "configuring_test");
    }

    @Test
    public void configured() throws Exception {
        ConfigHistory en = ConfigHistory.configured("configured", "configured_test");
        assertNotNull(en);
        assertEquals(en.getPid(), "configured");
        assertEquals(en.getType(), ConfigHistory.TaskType.END);
        assertEquals(en.getValue(), "configured_test");
    }

    @Test
    public void propertySet() throws Exception {
        ConfigHistory en = ConfigHistory.propertySet("propertySet", "propertySet.key", "new", "prev");
        assertNotNull(en);
        assertEquals(en.getPid(), "propertySet");
        assertEquals(en.getType(), ConfigHistory.TaskType.PROPERTY);
        assertEquals(en.getKey(), "propertySet.key");
        assertEquals(en.getPreviousValue(), "prev");
        assertEquals(en.getValue(),"new");
    }

    @Test
    public void setGetMaxHistory() throws Exception {
        ConfigHistory.setMaxHistory(1000);
        assertEquals(ConfigHistory.getMaxHistory(),1000);
    }

    @Test
    public void history() throws Exception {
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("getHistory", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory();
        assertNotNull(hist);
        assertTrue(hist.size()>=100);
    }

    @Test
    public void history_pid() throws Exception {
        ConfigHistory.configuring("history1", "history_pid");
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history1", "getHistory"+i, "prev"+i, "new"+i);
        }
        ConfigHistory.configured("history1", "history_pid");
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history2", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory("history1");
        assertNotNull(hist);
        assertTrue(hist.size()==102);
        hist = ConfigHistory.getHistory("history2");
        assertNotNull(hist);
        assertTrue(hist.size()==100);
        hist = ConfigHistory.getHistory(null);
        assertNotNull(hist);
        assertTrue(hist.size()>=202);
    }

    @Test
    public void clearHistory() throws Exception {
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history3", "getHistory"+i, "prev"+i, "new"+i);
        }
        for(int i=0;i<100;i++){
            ConfigHistory.propertySet("history4", "getHistory"+i, "prev"+i, "new"+i);
        }
        List<ConfigHistory> hist = ConfigHistory.getHistory("history3");
        assertNotNull(hist);
        assertTrue(hist.size()==100);
        assertEquals(ConfigHistory.getHistory("history4").size(), 100);
        ConfigHistory.clearHistory("history3");
        assertEquals(ConfigHistory.getHistory("history3").size(), 0);
        assertEquals(ConfigHistory.getHistory("history4").size(), 100);
        ConfigHistory.clearHistory(null);
        assertEquals(ConfigHistory.getHistory().size(), 0);
        assertEquals(ConfigHistory.getHistory("history4").size(), 0);
    }


    @Test
    public void setPreviousValue() throws Exception {
    }

    @Test
    public void getValue() throws Exception {
    }

    @Test
    public void getKey() throws Exception {
    }

    @Test
    public void saveRestore() throws Exception {
        for(int i=0;i<10;i++){
            ConfigHistory.propertySet("save", "getHistory"+i, "prev"+i, "new"+i);
        }
        assertEquals(ConfigHistory.getHistory("save").size(), 10);
        Dictionary<String,Object> config = new Hashtable<>();
        ConfigHistory.save(config);
        assertEquals(ConfigHistory.getHistory("save").size(), 10);
        ConfigHistory.clearHistory();
        assertEquals(ConfigHistory.getHistory("save").size(), 0);
        ConfigHistory.restore(config);
        assertEquals(ConfigHistory.getHistory("save").size(), 10);
    }

}