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

import javax.config.spi.Converter;
import java.util.Objects;

/**
 * Converter implementation that wraps a Javaconfig {@link Converter} instance.
 */
final class TamayaPropertyConverterAdapter<T> implements PropertyConverter<T> {

    private Converter<T> delegate;

    /**
     * Creates a new adapter instance.
     * @param delegate the delegate, not null.
     */
    public TamayaPropertyConverterAdapter(Converter<T> delegate){
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * Access the underlying converter instance.
     * @return the underlying converter, not null.
     */
    public Converter<T> getConverter(){
        return this.delegate;
    }

    @Override
    public T convert(String value, ConversionContext context) {
        return delegate.convert(value);
    }

    @Override
    public String toString() {
        return "TamayaPropertyConverterAdapter{" +
                "delegate=" + delegate +
                '}';
    }
}
