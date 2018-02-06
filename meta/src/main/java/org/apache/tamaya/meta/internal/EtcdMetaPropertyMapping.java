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
package org.apache.tamaya.meta.internal;

import org.apache.tamaya.meta.spi.MetaPropertyMapping;

import java.util.function.Predicate;


/**
 * Default metadata property mapping, which defines the following mapping:
 * <pre>
 * foo.bar.Property=foo
 *
 * // JSON
 * "_foo.bar.Property": {
 *   "propertysource": " MyPropertySource"
 * }
 *
 * // YAML
 * _foo.bar.Property:
 *   propertysource: "MyPropertySource"
 *
 * // properties
 * _foo.bar.Property.propertySource=MyPropertySource
 * </pre>
 */
public final class EtcdMetaPropertyMapping implements MetaPropertyMapping {

    @Override
    public String getKey(String entryKey, String metaKey) {
        return "_" + entryKey + "."+metaKey;
    }

    @Override
    public Predicate<String> getMetaEntryFilter(String key) {
        return s -> s.startsWith("_");
    }
}
