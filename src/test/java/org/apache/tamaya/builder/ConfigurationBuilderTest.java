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
package org.apache.tamaya.builder;

import org.apache.tamaya.Configuration;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ConfigurationBuilderTest {

    @Test
    public void buildCanBuildEmptyConfiguration() {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        Configuration config = builder.build();

        assertThat(config, notNullValue());
    }

    @Test(expected = IllegalStateException.class)
    public void buildCanBeCalledOnlyOnce() {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.build();
        builder.build();
    }

    @Test(expected = NullPointerException.class)
    public void addPropertySourcesDoesNotAcceptNullValue() {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.addPropertySources(null);
    }

}
