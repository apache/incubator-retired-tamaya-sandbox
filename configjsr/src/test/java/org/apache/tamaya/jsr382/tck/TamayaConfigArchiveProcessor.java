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
package org.apache.tamaya.jsr382.tck;

import org.apache.tamaya.core.internal.converters.OptionalConverter;
import org.apache.tamaya.jsr382.JavaConfigAdapterFactory;
import org.apache.tamaya.jsr382.TamayaConfigProviderResolver;
import org.apache.tamaya.jsr382.cdi.JavaConfigCDIExtension;
import org.apache.tamaya.jsr382.converter.BooleanAsIntegerConverterFix;
import org.apache.tamaya.spi.PropertyConverter;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import javax.config.spi.ConfigProviderResolver;
import javax.enterprise.inject.spi.Extension;
import java.io.File;

/**
 * Adds the whole Config implementation classes and resources to the
 * Arquillian deployment archive. This is needed to have the container
 * pick up the beans from within the impl for the TCK tests.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class TamayaConfigArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            File[] coreLibs = Maven.resolver()
                    .loadPomFromFile("pom.xml").resolve("org.apache.tamaya:tamaya-core")
                    .withTransitivity().asFile();
            File[] apiLibs = Maven.resolver()
                    .loadPomFromFile("pom.xml").resolve("org.apache.tamaya:tamaya-api")
                    .withTransitivity().asFile();
            File[] functionsLib = Maven.resolver()
                    .loadPomFromFile("pom.xml").resolve("org.apache.tamaya.ext:tamaya-functions")
                    .withTransitivity().asFile();

            JavaArchive configJar = ShrinkWrap
                    .create(JavaArchive.class, "tamaya-config-impl.jar")
                    .addPackage(JavaConfigAdapterFactory.class.getPackage())
                    .addPackage(JavaConfigCDIExtension.class.getPackage())
                    .addPackage(BooleanAsIntegerConverterFix.class.getPackage())
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsServiceProvider(ConfigProviderResolver.class, TamayaConfigProviderResolver.class)
                    .addAsServiceProvider(PropertyConverter.class, BooleanAsIntegerConverterFix.class)
                    .addAsServiceProvider(PropertyConverter.class, OptionalConverter.class)
                    .addAsServiceProvider(Extension.class, JavaConfigCDIExtension.class);
            ((WebArchive) applicationArchive).addAsLibraries(
                    configJar)
                    .addAsLibraries(apiLibs)
                    .addAsLibraries(coreLibs)
                    .addAsLibraries(functionsLib);
        }
    }
}