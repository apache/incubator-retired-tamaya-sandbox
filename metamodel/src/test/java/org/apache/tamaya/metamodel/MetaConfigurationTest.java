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
package org.apache.tamaya.metamodel;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetaConfigurationTest {

    @Test
    public void configure() {
        Configuration config = Configuration.current();
        MetaConfiguration.configure();
        Configuration config2 = Configuration.current();
        assertThat(config).isNotEqualTo(config2);
    }

    @Test
    public void configure_with_URL() {
        Configuration config = Configuration.current();
        MetaConfiguration.configure(getClass().getResource("/integrationTests/empty-config.conf"));
        Configuration config2 = Configuration.current();
        assertThat(config).isNotEqualTo(config2);
        assertThat(config2.getContext().getPropertySources()).isEmpty();
        assertThat(config2.getContext().getPropertyConverters()).isEmpty();
        assertThat(config2.getContext().getPropertyFilters()).isEmpty();
    }

    @Test
    public void createConfigBuilder() {
        ConfigurationBuilder builder = MetaConfiguration.createConfigBuilder(
                getClass().getResource("/integrationTests/empty-config.conf"));
        assertThat(builder).isNotNull();
        assertThat(builder.getPropertySources()).isEmpty();
        assertThat(builder.getPropertyConverter()).isEmpty();
        assertThat(builder.getPropertyFilters()).isEmpty();
    }

    @Test
    public void createConfiguration() {
        Configuration config = MetaConfiguration.createConfiguration(getClass().getResource("/integrationTests/empty-config.conf"));
        assertThat(config.getContext().getPropertySources()).isEmpty();
        assertThat(config.getContext().getPropertyConverters()).isEmpty();
        assertThat(config.getContext().getPropertyFilters()).isEmpty();
    }
}