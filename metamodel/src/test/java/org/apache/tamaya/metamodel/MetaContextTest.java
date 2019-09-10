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

import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class MetaContextTest {

    @Test
    public void getInstance() {
        assertThat(MetaContext.getInstance()).isNotNull();
    }

    @Test
    public void getId() {
        assertThat(MetaContext.getInstance().getId()).isNotNull().isNotEmpty();
    }

    @Test
    public void initialize() {
        MetaContext meta = new MetaContext();
        meta.initialize();
        assertThat(meta.getProperty("key", String.class)).isEqualTo(Optional.of("value"));
    }

    @Test
    public void initialize_WithClassLoader() {
        MetaContext meta = new MetaContext();
        meta.initialize(new URLClassLoader(new URL[0]));
        assertThat(meta.getProperty("key", String.class)).isEqualTo(Optional.of("value"));
    }

    @Test
    public void combineWith() {
        MetaContext meta = new MetaContext();
        meta.initialize();
        MetaContext meta2 = new MetaContext();
        meta2.setProperty("foo", String.class, "bar");
        meta.initialize(new URLClassLoader(new URL[0]));
        MetaContext meta3 = meta.combineWith(meta2);
        assertThat(meta3.getProperty("key", String.class)).isEqualTo(Optional.of("value"));
        assertThat(meta3.getProperty("foo", String.class)).isEqualTo(Optional.of("bar"));
        assertThat(meta2.getProperty("key", String.class)).isEqualTo(Optional.empty());
        assertThat(meta.getProperty("foo", String.class)).isEqualTo(Optional.empty());
    }

    @Test
    public void getStringProperty() {
        assertThat(MetaContext.getInstance().getProperty("key", String.class)).isEqualTo(Optional.of("value"));
    }

    @Test
    public void getBooleanProperty() {
        assertThat(MetaContext.getInstance().getProperty("boolean", boolean.class)).isEqualTo(Optional.of(true));
    }

    @Test
    public void getNumberProperty() {
        assertThat(MetaContext.getInstance().getProperty("int",  Number.class)).isEqualTo(Optional.of(Integer.valueOf(1)));
    }

    @Test
    public void getProperty() {
        assertThat(MetaContext.getInstance().getProperty("key", String.class)).isNotNull();
    }

    @Test
    public void setStringProperty() {
        MetaContext.getInstance().setStringProperty("a", "b");
        assertThat(MetaContext.getInstance().getProperty("key", String.class)).isEqualTo(Optional.of("value"));
        assertThat(MetaContext.getInstance().getProperty("a", String.class)).isEqualTo(Optional.of("b"));
    }

    @Test
    public void setBooleanProperty() {
        assertThat(MetaContext.getInstance().getProperty("boolean", Boolean.class)).isEqualTo(Optional.of(true));
    }

    @Test
    public void setNumberProperty() {
        assertThat(MetaContext.getInstance().getProperty("int",  Number.class)).isEqualTo(Optional.of(Integer.valueOf(1)));
    }

    @Test
    public void setProperty() {
        MetaContext.getInstance().setProperty("test", String.class, "testValue");
        assertThat(MetaContext.getInstance().getProperty("test", String.class)).isEqualTo(Optional.of("testValue"));
    }

    @Test
    public void setPropertyIfAbsent() {
        MetaContext.getInstance().setPropertyIfAbsent("test", String.class, "testValue");
        assertThat(MetaContext.getInstance().getProperty("test", String.class)).isEqualTo(Optional.of("testValue"));
        MetaContext.getInstance().setPropertyIfAbsent("test", String.class, "testValue2");
        assertThat(MetaContext.getInstance().getProperty("test", String.class)).isEqualTo(Optional.of("testValue"));
    }

    @Test
    public void checkPropertiesArePresent() {
        assertThat(MetaContext.getInstance().checkPropertiesArePresent()).isTrue();
        assertThat(MetaContext.getInstance().checkPropertiesArePresent("boolean", "int", "key")).isTrue();
        assertThat(MetaContext.getInstance().checkPropertiesArePresent("boolean", "boolean")).isTrue();
        assertThat(MetaContext.getInstance().checkPropertiesArePresent("boolean", "int", "foo")).isFalse();
        assertThat(MetaContext.getInstance().checkPropertiesArePresent("foo")).isFalse();
    }

    @Test
    public void getProperties() {
        assertThat(MetaContext.getInstance().getProperties())
                .containsEntry("int", 1)
                .containsEntry("boolean", true)
                .containsEntry("key", "value");
    }

    @Test
    public void testEquals() {
        MetaContext meta = new MetaContext();
        meta.initialize();
        MetaContext meta2 = new MetaContext();
        meta2.initialize();
        assertThat(meta).isNotEqualTo(meta2);
    }

    @Test
    public void testHashCode() {
        MetaContext meta = new MetaContext();
        meta.initialize();
        MetaContext meta2 = new MetaContext();
        meta2.initialize();
        assertThat(meta.hashCode()).isNotEqualTo(meta2.hashCode());
    }

    @Test
    public void testToString() {
        assertThat(MetaContext.getInstance().toString()).isNotNull()
                .contains("properties={boolean=true, test=testValue")
                .contains("float=23.45, key=value, int=1")
                .contains("MetaContext");
    }
}