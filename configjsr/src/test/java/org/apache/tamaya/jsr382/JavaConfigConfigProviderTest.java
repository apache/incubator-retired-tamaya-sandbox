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

import org.apache.tamaya.ConfigurationProvider;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by atsticks on 24.03.17.
 */
public class JavaConfigConfigProviderTest {

    @Test
    public void testDefaultConfigAccess(){
        Config config = ConfigProvider.getConfig();
        assertThat(config).isNotNull();
        Iterable<String> names = config.getPropertyNames();
        assertThat(names).isNotNull();
        int count = 0;
        for(String name:names){
            count++;
            System.out.println(count + ": " +name);
        }
        int cfgCount = 0;
        for(String s:ConfigProvider.getConfig().getPropertyNames()){
            cfgCount++;
        }
        assertThat(cfgCount).isLessThanOrEqualTo(count);
    }

    @Test
    public void testClassloaderAccess(){
        Config config = ConfigProvider.getConfig(Thread.currentThread().getContextClassLoader());
        assertThat(config).isNotNull();
        Iterable<String> names = config.getPropertyNames();
        assertThat(names).isNotNull();
        int count = 0;
        for(String name:names){
            count++;
        }
        assertThat(count).isGreaterThan(0);
    }

}
