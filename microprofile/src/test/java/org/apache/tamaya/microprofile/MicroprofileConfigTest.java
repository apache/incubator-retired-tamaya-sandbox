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
package org.apache.tamaya.microprofile;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 24.03.17.
 */
public class MicroprofileConfigTest {

    @Test
    public void testDefaultConfigAccess() {
        Config config = ConfigProvider.getConfig();
        Iterable<ConfigSource> sources = config.getConfigSources();
        int count = 0;
        for (ConfigSource cs : sources) {
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testOptionalAccess(){
        Config config = ConfigProvider.getConfig();
        int count = 0;
        for(String key:config.getPropertyNames()){
            Optional<String> val = config.getOptionalValue(key, String.class);
            assertNotNull(val);
            val = config.getOptionalValue(key + System.currentTimeMillis(), String.class);
            assertNotNull(val);
            assertFalse(val.isPresent());
        }
    }

    @Test
    public void testGetValue(){
        Config config = ConfigProvider.getConfig();
        int count = 0;
        for(String key:config.getPropertyNames()){
            String val = config.getValue(key, String.class);
            assertNotNull(val);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetValue_NoValue(){
        Config config = ConfigProvider.getConfig();
        config.getValue("fooBar", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValue_InvalidType(){
        Config config = ConfigProvider.getConfig();
        config.getValue("java.version", Integer.class);
    }


}
