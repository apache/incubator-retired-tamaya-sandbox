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
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.*;

/**
 * Created by atsticks on 24.03.17.
 */
public class MicroprofileConfigProviderResolverTest {

    @Test
    public void testInstance(){
        assertNotNull(ConfigProviderResolver.instance());
    }

    @Test
    public void testGetBuilder(){
        assertNotNull(ConfigProviderResolver.instance().getBuilder());
    }

    @Test
    public void testGetConfig(){
        assertNotNull(ConfigProviderResolver.instance().getConfig());
    }

    @Test
    public void testGetConfig_CL(){
        assertNotNull(ConfigProviderResolver.instance().getConfig(ClassLoader.getSystemClassLoader()));
    }

    @Test
    public void testRegisterAndReleaseConfig(){
        ClassLoader cl = new URLClassLoader(new URL[]{});
        Config emptyConfig = ConfigProviderResolver.instance().getBuilder().build();
        assertNotNull(emptyConfig);
        Config cfg = ConfigProviderResolver.instance().getConfig(cl);
        assertNotNull(cfg);
        ConfigProviderResolver.instance().registerConfig(emptyConfig, cl);
        cfg = ConfigProviderResolver.instance().getConfig(cl);
        assertNotNull(cfg);
        assertEquals(cfg, emptyConfig);
        ConfigProviderResolver.instance().releaseConfig(emptyConfig);
        cfg = ConfigProviderResolver.instance().getConfig(cl);
        assertNotNull(cfg);
        assertNotSame(cfg, emptyConfig);
    }



}
