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
package org.apache.tamaya.osgi;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Created by atsticks on 10.12.16.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class InjectionKarafTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    protected FeaturesService featuresService;

    @org.ops4j.pax.exam.Configuration
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
                .version("0.4-incubating-SNAPSHOT");
        return options(
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
                // if activated, the remote karaf instance will stop and wait for
                // debugger to connect to.
//                KarafDistributionOption.debugConfiguration("5006", true),
                KarafDistributionOption.configureConsole().ignoreLocalConsole(),
                KarafDistributionOption.configureConsole().ignoreRemoteShell(),
                // keep runtime folder allowing analysing results
                KarafDistributionOption.keepRuntimeFolder(),
                // use custom logging configuration file with a custom appender
                KarafDistributionOption.replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg", new File(
                        "src/test/resources/org.ops4j.pax.logging.cfg")),

                mavenBundle("org.apache.geronimo.specs", "geronimo-annotation_1.2_spec", "1.0-alpha-1"),
                mavenBundle("org.apache.tamaya", "tamaya-api", "0.4-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya", "tamaya-core", "0.4-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-spisupport", "0.4-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-functions", "0.4-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-osgi", "0.4-incubating-SNAPSHOT"),
                // injection libs
                mavenBundle("org.apache.geronimo.specs", "geronimo-atinject_1.0_spec", "1.0"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-injection-api", "0.4-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-injection", "0.4-incubating-SNAPSHOT"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-osgi-injection", "0.4-incubating-SNAPSHOT"),
                KarafDistributionOption.features(
                        karafStandardRepo, "scr"),
                KarafDistributionOption.features(
                        tamayaRepo, "tamaya-osgi-features/0.3.0.incubating-SNAPSHOT"),
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
        bundleContext.getService(bundleContext.getServiceReference(ConfigurationAdmin.class));
    }

    @Test
    public void checkTamayaInjectionOnService() throws Exception {
        Hashtable<String,String> config = new Hashtable<>();
        bundleContext.registerService(Hello.class, new ServiceFactory<Hello>() {
                    @Override
                    public Hello getService(Bundle bundle, ServiceRegistration<Hello> registration) {
                        return new HelloImpl();
                    }

                    @Override
                    public void ungetService(Bundle bundle, ServiceRegistration<Hello> registration, Hello service) {
                    }
                },
                config);
        ServiceReference<Hello> helloServiceRef = bundleContext.getServiceReference(Hello.class);
        assertNotNull(helloServiceRef);
        Hello hello = bundleContext.getService(helloServiceRef);
        assertNotNull(hello);
        assertEquals(hello.sayHello(), "Hello: ");
    }

}