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
package org.apache.tamaya.metamodel.internal.resolver;

import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.spi.SimpleResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Simple resolver for {@link MetaContext} entries that
 * reads data from system and environment properties.
 *
 * Valid inputs are:
 * <ul>
 *     <li>{@code ${properties:sys:key?default=abcval} } reading a system property.</li>
 *     <li>{@code ${properties:env:key?default=abcval} } reading a environment property.</li>
 *     <li>{@code ${properties:ctx:[ctxName:]key?default=abcval} } reading a <i>default</i> MetaContext entry.</li>
 * </ul>
 *
 * Hereby the _default_ parameter defines the default value to be applied, if no value was found.
 */
public final class JavaResolver implements SimpleResolver{
    @Override
    public String getResolverId() {
        return "java";
    }

    @Override
    public String evaluate(String expression) {
        String[] mainParts = expression.split("#");
        if(mainParts.length<2){
            return null;
        }else{
            try {
                return evaluate(mainParts[0].trim(), mainParts[1].trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String evaluate(String className, String memberName) throws Exception{
        Objects.requireNonNull(className);
        Objects.requireNonNull(memberName);
        Class clazz = Class.forName(className);
        try {
            Method method = clazz.getDeclaredMethod(memberName);
            if (Modifier.isStatic(method.getModifiers())) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return (String) method.invoke(null);
            }
        }catch(Exception e){
            // ignore
        }
        try {
            Field field = clazz.getDeclaredField(memberName);
            if (Modifier.isStatic(field.getModifiers())) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return (String) field.get(null);
            }
        }catch(Exception e){
            // ignore
        }
        return null;
    }
}
