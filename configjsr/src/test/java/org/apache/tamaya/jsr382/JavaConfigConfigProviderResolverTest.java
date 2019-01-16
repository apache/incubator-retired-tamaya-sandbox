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
package org.apache.tamaya.jsr382;

import org.junit.Test;

import javax.config.Config;
import javax.config.spi.ConfigProviderResolver;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by atsticks on 24.03.17.
 */
public class JavaConfigConfigProviderResolverTest {

    @Test
    public void testInstance(){
        assertThat(ConfigProviderResolver.instance()).isNotNull();
    }

    @Test
    public void testGetBuilder(){
        assertThat(ConfigProviderResolver.instance().getBuilder()).isNotNull();
    }

    @Test
    public void testGetConfig(){
        assertThat(ConfigProviderResolver.instance().getConfig()).isNotNull();
    }

    @Test
    public void testGetConfig_CL(){
        assertThat(ConfigProviderResolver.instance().getConfig(ClassLoader.getSystemClassLoader())).isNotNull();
    }

    @Test
    public void testRegisterAndReleaseConfig(){
        ClassLoader cl = new URLClassLoader(new URL[]{});
        Config emptyConfig = ConfigProviderResolver.instance().getBuilder().build();
        assertThat(emptyConfig).isNotNull();
        Config cfg = ConfigProviderResolver.instance().getConfig(cl);
        assertThat(cfg).isNotNull();
        ConfigProviderResolver.instance().registerConfig(emptyConfig, cl);
        cfg = ConfigProviderResolver.instance().getConfig(cl);
        assertThat(cfg).isNotNull();
        assertThat(cfg).isEqualTo(emptyConfig);
        ConfigProviderResolver.instance().releaseConfig(emptyConfig);
        cfg = ConfigProviderResolver.instance().getConfig(cl);
        assertThat(cfg).isNotNull();
        assertThat(cfg).isNotSameAs(emptyConfig);
    }

}
