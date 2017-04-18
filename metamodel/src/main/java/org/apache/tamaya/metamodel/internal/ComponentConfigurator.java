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
package org.apache.tamaya.metamodel.internal;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Small helper class for loading of configured instances.
 */
public final class ComponentConfigurator<T> {

    private static final Logger LOG = Logger.getLogger(ComponentConfigurator.class.getName());

    private ComponentConfigurator(){}

    /**
     * Configures the given instance with whatever is defined in the current child nodes.
     * @param instance the instance to be configured, not null.
     * @param node the node containing any configuration child nodes, not null.
     */
    public static void configure(Object instance, Node node) {
        Map<String,String> params = extractParameters(node);
        configure(instance, params);
    }

    /**
     * Configures the given instance with whatever is defined in the current child nodes.
     * @param instance the instance to be configured, not null.
     * @param params the node containing any configuration child nodes, not null.
     */
    public static void configure(Object instance, Map<String,String> params) {
        LOG.finest("Configuring instance: " + instance + " with " + params);
        for(Map.Entry<String,String> en:params.entrySet()){
            applyParam(instance, en.getKey(), en.getValue());
        }
    }

    /**
     * Apply parameters to instance using reflection ,only if found, as of now only
     * String and basic lang types are supported.
     * @param instance the instance to configure.
     * @param key the parameter name, not null.
     * @param value the value to be set, normally not null.
     */
    private static void applyParam(Object instance, String key, String value) {
        // apply parameters to instance using reflection ,only if found.
        Class type = instance.getClass();
        try {
            Method[] methods = type.getMethods();
            String methodName = "set" + toUpperCase(key);
            for(Method m:methods){
                if(methodName.equals(m.getName()) && m.getParameterTypes().length==1) {
                    if (applyParam(instance, key, value, m)) {
                        return;
                    }
                }
            }
        }catch(Exception e){
            LOG.log(Level.FINE, "Reflection issue configuring instance: " + instance, e);
        }
        try{
            Field field = type.getDeclaredField(key);
            applyParam(instance, key, value, field);
        }catch(Exception e){
            LOG.log(Level.FINE, "Reflection issue configuring instance: " + instance, e);
        }
    }

    private static String toUpperCase(String value) {
        return value.substring(0,1).toUpperCase() + value.substring(1);
    }

    /**
     * Apply parameters to instance using reflection ,only if found, as of now only
     * String and basic lang types are supported.
     * @param instance the instance to configure.
     * @param key the parameter name, not null.
     * @param value the value to be set, normally not null.
     * @param setter the setter method, not null.
     */
    private static boolean applyParam(Object instance, String key, String value, Method setter) {
        if(!Modifier.isPublic(setter.getModifiers())){
            LOG.fine("Setting method as accessible: " + instance.getClass().getSimpleName() + '#' + setter.getName());
            setter.setAccessible(true);
        }
        try {
            Class<?> targetType = setter.getParameterTypes()[0];
            setter.invoke(instance, convert(value, targetType));
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not apply parameter (SETTER) '" + key + "' to " + instance, e);
            return false;
        }
    }

    /**
     * Apply parameters to instance using reflection ,only if found, as of now only
     * String and basic lang types are supported.
     * @param instance the instance to configure.
     * @param key the parameter name, not null.
     * @param value the value to be set, normally not null.
     * @param field the field method, not null.
     */
    private static void applyParam(Object instance, String key, String value, Field field) {
        if(Modifier.isFinal(field.getModifiers())){
            LOG.finest("Ignoring final field: " + instance.getClass().getSimpleName() + '#' + field.getName());
            return;
        }
        if(!Modifier.isPublic(field.getModifiers())){
            LOG.finest("Setting field as accessible: " + instance.getClass().getSimpleName() + '#' + field.getName());
            field.setAccessible(true);
        }
        try {
            Class<?> targetType = field.getType();
            field.set(instance, convert(value, targetType));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not apply parameter (FIELD) '" + key + "' to " + instance, e);
        }
    }

    private static Object convert(String value, Class<?> targetType) {
        try {
            switch (targetType.getSimpleName()) {
                case "String":
                case "Object":
                    return value;
                case "boolean":
                case "Boolean":
                    return Boolean.valueOf(value);
                case "byte":
                case "Byte":
                    return Byte.valueOf(value);
                case "char":
                case "Character":
                    if (value.isEmpty()) {
                        return null;
                    }
                    return Character.valueOf(value.charAt(0));
                case "short":
                case "Short":
                    return Short.valueOf(value);
                case "int":
                case "Integer":
                    return Integer.valueOf(value);
                case "long":
                case "Long":
                    return Long.valueOf(value);
                case "float":
                case "Float":
                    return Float.valueOf(value);
                case "double":
                case "Double":
                case "Number":
                    return Float.valueOf(value);
                default:
                    Constructor c = targetType.getConstructor(String.class);
                    if (!Modifier.isPublic(c.getModifiers())) {
                        LOG.fine("Setting constructor as accessible: " + targetType.getSimpleName() + "#<constructor>(String)");
                        c.setAccessible(true);
                    }
                    return c.newInstance(value);
            }
        }catch(Exception e){
            LOG.log(Level.WARNING,
                    "Failed to convert value '"+value+"' to required target type: " + targetType.getName(), e);
            return null;
        }
    }

    public static Map<String, String> extractParameters(Node node) {
        Map<String,String> params = new HashMap<>();
        NamedNodeMap attributes = node.getAttributes();
        for(int c=0;c<attributes.getLength();c++) {
            Node pn = attributes.item(c);
            String key = pn.getNodeName();
            String value = pn.getNodeValue();
            params.put(key, value);
        }
        NodeList entryNodes = node.getChildNodes();
        for(int c=0;c<entryNodes.getLength();c++) {
            Node filterNode = entryNodes.item(c);
            if(filterNode.getNodeType()==Node.ELEMENT_NODE) {
                String key = filterNode.getNodeName();
                String value = filterNode.getTextContent();
                params.put(key, value);
            }
        }
        return params;
    }

}
