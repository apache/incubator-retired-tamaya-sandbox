/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tamaya.microprofile.cdi;

import org.apache.tamaya.Configuration;

import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Field;

/**
 * CDI implementation for event publishing of configured instances.
 */
class ConfiguredField {

    private final Field field;
    private String key;

    ConfiguredField(InjectionPoint injectionPoint, String key){
        this.field = (Field)injectionPoint.getMember();
        this.key = key;
    }

    public Class<?> getType() {
        return field.getType();
    }

    public String getKey() {
        return key;
    }

    public Field getAnnotatedField() {
        return field;
    }

    public String getName() {
        return field.getName();
    }

    public String getSignature() {
        return getName()+':'+field.getType().getName();
    }

    public void configure(Object instance, Configuration config) {
        throw new UnsupportedOperationException("Use CDI annotations for configuration injection.");
    }

    @Override
    public String toString() {
        return "CDIConfiguredField["+getSignature()+']';
    }
}
