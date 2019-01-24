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
package org.apache.tamaya.jsr382.converter;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.ConfigQuery;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import javax.annotation.Priority;
import javax.inject.Provider;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converter, converting from String to Boolean for 1 = true, otherwise false.
 */
@Priority(-1)
public class ProviderConverter implements PropertyConverter<Provider> {

    private static final Logger LOG = Logger.getLogger(ProviderConverter.class.getName());

    @Override
    public Provider convert(String value, ConversionContext context) {
        return () -> {
            try{
                Type targetType = context.getTargetType().getType();
                ConvertQuery converter = new ConvertQuery(value, TypeLiteral.of(targetType));
                return context.getConfiguration().query(converter);
            }catch(Exception e){
                throw new ConfigException("Error evaluating config value.", e);
            }
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

    /**
     * A class for converting from a String to a particular type.
     * @param <T> the ConfigQuery type
     */
    private static final class ConvertQuery<T> implements ConfigQuery<T> {

        private String rawValue;
        private TypeLiteral<T> type;

        public ConvertQuery(String rawValue, TypeLiteral<T> type) {
            this.rawValue = Objects.requireNonNull(rawValue);
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public T query(Configuration config) {
            List<PropertyConverter<T>> converters = config.getContext().getPropertyConverters(type);
            ConversionContext context = new ConversionContext.Builder(type).setConfiguration(config)
                    .setConfiguration(config).setKey(ConvertQuery.class.getName()).build();
            for(PropertyConverter<?> conv: converters) {
                try{
                    if(conv instanceof ProviderConverter){
                        continue;
                    }
                    T result = (T)conv.convert(rawValue, context);
                    if(result!=null){
                        return result;
                    }
                }catch(Exception e){
                    LOG.log(Level.FINEST,  e, () -> "Converter "+ conv +" failed to convert to " + type);
                }
            }
            return null;
        }
    }
}
