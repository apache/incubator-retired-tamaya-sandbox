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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by atsti on 30.09.2017.
 */
public class HistoryCommandsTest {
    @Test
    public void clearHistory() throws Exception {
        ConfigHistory.configured("clearHistory1", "test");
        ConfigHistory.configured("clearHistory2", "test");
        assertTrue(ConfigHistory.history("clearHistory1").size()==1);
        assertTrue(ConfigHistory.history("clearHistory2").size()==1);
        assertTrue(ConfigHistory.history("clearHistory3").size()==0);
        String result = HistoryCommands.clearHistory("clearHistory1");
        assertTrue(result.contains("PID"));
        assertTrue(result.contains("clearHistory1"));
        assertTrue(ConfigHistory.history("clearHistory1").size()==0);
        assertTrue(ConfigHistory.history("clearHistory2").size()==1);
        assertTrue(ConfigHistory.history("clearHistory3").size()==0);
        ConfigHistory.configured("clearHistory1", "test");
        result = HistoryCommands.clearHistory("*");
        assertTrue(result.contains("PID"));
        assertTrue(result.contains("*"));
        assertTrue(ConfigHistory.history("clearHistory1").size()==0);
        assertTrue(ConfigHistory.history("clearHistory2").size()==0);
        assertTrue(ConfigHistory.history("clearHistory3").size()==0);

    }

    @Test
    public void getHistory() throws Exception {
        ConfigHistory.configured("getHistory", "test");
        ConfigHistory.configuring("getHistory", "test");
        ConfigHistory.propertySet("getHistory", "k1", "v1", null);
        ConfigHistory.propertySet("getHistory", "k2", null, "v2");
        String result = HistoryCommands.getHistory("getHistory");
        assertNotNull(result);
        assertTrue(result.contains("k1"));
        assertTrue(result.contains("v1"));
        assertTrue(result.contains("test"));
        result = HistoryCommands.getHistory("getHistory", ConfigHistory.TaskType.BEGIN.toString());
        assertNotNull(result);
        assertTrue(result.contains("getHistory"));
        assertTrue(result.contains("test"));
        assertFalse(result.contains("k1"));
        assertFalse(result.contains("v2"));
    }

    @Test
    public void getSetMaxHistorySize() throws Exception {
        String result = HistoryCommands.getMaxHistorySize();
        assertEquals(result, String.valueOf(ConfigHistory.getMaxHistory()));
        result = HistoryCommands.setMaxHistorySize(111);
        assertEquals(result, "tamaya.max-history-size=111");
        result = HistoryCommands.getMaxHistorySize();
        assertEquals(result, "111");
    }

}