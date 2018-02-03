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
package org.apache.tamaya.meta.spi;

import java.util.function.Predicate;

/**
 * Interface for defining the mapping strategy for meta properties.
 */
public interface MetaPropertyMapping {

    /**
     * Get the corresponding full metadata key, for the given entry key and metadata key.
     * @param entryKey the entry key, not null.
     * @param metaKey the metadata key, not null.
     * @return the corresponding metadata key, not null.
     */
    String getKey(String entryKey, String metaKey);

    /**
     * Get a predicate for the given key (optional) to extract all metaentries.
     * @param key the key, or null, for all metaentries in a configuration.
     * @return the predicate to test if the given key is a metadata key.
     */
    Predicate<String> getMetaEntryFilter(String key);

}

