/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy createObject the License at
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
package org.apache.tamaya.doc.annot;

import org.apache.tamaya.spi.PropertyValue;

import java.lang.annotation.*;

/**
 * This annotation allows to specify a configuration area.
 */
@Repeatable(ConfigAreaSpecs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ConfigAreaSpec {
    /**
     * The areas configuration area path, e.g. {@code foo.bar}. An empty String means the root
     * of the configuration tree.
     * @return the path, not null.
     */
    String path()default"";

    /**
     * Define a description of the area.
     * @return the description.
     */
    String description() default "";

    /**
     * Defines the value type that is represented by a given area path, e.g. all properties on level
     * {@code foo.bar.Server} are mapped to some client classes {@code a.b.ServerConfig}.
     * @return the type mapping, if any.
     */
    Class<?> valueType() default Object.class;

    /**
     * Allows to specifiy the node type of an area:
     * <ul>
     *     <li><b>ARRAY: </b> is an array of child items.</li>
     *     <li><b>MAP: </b> is an map of named child items.</li>
     * </ul>
     * @return the area group type, default is {@link org.apache.tamaya.spi.PropertyValue.ValueType#MAP}.
     */
    PropertyValue.ValueType areaType() default PropertyValue.ValueType.MAP;

    /**
     * The minimal cardinality required. A cardinality > 0 means that the corresponding area must be present
     * in your configuration. This can be ensured/checked by a configuration validation system.
     * @return the minimal cardinality.
     */
    int min() default 0;

    /**
     * The maximal cardinality allowed. A cardinality > 0 means that the corresponding area must not be present
     * in your configuration more than the configured times. This can be ensured/checked by a configuration validation
     * system.
     * @return the maximal cardinality.
     */
    int max() default 0;

    /**
     * The properties managed in this area.
     * @return the properties managed within this area.
     */
    ConfigPropertySpec[] properties() default {};

    /**
     * Allows to define that this area is only required, if any of these configured areas are present in your config.
     * @return the area dependencies. This can be ensured/checked by a configuration validation
     */
    String[] dependsOnAreas() default {};

    /**
     * Allows to define that this area is only required, if any of these configured properties are present in your config.
     * @return the area dependencies. This can be ensured/checked by a configuration validation
     */
    String[] dependsOnProperties() default {};
}
