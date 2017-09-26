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
package org.apache.tamaya.osgi;

import org.apache.tamaya.spi.ConfigurationProviderSpi;
import org.apache.tamaya.spi.ServiceContextManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGITest {
 
    @Inject
    private BundleContext bundleContext;

    @Inject
    private ConfigurationAdmin configAdmin;

    private Activator activator = new Activator();

    @Before
    public void setup()throws Exception{
        activator.start(bundleContext);
    }

    @After
    public void tearDown()throws Exception{
        activator.stop(bundleContext);
    }

    @Configuration
    public Option[] config() {
        return options(
                cleanCaches(),
                junitBundles(),
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin"),
                mavenBundle("org.apache.geronimo.specs", "geronimo-annotation_1.2_spec", "1.0"),
                mavenBundle("org.apache.tamaya", "tamaya-api"),
                mavenBundle("org.apache.tamaya", "tamaya-core"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-spisupport"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-functions"),
                bundle("reference:file:target/classes")
        );
    }


    @Test
    public void testConfiguration() throws Exception {
        assertNotNull(configAdmin);
        org.osgi.service.cm.Configuration config = configAdmin.getConfiguration("tamaya");
        assertNotNull(config);
        assertNotNull(config.getProperties());
        assertFalse(config.getProperties().isEmpty());
        assertEquals(config.getProperties().size(), 4);
        assertEquals(config.getProperties().get("my.testProperty1"), "success1");
        assertEquals(config.getProperties().get("my.testProperty2"), "success2");
        assertEquals(config.getProperties().get("my.testProperty3"), "success3");
        assertEquals(config.getProperties().get("my.testProperty4"), "success4");
        StringBuilder b = new StringBuilder();
        b.append("Print all configs....\n\n");
        org.osgi.service.cm.Configuration[] configs = configAdmin.listConfigurations(null);
        for (org.osgi.service.cm.Configuration cfg : configs) {
            b.append("\nConfiguration found in OSGI Container: " + cfg);
            b.append("\n-------------------------------------------------");
        }
        System.out.println(b.toString());
    }

    @Test
    public void testResourceIsVisible(){
        assertNotNull(ServiceContextManager.getServiceContext()
        .getResource("META-INF/javaconfiguration.properties", null));
    }

    @Test
    public void testResourcesAreVisible() throws IOException {
        Enumeration<URL> urls = ServiceContextManager.getServiceContext()
                .getResources("META-INF/javaconfiguration.properties", null);
        assertNotNull(urls);
        assertTrue(urls.hasMoreElements());
        URL url = urls.nextElement();
        assertNotNull(url);
    }

    @Test
    public void testServices() {
        assertNotNull("ConfigurationProviderSpi service missing.", bundleContext.getService(bundleContext.getServiceReference(ConfigurationProviderSpi.class)));
        assertNotNull("ConfigurationAdmin service missing.", bundleContext.getService(bundleContext.getServiceReference(ConfigurationAdmin.class)));
    }
}