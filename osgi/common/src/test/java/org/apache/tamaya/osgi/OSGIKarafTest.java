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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.BootClasspathLibraryOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class OSGIKarafTest {
 
    @Inject
    private ConfigurationAdmin configAdmin;

    @Configuration
    public Option[] config() {
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
                KarafDistributionOption.configureConsole().startLocalConsole(),
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
                        maven().groupId("org.apache.karaf.features").artifactId("standard").type("xml")
                                .classifier("features").version(getKarafVersion()), "aries-blueprint"),
//                KarafDistributionOption.features(
//                        "mvn:org.apache.karaf.features/standard/4.0.7/xml/features",
//                        "aries-blueprint"),
//                bundle("reference:file:target/test-classes"),
                junitBundles()
        );
    }

    private String getKarafVersion() {
        return "4.0.7";
    }


    @Test
    @Ignore
    public void ensureEnvironmentIsWorkingAndTamayaIsActive() {
        assertNotNull(configAdmin);
        assertTrue(configAdmin instanceof TamayaConfigAdminImpl);
    }

    @Test
    @Ignore
    public void getConfiguration() throws Exception {
        org.osgi.service.cm.Configuration config = configAdmin.getConfiguration("tamaya");
        assertNotNull(config);
        assertFalse(config.getProperties().isEmpty());
        assertEquals(config.getProperties().size(), 4);
        assertEquals(config.getProperties().get("my.testProperty1"), "success1");
        assertEquals(config.getProperties().get("my.testProperty2"), "success2");
        assertEquals(config.getProperties().get("my.testProperty3"), "success3");
        assertEquals(config.getProperties().get("my.testProperty4"), "success4");
    }

}