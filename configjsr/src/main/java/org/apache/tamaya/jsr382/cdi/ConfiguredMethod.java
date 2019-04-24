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
package org.apache.tamaya.jsr382.cdi;

import org.apache.tamaya.Configuration;

import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Method;

/**
 * Implementation of a configured methods for CDI module.
 */
public final class ConfiguredMethod {

    private final Method method;
    private String key;

    ConfiguredMethod(InjectionPoint injectionPoint, String key) {
        this.method = (Method) injectionPoint.getMember();
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Method getAnnotatedMethod() {
        return method;
    }

    public String getName() {
        return method.getName();
    }

    public String getSignature() {
        return null;
    }

    public void configure(Object instance, Configuration config) {
        throw new UnsupportedOperationException("Use CDI annotations for configuration injection.");
    }

    @Override
    public String toString() {
        return "CDIConfiguredMethod[" + getSignature() + ']';
    }
}
