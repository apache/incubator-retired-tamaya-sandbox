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
package org.apache.tamaya.management.internal;

import org.apache.tamaya.management.ConfigManagementSupport;
import org.apache.tamaya.management.ManagedConfig;
import org.apache.tamaya.management.ManagedConfigMBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by Anatole on 20.08.2015.
 */
public class ManagedConfigTest {

    private final ManagedConfigMBean bean = new ManagedConfig();

    @org.junit.Test
    public void testGetJsonConfigurationInfo() throws Exception {
        String info = bean.getJsonConfigurationInfo();
        assertThat(info).isNotNull();
        assertThat(info.contains("java.version")).isTrue();
        System.out.println(bean.getJsonConfigurationInfo());
    }

    @org.junit.Test
    public void testGetXmlConfigurationInfo() throws Exception {
        String info = bean.getXmlConfigurationInfo();
        assertThat(info).isNotNull();
        assertThat(info.contains("java.version")).isTrue();
        assertThat(info.contains("<configuration>")).isTrue();
        System.out.println(bean.getXmlConfigurationInfo());
    }

    @org.junit.Test
    public void testGetConfiguration() throws Exception {
        Map<String,String> config = bean.getConfiguration();
        assertThat(config).isNotNull();
        for(Map.Entry<Object, Object> en:System.getProperties().entrySet()){
            assertThat(config.get(en.getKey())).isEqualTo(en.getValue());
        }
    }

    @org.junit.Test
    public void testGetConfigurationArea() throws Exception {
        Map<String,String> cfg = bean.getSection("java", false);
        for(Map.Entry<String,String> en:cfg.entrySet()){
            assertThat(System.getProperty(en.getKey())).isEqualTo(en.getValue());
        }
    }

    @org.junit.Test
    public void testGetAreas() throws Exception {
        Set<String> sections = (bean.getSections());
        assertThat(sections).isNotNull();
        assertThat(sections.contains("java")).isTrue();
        assertThat(sections.contains("file")).isTrue();
    }

    @org.junit.Test
    public void testGetTransitiveAreas() throws Exception {
        Set<String> sections = (bean.getTransitiveSections());
        Set<String> sectionsNT = (bean.getSections());
        assertThat(sections).isNotNull();
        assertThat(sections.contains("java")).isTrue();
        assertThat(sections.contains("sun")).isTrue();
        assertThat(sections.contains("sun.os")).isTrue();
        assertThat(sectionsNT.size() < sections.size()).isTrue();
    }

    @org.junit.Test
    public void testIsAreaExisting() throws Exception {
        assertThat(bean.isAreaExisting("java")).isTrue();
        assertThat(bean.isAreaExisting("sd.fldsfl.erlwsf")).isFalse();
    }

    @org.junit.Test
    public void testRegisterMBean() throws Exception {
        ObjectName on = ConfigManagementSupport.registerMBean();
        ConfigManagementSupport.registerMBean();
        // Lookup createObject name
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        assertThat(mbs.getMBeanInfo(on) != null).isTrue();
    }

    @org.junit.Test
    public void testRegisterMBean1() throws Exception {
        ObjectName on1 = ConfigManagementSupport.registerMBean("SubContext1");
        ConfigManagementSupport.registerMBean("SubContext1");
        ObjectName on2 = ConfigManagementSupport.registerMBean("SubContext2");
        // Lookup createObject name
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        assertThat(mbs.getMBeanInfo(on1) != null).isTrue();
        assertThat(mbs.getMBeanInfo(on2) != null).isTrue();
    }
}
