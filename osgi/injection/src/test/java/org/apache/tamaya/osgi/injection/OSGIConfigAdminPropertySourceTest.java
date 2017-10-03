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
package org.apache.tamaya.osgi.injection;

import org.apache.tamaya.spi.PropertyValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by atsti on 03.10.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class OSGIConfigAdminPropertySourceTest extends AbstractOSGITest{

    OSGIConfigAdminPropertySource propertySource;

    @Before
    public void init(){
        propertySource = new OSGIConfigAdminPropertySource(cm, "tamaya");
    }

    @Test
    public void get() throws Exception {
        PropertyValue val = propertySource.get("java.home");
        assertNotNull(val);
        assertEquals(val.getKey(), "java.home");
        assertEquals(val.getValue(), System.getProperty("java.home"));
        val = propertySource.get("foo.bar");
        assertNull(val);
    }

    @Test
    public void getProperties() throws Exception {
        Map<String,PropertyValue> props = propertySource.getProperties();
        assertNotNull(props);
        PropertyValue val = props.get("java.home");
        assertEquals(val.getKey(), "java.home");
        assertEquals(val.getValue(), System.getProperty("java.home"));
        val = props.get("foo.bar");
        assertNull(val);
    }

}