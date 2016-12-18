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

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.BootClasspathLibraryOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGIKarafTest {
 
    @Inject
    private BundleContext bundleContext;

    @Inject
    protected FeaturesService featuresService;
    private ConfigurationAdmin configAdmin;

    @Configuration
    public Option[] config() {
        MavenUrlReference karafStandardRepo = maven()
                .groupId("org.apache.karaf.features")
                .artifactId("standard")
                .classifier("features")
                .type("xml")
                .version(getKarafVersion());
        MavenUrlReference tamayaRepo = maven()
                .groupId("org.apache.tamaya.ext")
                .artifactId("tamaya-osgi-features")
                .type("xml")
                .classifier("features")
                .version("0.3-incubating-SNAPSHOT");
        return options(
                // distribution to test: Karaf 3.0.3
                KarafDistributionOption.karafDistributionConfiguration()
                        .frameworkUrl(CoreOptions.maven()
                                .groupId("org.apache.karaf")
                                .artifactId("apache-karaf")
                                .type("zip")
                                .version(getKarafVersion()))
                        .karafVersion(getKarafVersion())
                        .name("ApacheKaraf")
                        .useDeployFolder(false)
                        .unpackDirectory(new File("target/karaf")),
                // no local and remote consoles
//                KarafDistributionOption.debugConfiguration("5005", true),
                KarafDistributionOption.configureConsole().ignoreLocalConsole(),
                KarafDistributionOption.configureConsole().ignoreRemoteShell(),
                // keep runtime folder allowing analysing results
                KarafDistributionOption.keepRuntimeFolder(),
                // use custom logging configuration file with a custom appender
                KarafDistributionOption.replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg", new File(
                        "src/test/resources/org.ops4j.pax.logging.cfg")),

                mavenBundle("org.apache.geronimo.specs", "geronimo-annotation_1.2_spec", "1.0-alpha-1"),
                mavenBundle("org.apache.tamaya", "tamaya-api", "0.3-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya", "tamaya-core", "0.3-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-spisupport", "0.3-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-functions", "0.3-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-osgi", "0.3-incubating-SNAPSHOT"),
                KarafDistributionOption.features(
                        karafStandardRepo, "scr"),
                KarafDistributionOption.features(
                        tamayaRepo, "tamaya-osgi-features/0.3.0.incubating-SNAPSHOT"),
//                bundle("reference:file:target/test-classes"),
                junitBundles()
        );
    }

    public static String getKarafVersion() {
        ConfigurationManager cm = new ConfigurationManager();
        String karafVersion = cm.getProperty("pax.exam.karaf.version", "4.0.7");
        return karafVersion;
    }

    @Before
    public void beforeTest()throws Exception {
        featuresService.installFeature("config/4.0.7");
        featuresService.installFeature("tamaya-osgi-features/0.3.0.incubating-SNAPSHOT");
    }

    @Test
    public void ensureFeatureIsInstalled() throws Exception {
        Feature feature = featuresService.getFeature("tamaya-osgi-features/0.3.0.incubating-SNAPSHOT");
        assertNotNull(feature);
        for (Feature feat : featuresService.listFeatures()){
            System.out.println(feat);
        }
    }

    @Test
    public void ensureEnvironmentIsWorkingAndTamayaIsActive() {
        ConfigurationAdmin configAdmin = getConfigAdmin();
        assertNotNull(configAdmin);
        assertTrue(configAdmin instanceof TamayaConfigAdminImpl);
        System.out.println("ConfigAdmin found in Karaf OSGI Container: " + configAdmin);
    }

    @Test
    @Ignore
    public void getConfiguration() throws Exception {
        ConfigurationAdmin configAdmin = getConfigAdmin();
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
        org.osgi.service.cm.Configuration[] configs = configAdmin.listConfigurations("*");
        for (org.osgi.service.cm.Configuration cfg : configs) {
            b.append("\nConfiguration found in Karaf OSGI Container: " + cfg);
            b.append("\n-------------------------------------------------");
        }
        System.out.println(b.toString());
    }

    public ConfigurationAdmin getConfigAdmin() {
        return bundleContext.getService(bundleContext.getServiceReference(ConfigurationAdmin.class));
    }
}