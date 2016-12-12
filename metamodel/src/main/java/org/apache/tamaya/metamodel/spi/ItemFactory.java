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

import java.util.Map;

/**
 * Factory for items that are configurable using meta-configuration.
 * This allows easy registration using the name, instead of the fully qualified
 * class name.
 */
public interface ItemFactory<T> {

    /**
     * Get the factory name.
     * @return the factory name, not null.
     */
    String getName();

    /**
     * Create a new instance.
     * @param parameters the parameters for configuring the instance.
     * @return the new instance, not null.
     */
    T create(Map<String,String> parameters);

    /**
     * Get the target type created by this factory. This can be used to
     * assign the factory to an acording item base type, e.g. a PropertySource,
     * PropertySourceProvider, PropertyFilter etc.
     * @return the target type, not null.
     */
    Class<? extends T> getType();

}
