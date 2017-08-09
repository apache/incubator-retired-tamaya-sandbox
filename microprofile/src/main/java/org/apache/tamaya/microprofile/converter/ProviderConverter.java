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
package org.apache.tamaya.microprofile.converter;

import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import javax.inject.Provider;
import java.util.logging.Logger;

/**
 * Converter, converting from String to Boolean.
 */
public class ProviderConverter implements PropertyConverter<Provider> {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Override
    public Provider<?> convert(String value, ConversionContext context) {
        TypeLiteral<Provider> target = (TypeLiteral<Provider>)context.getTargetType();
        return () -> {
            Object result = null;
            for(PropertyConverter pv:context.getConfigurationContext().getPropertyConverters(
                    TypeLiteral.of(target.getType()))){
                result = pv.convert(value, context);
                if(result!=null){
                    break;
                }
            }
            if(result==null){
                throw new IllegalArgumentException("Unconvertable value: " + value);
            }
            return result;
        };
    }

    @Override
    public boolean equals(Object o){
        return getClass().equals(o.getClass());
    }

    @Override
    public int hashCode(){
        return getClass().hashCode();
    }
}