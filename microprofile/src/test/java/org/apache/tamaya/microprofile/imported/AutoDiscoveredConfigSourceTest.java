/*
 *******************************************************************************
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
 *******************************************************************************/
package org.apache.tamaya.microprofile.imported;

import org.apache.tamaya.microprofile.imported.converters.Pizza;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Verify the method addDiscoveredSources() on ConfigBuilder.
 *
 * @author <a href="mailto:emijiang@uk.ibm.com">Emily Jiang</a>
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class AutoDiscoveredConfigSourceTest {

    @Test
    public void testAutoDiscoveredConfigureSources() {
        Config config = ConfigProviderResolver.instance().getBuilder().addDefaultSources().addDiscoveredSources().build();
        Assert.assertEquals(config.getValue("tck.config.test.customDbConfig.key1", String.class), "valueFromDb1");
    }

    @Test
    public void testAutoDiscoveredConverterManuallyAdded() {
               
        Config config = ConfigProviderResolver.instance().getBuilder().addDefaultSources().addDiscoveredSources().addDiscoveredConverters().build();
        Pizza dVaule = config.getValue("tck.config.test.customDbConfig.key3", Pizza.class);
        Assert.assertEquals(dVaule.getSize(), "big");
        Assert.assertEquals(dVaule.getFlavor(), "cheese");
    }
    
    @Test
    public void testAutoDiscoveredConverterNotAddedAutomatically() {               
        Config config = ConfigProviderResolver.instance().getBuilder().addDefaultSources().addDiscoveredSources().build();
        try {
            // Pizza is too simple, so Tamaya find's a way to construct it.
            Pizza dVaule = config.getValue("tck.config.test.customDbConfig.key3", Pizza.class);
            System.out.println("WARNING: The auto discovered converter should not be added automatically.");
        } 
        catch (Exception e) {
            Assert.assertTrue( e instanceof IllegalArgumentException);
        }
       
    }
}
