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
package org.apache.tamaya.metamodel;

import org.apache.tamaya.spisupport.propertysource.SystemPropertySource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EnabledPropertySourceTest {

    private SystemPropertySource systemPropertySource = new SystemPropertySource();
    private EnabledPropertySource enabledPropertySource = new EnabledPropertySource(
            systemPropertySource, "true"
    );

    @Test
    public void calculateEnabled() {
        assertThat(enabledPropertySource.calculateEnabled()).isTrue();
        enabledPropertySource = new EnabledPropertySource(
                systemPropertySource, "false"
        );
        assertThat(enabledPropertySource.calculateEnabled()).isFalse();
    }

    @Test
    public void setEnabled() {
        enabledPropertySource.setEnabled(false);
        assertThat(enabledPropertySource.isEnabled());
        enabledPropertySource.setEnabled(true);
        assertThat(enabledPropertySource.isEnabled());

    }

    @Test
    public void getOrdinal() {
        assertThat(enabledPropertySource.getOrdinal()).isEqualTo(systemPropertySource.getOrdinal());

    }

    @Test
    public void getName() {
        assertThat(enabledPropertySource.getName()).isEqualTo(systemPropertySource.getName());
    }

    @Test
    public void get() {
        assertThat(enabledPropertySource.get("java.version")).isEqualTo(systemPropertySource.get("java.version"));
        enabledPropertySource.setEnabled(false);
        assertThat(enabledPropertySource.get("java.version")).isNull();
    }

    @Test
    public void getProperties() {
        assertThat(enabledPropertySource.getProperties()).isEqualTo(systemPropertySource.getProperties());
        enabledPropertySource.setEnabled(false);
        assertThat(enabledPropertySource.getProperties()).isEmpty();
    }

    @Test
    public void isScannable() {
        assertThat(enabledPropertySource.isScannable()).isTrue();
    }

    @Test
    public void testToString() {
        assertThat(enabledPropertySource.toString()).isNotNull().isEqualTo("EnabledPropertySource{\n" +
                " enabled=true\n" +
                " wrapped=SystemPropertySource{  defaultOrdinal=1000\n" +
                "  ordinal=null\n" +
                "  disabled=false\n" +
                "  name='system-properties'\n" +
                "}}");
    }
}