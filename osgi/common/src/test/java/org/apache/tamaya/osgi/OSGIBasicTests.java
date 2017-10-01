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

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import org.apache.tamaya.osgi.commands.BackupCommands;
import org.apache.tamaya.osgi.commands.ConfigCommands;
import org.apache.tamaya.osgi.commands.HistoryCommands;
import org.apache.tamaya.osgi.commands.StringUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@Ignore
public class OSGIBasicTests {

    @Inject
    private BundleContext bundleContext;

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader( Constants.IMPORT_PACKAGE,
                        "org.apache.tamaya.osgi,org.apache.tamaya.osgi.commands" );
        return probe;
    }

    @org.ops4j.pax.exam.Configuration
    public Option[] config() throws FileNotFoundException {
        return new Option[] {
                cleanCaches(true),
                junitBundles(),
                mavenBundle("org.apache.felix","org.apache.felix.configadmin"),
                mavenBundle("org.apache.geronimo.specs", "geronimo-annotation_1.2_spec", "1.0"),
                mavenBundle("org.apache.tamaya", "tamaya-api"),
                mavenBundle("org.apache.tamaya", "tamaya-core"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-spisupport"),
                mavenBundle("org.apache.tamaya.ext", "tamaya-functions"),
                streamBundle(TinyBundles.bundle()
                        .set( Constants.BUNDLE_VERSION, "0.4.0")
                        .set( Constants.BUNDLE_NAME, "org.apache.tamaya.osgi" )
                        .set( Constants.BUNDLE_SYMBOLICNAME, "org.apache.tamaya.osgi" )
                        .set( Constants.EXPORT_PACKAGE, "org.apache.tamaya.osgi,org.apache.tamaya.osgi.commands" )
                        .set( Constants.IMPORT_PACKAGE, "org.osgi.framework,org.osgi.service.cm,org.apache.tamaya," +
                                "org.apache.tamaya.spi,org.apache.tamaya.functions,org.apache.tamaya.spisupport" )
                        .set( Constants.BUNDLE_ACTIVATOR, Activator.class.getName() )
                        .set( Constants.BUNDLE_MANIFESTVERSION, "2")
                        .set("Export-Service", "org.apache.tamaya.osgi.TamayaConfigPlugin")
                        .add(Activator.class)
                        .add(Backups.class)
                        .add(ConfigChanger.class)
                        .add(ConfigHistory.class)
                        .add(OperationMode.class)
                        .add(TamayaConfigPlugin.class)
                        .add(BackupCommands.class)
                        .add(ConfigCommands.class)
                        .add(HistoryCommands.class)
                        .add(StringUtil.class)
                        .add("META-INF/javaconfiguration.properties", new FileInputStream("src/test/resources/META-INF/javaconfiguration.properties"))
                        .add(OSGIBasicTests.class)
                        .build()),
        };
    }

    @Test
    public void hello(){}

}
