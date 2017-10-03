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

import org.apache.tamaya.inject.api.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by atsti on 03.10.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class OSGIConfigurationInjectorTest extends AbstractOSGITest{


    @Test
    public void configure() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "OSGIConfigurationInjectorTest");
        Example example = new Example();
        Example result = injector.configure(example);
        assertNotNull(result);
        assertTrue(result==example);
        checkExampleConfig(example);
    }

    @Test
    public void getConfiguredSupplier() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "OSGIConfigurationInjectorTest");
        Supplier<Example> supplier = injector.getConfiguredSupplier(Example::new);
        assertNotNull(supplier);
        Example example = supplier.get();
        checkExampleConfig(example);
    }

    @Test
    public void createTemplate() throws Exception {
        OSGIConfigurationInjector injector = new OSGIConfigurationInjector(cm, "OSGIConfigurationInjectorTest");
        TemplateExample template = injector.createTemplate(TemplateExample.class);
        checkExampleConfig(template);
    }

    private void checkExampleConfig(Example example) {
        assertNotNull(example);
        assertEquals(example.javaHome, System.getProperty("java.home"));
        assertEquals(example.javaVersion, System.getProperty("java.version"));
        assertEquals(example.javaUsed, true);
    }

    private void checkExampleConfig(TemplateExample template) {
        assertNotNull(template);
        assertEquals(template.getJavaHome(), System.getProperty("java.home"));
        assertEquals(template.javaVersion(), System.getProperty("java.version"));
        assertEquals(template.isJavaUsed(), true);
    }

    private static final class Example {
        @Config("java.home")
        String javaHome;
        @Config("java.version")
        String javaVersion;
        @Config(value = "java.used", defaultValue = "true")
        boolean javaUsed;
    }

    private interface TemplateExample {

        @Config("java.home")
        String getJavaHome();

        @Config("java.version")
        String javaVersion();

        @Config(value = "java.used", defaultValue = "true")
        boolean isJavaUsed();
    }
}