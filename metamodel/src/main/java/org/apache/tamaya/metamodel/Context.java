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
package org.apache.tamaya.metamodel;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class managing the configuration system's shared context. This
 * context is used by the configuration system to evaluate the
 * right properties, e.g. by defining the current stage or labels
 * that apply to the current configuration.
 */
public final class Context {

    private static final Map<String,Context> CONTEXTS = new ConcurrentHashMap();
    private final Map<String,String> properties = new ConcurrentHashMap<>();

    public static Context getInstance(){
        return getInstance("");
    }

    /**
     * Access the context with a goven contextId, this allows to manage multiple
     * contexts, e.g. for different EE application's deployed. If no such context
     * exists a new one will be created.
     * @param contextId the contextId, not null.
     * @return the corresponding context, never null.
     */
    public static Context getInstance(String contextId){
        Context context = CONTEXTS.get(contextId);
        if(context==null){
            synchronized (Context.class){
                context = CONTEXTS.get(contextId);
                if(context==null){
                    context = new Context();
                    CONTEXTS.put(contextId, context);
                }
            }
        }
        return context;
    }

    public String getProperty(String key){
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue){
        String value = this.properties.get(key);
        if(value==null){
            return defaultValue;
        }
        return value;
    }

    public String setProperty(String key, String value){
       return this.properties.put(key,value);
    }

    public void setPropertyIfAbsent(String key, String value){
        String prev = this.properties.get(key);
        if(prev==null){
            this.properties.put(key, prev);
        }
    }

    public void setProperties(Map<String,String> properties){
        this.properties.putAll(properties);
    }

    public boolean checkProperties(String... keys){
        for(String key:keys) {
            if (getProperty(key, null) == null) {
                return false;
            }
        }
        return true;
    }

    public boolean checkProperty(String key, String value){
        if(value!=null){
            return value.equals(getProperty(key, null));
        }
        return !checkProperties(key);
    }

    public Map<String,String> getProperties(){
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Context)) return false;

        Context context = (Context) o;

        return getProperties().equals(context.getProperties());

    }

    @Override
    public int hashCode() {
        return getProperties().hashCode();
    }

    @Override
    public String toString() {
        return "Context{" +
                properties +
                '}';
    }
}
