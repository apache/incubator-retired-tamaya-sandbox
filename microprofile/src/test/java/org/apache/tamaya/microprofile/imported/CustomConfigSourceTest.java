/*
 * Copyright (c) 2016-2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.tamaya.microprofile.imported;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

import static org.eclipse.microprofile.config.tck.base.AbstractTest.addFile;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class CustomConfigSourceTest {

    private Config config = ConfigProvider.getConfig();

//    @Deployment
//    public static WebArchive deploy() {
//        JavaArchive testJar = ShrinkWrap
//                .create(JavaArchive.class, "customConfigSourceTest.jar")
//                .addClasses(org.eclipse.microprofile.config.tck.CustomConfigSourceTest.class, CustomDbConfigSource.class, CustomConfigSourceProvider.class)
//                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
//                .addAsServiceProvider(ConfigSource.class, CustomDbConfigSource.class)
//                .addAsServiceProvider(ConfigSourceProvider.class, CustomConfigSourceProvider.class)
//                .as(JavaArchive.class);
//
//        addFile(testJar, "META-INF/microprofile-config.properties");
//
//        WebArchive war = ShrinkWrap
//                .create(WebArchive.class, "customConfigSourceTest.war")
//                .addAsLibrary(testJar);
//        return war;
//    }


    @Test
    public void testConfigSourceProvider() {
        Assert.assertEquals(config.getValue("tck.config.test.customDbConfig.key1", String.class), "valueFromDb1");
    }
}
