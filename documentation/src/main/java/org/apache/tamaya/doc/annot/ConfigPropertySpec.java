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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation allows one to specify a configuration property.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ConfigPropertySpec {

//    boolean id() default false;

    /**
     * The property name. The full property key is a combination of the parent
     * @return the name of the property, taken from the default resolution, if empty.
     */
    String name() default "";

    /**
     * Define a description of the property.
     * @return the description.
     */
    String description() default "";

    /**
     * The property's value type, by default {@link String}.
     * @return the property's value type.
     */
    Class<?> valueType() default String.class;

    /**
     * Allows to define that this property is only required, if any of these configured areas are present in your config.
     * @return the area dependencies. This can be ensured/checked by a configuration validation
     */
    String[] dependsOnAreas() default {};

    /**
     * Allows to define that this property is only required, if any of these configured properties are present in your config.
     * @return the area dependencies. This can be ensured/checked by a configuration validation
     */
    String[] dependsOnProperties() default {};

}
