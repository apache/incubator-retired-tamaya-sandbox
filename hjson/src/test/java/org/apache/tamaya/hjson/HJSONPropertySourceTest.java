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
package org.apache.tamaya.hjson;

import org.apache.tamaya.spi.PropertySource;
import org.junit.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class HJSONPropertySourceTest extends CommonHJSONTestCaseCollection {

    @Test
    public void tamayaOrdinalKeywordIsNotPropagatedAsNormalProperty() throws Exception {
        URL configURL = HJSONPropertySourceTest.class.getResource("/configs/valid/with-explicit-priority.hjson");

        assertThat(configURL).isNotNull();

        HJSONPropertySource source = new HJSONPropertySource(configURL, 4);
        assertThat(source.getOrdinal()).isEqualTo(16784);
    }
    
    @Test
    public void testAcceptJsonArrays() throws Exception {
        URL configURL = HJSONPropertySourceTest.class.getResource("/configs/invalid/array.hjson");

        assertThat(configURL).isNotNull();

        new HJSONPropertySource(configURL);
    }

    @Override
    PropertySource getPropertiesFrom(URL source) throws Exception {
        return new HJSONPropertySource(source);
    }
}
