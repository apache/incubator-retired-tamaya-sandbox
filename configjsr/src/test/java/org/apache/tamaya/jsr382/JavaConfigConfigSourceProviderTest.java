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
package org.apache.tamaya.jsr382;

import org.apache.tamaya.spisupport.propertysource.BuildablePropertySource;
import org.apache.tamaya.spisupport.propertysource.BuildablePropertySourceProvider;
import org.junit.Test;

import javax.config.spi.ConfigSource;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaConfigConfigSourceProviderTest {
    @Test
    public void getPropertySourceProvider() throws Exception {
        BuildablePropertySourceProvider prov = BuildablePropertySourceProvider.builder()
                .withPropertySourcs(
                        BuildablePropertySource.builder()
                        .withSimpleProperty("a", "b").build())
                .build();
        JavaConfigSourceProvider provider = new JavaConfigSourceProvider(prov);
        assertThat(provider).isNotNull();
        Iterable<ConfigSource> configSources = provider.getConfigSources(null);
        assertThat(configSources).isNotNull();
        assertThat(configSources.iterator().hasNext()).isTrue();
        assertThat("b").isEqualTo(configSources.iterator().next().getValue("a"));
    }

}
