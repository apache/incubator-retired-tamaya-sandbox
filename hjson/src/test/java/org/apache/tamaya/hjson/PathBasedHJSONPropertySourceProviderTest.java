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
package org.apache.tamaya.hjson;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PathBasedHJSONPropertySourceProvider}.
 */
public class PathBasedHJSONPropertySourceProviderTest {

    @Test
    public void getPropertySources() {
        PathBasedHJSONPropertySourceProvider provider = new PathBasedHJSONPropertySourceProvider(
                "configs/valid/*.hjson"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(7).isEqualTo(provider.getPropertySources().size());
    }

    @Test
    public void getPropertySources_one() {
        PathBasedHJSONPropertySourceProvider provider = new PathBasedHJSONPropertySourceProvider(
                "configs/valid/cyril*.hjson"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(1).isEqualTo(provider.getPropertySources().size());
    }

    @Test
    public void getPropertySources_two() {
        PathBasedHJSONPropertySourceProvider provider = new PathBasedHJSONPropertySourceProvider(
                "configs/valid/simple-*.hjson"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(3).isEqualTo(provider.getPropertySources().size());
    }

    @Test
    public void getPropertySources_none() {
        PathBasedHJSONPropertySourceProvider provider = new PathBasedHJSONPropertySourceProvider(
                "configs/valid/foo*.hjson", "configs/valid/*.HJSON"
        );
        assertThat(provider.getPropertySources()).isNotNull();
        assertThat(0).isEqualTo(provider.getPropertySources().size());
    }
}
