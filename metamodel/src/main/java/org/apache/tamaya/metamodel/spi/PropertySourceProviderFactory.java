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
package org.apache.tamaya.metamodel.spi;

import org.apache.tamaya.metamodel.internal.SourceConfig;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;

import java.util.Map;


/**
 * {@link PropertySource} and {@link SourceConfig} instances that
 * implement configurable are configured with the according configuration
 * settings provided in the {@code tamaya-config.xml} meta-configuration.
 */
public interface PropertySourceProviderFactory {

    /**
     * Resolve the given expression (without the key part).
     * @param config any further extended configuration, not null, but may be
     *                       empty.
     * @return the property source, or null.
     */
    PropertySourceProvider create(Map<String, String> config);

    /**
     * Get the property source type. The type is used to identify the correct factory instance
     * to resolve a configured property source.
     * @return the (unique) type key, never null and not empty.
     */
    String getType();
}
