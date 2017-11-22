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

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;

import javax.annotation.Priority;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Converter, converting from String to Boolean for 1 = true, otherwise false.
 */
@Priority(-1)
public class BooleanAsIntegerConverterFix implements PropertyConverter<Boolean> {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    @Override
    public Boolean convert(String value, ConversionContext context) {
        context.addSupportedFormats(getClass(), "'1' (true), otherwise false.");
        try{
            int val = Integer.parseInt(Objects.requireNonNull(value).trim());
            if(val==1) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }catch(Exception e){
            // OK
            return Boolean.FALSE;
        }
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
