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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Event published for items configured by CDI extensions. This is for example used by the documentation module
 * to automatically track the configuration endpoints for documentation.
 */
public final class ConfiguredType {

    private final Class<?> type;
    private final List<ConfiguredMethod> methods = new ArrayList<>();
    private final List<ConfiguredField> fields = new ArrayList<>();

    public ConfiguredType(Class<?> type){
        this.type = Objects.requireNonNull(type);
    }

    public Class getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    public Collection<ConfiguredField> getConfiguredFields() {
        return null;
    }

    public Collection<ConfiguredMethod> getConfiguredMethods() {
        return null;
    }

    public void configure(Object instance, Configuration config) {
        throw new UnsupportedOperationException("Use CDI annotations for configuration injection.");
    }

    /**
     * Used to build up during injection point processing.
     * @param injectionPoint the CDI injection point, not null.
     * @param key the possible config key, not null.
     */
    void addConfiguredMember(InjectionPoint injectionPoint, String key) {
        Member member = injectionPoint.getMember();
        if(member instanceof Field){
            this.fields.add(new ConfiguredField(injectionPoint, key));
        } else if(member instanceof Method){
            this.methods.add(new ConfiguredMethod(injectionPoint, key));
        }
    }

    @Override
    public String toString() {
        return "CDIConfiguredType{" +
                "type=" + type +
                ", methods=" + methods +
                ", fields=" + fields +
                '}';
    }
}
