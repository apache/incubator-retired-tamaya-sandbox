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
import org.junit.Test;

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

    @Test
    public void testGetJsonConfigurationInfo() {
        String info = bean.getJsonConfigurationInfo();
        assertThat(info).isNotNull().contains("java.version");
        System.out.println(bean.getJsonConfigurationInfo());
    }

    @Test
    public void testGetXmlConfigurationInfo() {
        String info = bean.getXmlConfigurationInfo();
        assertThat(info).isNotNull().contains("java.version", "<configuration>");
        System.out.println(bean.getXmlConfigurationInfo());
    }

    @Test
    public void testGetConfiguration() {
        Map<String,String> config = bean.getConfiguration();
        assertThat(config).isNotNull();
        for(Map.Entry<Object, Object> en:System.getProperties().entrySet()){
            assertThat(config.get(en.getKey())).isEqualTo(en.getValue());
        }
    }

    @Test
    public void testGetConfigurationArea() {
        Map<String,String> cfg = bean.getSection("java", false);
        for(Map.Entry<String,String> en:cfg.entrySet()){
            assertThat(System.getProperty(en.getKey())).isEqualTo(en.getValue());
        }
    }

    @Test
    public void testGetAreas() {
        Set<String> sections = (bean.getSections());
        assertThat(sections).isNotNull().contains("java", "file");
    }

    @Test
    public void testGetTransitiveAreas() {
        Set<String> sections = (bean.getTransitiveSections());
        Set<String> sectionsNT = (bean.getSections());
        assertThat(sections).isNotNull().contains("java", "sun", "sun.os");
        assertThat(sectionsNT.size()).isLessThan(sections.size());
    }

    @Test
    public void testIsAreaExisting() {
        assertThat(bean.isAreaExisting("java")).isTrue();
        assertThat(bean.isAreaExisting("sd.fldsfl.erlwsf")).isFalse();
    }

    @Test
    public void testRegisterMBean() throws Exception {
        ObjectName on = ConfigManagementSupport.registerMBean();
        ConfigManagementSupport.registerMBean();
        // Lookup createObject name
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        assertThat(mbs.getMBeanInfo(on) != null).isTrue();
    }

    @Test
    public void testRegisterMBean1() throws Exception {
        ObjectName on1 = ConfigManagementSupport.registerMBean("SubContext1");
        ConfigManagementSupport.registerMBean("SubContext1");
        ObjectName on2 = ConfigManagementSupport.registerMBean("SubContext2");
        // Lookup createObject name
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        assertThat(mbs.getMBeanInfo(on1)).isNotNull();
        assertThat(mbs.getMBeanInfo(on2)).isNotNull();
    }
}
