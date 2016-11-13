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

    private static final ThreadLocal<Context> THREAD_CONTEXT = new ThreadLocal<Context>(){
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };

    private final Map<String,String> properties = new ConcurrentHashMap<>();

    /**
     * Access the thread-based context. If no such context
     * exists a new one will be created.
     * @return the corresponding context, never null.
     */
    public static Context getThreadInstance(){
        return THREAD_CONTEXT.get();
    }

    /**
     * Access the current context, which actually is the current context, combined with the thread based
     * context (overriding).
     * @return the corresponding context, never null.
     */
    public Context getCurrentInstance(){
        return this.combineWith(THREAD_CONTEXT.get());
    }

    /**
     * Combine this context with the other contexts given.
     * @param contexts the context to merge with this context.
     * @return the newly created Context.
     */
    public Context combineWith(Context... contexts) {
        Context newContext = new Context();
        newContext.properties.putAll(getProperties());
        for(Context ctx:contexts) {
            newContext.properties.putAll(ctx.getProperties());
        }
        return newContext;
    }

    /**
     * Access the given context property.
     * @param key the key, not null
     * @return the value, or null.
     */
    public String getProperty(String key){
        return getProperty(key, null);
    }

    /**
     * Access the given context property.
     * @param key the key, not the default value.
     * @param defaultValue the default value to be returned, if no value is defined.
     * @return the value, default value or null.
     */
    public String getProperty(String key, String defaultValue){
        String value = this.properties.get(key);
        if(value==null){
            return defaultValue;
        }
        return value;
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the value, not null.
     * @return the porevious value, or null.
     */
    public String setProperty(String key, String value){
       return this.properties.put(key,value);
    }

    /**
     * Sets the given property unless there is already a value defined.
     * @param key the key, not null.
     * @param value the value, not null.
     */
    public void setPropertyIfAbsent(String key, String value){
        String prev = this.properties.get(key);
        if(prev==null){
            this.properties.put(key, prev);
        }
    }

    /**
     * Adds all properties given, overriding any existing properties.
     * @param properties the properties, not null.
     */
    public void setProperties(Map<String,String> properties){
        this.properties.putAll(properties);
    }

    /**
     * Checks if all the given properties are present.
     * @param keys the keys to check, not null.
     * @return true, if all the given keys are existing.
     */
    public boolean checkProperties(String... keys){
        for(String key:keys) {
            if (getProperty(key, null) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Access all the current context properties.
     * @return the properties, never null.
     */
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
