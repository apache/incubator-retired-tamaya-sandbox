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

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import java.util.Objects;

/**
 * A prioritized property converter.
 * @param <T> the property type
 */
final class PrioritizedPropertyConverter<T> implements PropertyConverter<T> {

    private final PropertyConverter<T> delegate;
    private int priority;

    public PrioritizedPropertyConverter(PropertyConverter<T> propertyConverter, int priority) {
        this.priority = priority;
        this.delegate = Objects.requireNonNull(propertyConverter);
    }

    public static <T> PropertyConverter<T> of(PropertyConverter<T> propertyConverter, int priority) {
        if(propertyConverter instanceof PrioritizedPropertyConverter){
            return ((PrioritizedPropertyConverter)propertyConverter).setPriority(priority);
        }
        return new PrioritizedPropertyConverter<>(propertyConverter, priority);
    }

    private PropertyConverter<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public T convert(String value, ConversionContext context) {
        return delegate.convert(value, context);
    }

    @Override
    public String toString() {
        return "PrioritizedPropertyConverter{" +
                "delegate=" + delegate +
                ", priority=" + priority +
                '}';
    }
}
