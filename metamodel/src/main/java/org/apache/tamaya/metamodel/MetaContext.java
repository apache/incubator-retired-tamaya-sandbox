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

import org.apache.tamaya.functions.Supplier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Class managing the configuration system's shared context. This
 * context is used by the configuration system to evaluate the
 * right properties, e.g. by defining the current stage or labels
 * that apply to the current configuration. Hereby context are
 * <ul>
 *     <li>identified by unique names</li>
 *     <li>stackable into a context hierarchy, see {@link #combineWith(MetaContext...)}</li>
 *     <li>optionally be valid only in some contexts or for a limited time, see {@link #isValid()},
 *     {@link #getInstance(String, Supplier)}</li>
 *     <li>providing key/values only valid for a certain time (assigned a TTL), see {@link #setProperty(String, String, int, TimeUnit)},
 *     {@link #setProperties(Map, long, TimeUnit)}</li>
 * </ul>
 * Additionally there is special support for thread related contexts, see {@link #getThreadInstance()}.
 * Finally there is also one special globally shared context instance, see {@link #getCurrentInstance()}.
 */
public final class MetaContext {

    private static final ThreadLocal<MetaContext> THREAD_CONTEXT = new ThreadLocal<MetaContext>(){
        @Override
        protected MetaContext initialValue() {
            return new MetaContext();
        }
    };
    public static final String DEFAULT_CONTEXT_NAME = "<DEFAULT>";

    private String id;

    private Supplier<Boolean> validSupplier;

    private final Map<String,Value> properties = new ConcurrentHashMap<>();

    private static final Map<String,MetaContext> CONTEXTS = new WeakHashMap<>();

    /**
     * Access a context by name. Contexts are managed as weak references in this class. If no
     * such context exists, a new instance is created.
     * @param contextName the context name, not null.
     * @return the context instance, never null.
     */
    public static MetaContext getInstance(String contextName) {
        return getInstance(contextName, null);
    }

    /**
     * Access the default context. Contexts are managed as weak references in this class. If no
     * such context exists, a new instance is created.
     * @return the context instance, never null.
     */
    public static MetaContext getDefaultInstance(){
        return getInstance(DEFAULT_CONTEXT_NAME);
    }

    /**
     * Access a context by name. Contexts are managed as weak references in this class. If no
     * such valid context exists, a new instance is created, using the given {@code validSupplier}.
     * @param contextName the context name, not null.
     * @return the context instance, never null.
     */
    public static MetaContext getInstance(String contextName, Supplier<Boolean> validSupplier){
        synchronized(CONTEXTS){
            MetaContext ctx = CONTEXTS.get(contextName);
            if(ctx!=null && ctx.isValid()){
                return ctx;
            }
            ctx = new MetaContext();
            ctx.id = Objects.requireNonNull(contextName);
            ctx.validSupplier = validSupplier;
            CONTEXTS.put(contextName, ctx);
            return ctx;
        }

    }

    /**
     * Access the thread-based context. If no such context
     * exists a new one will be created.
     * @return the corresponding context, never null.
     */
    public static MetaContext getThreadInstance(){
        return THREAD_CONTEXT.get();
    }

    /**
     * Access the current context, which actually is the current context, combined with the thread based
     * context (overriding).
     * @return the corresponding context, never null.
     */
    public MetaContext getCurrentInstance(){
        return this.combineWith(THREAD_CONTEXT.get());
    }

    /**
     * Method to evaluate if a context is valid. This basically depends on the
     * {@code validSupplier}, if any is set. If no supplier is present the context is valid.
     *
     * @return true, if this context is valid.
     */
    public boolean isValid(){
        return this.validSupplier == null || validSupplier.get();
    }


    /**
     * Combine this context with the other contexts given, hereby only contexts are included
     * which are {@code valid}, see {@link #isValid()}.
     * @param contexts the context to merge with this context.
     * @return the newly created Context.
     */
    public MetaContext combineWith(MetaContext... contexts) {
        MetaContext newContext = new MetaContext();
        newContext.properties.putAll(this.properties);
        for(MetaContext ctx:contexts) {
            if(ctx.isValid()) {
                newContext.properties.putAll(ctx.properties);
            }
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
     * @param defaultValue the default value to be returned, if no value is defined, or the
     *                     stored value's TTL has been reached.
     * @return the value, default value or null.
     */
    public String getProperty(String key, String defaultValue){
        Value value = this.properties.get(key);
        if(value==null){
            return defaultValue;
        }
        if(!value.isValid()){
            this.properties.remove(key);
            return null;
        }
        return value.value;
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the value, not null.
     * @return the porevious value, or null.
     */
    public String setProperty(String key, String value){
       return setProperty(key, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the value, not null.
     * @param ttl the time to live. Zero or less than zero means, no timeout.
     * @param unit the target time unit.
     * @return the porevious value, or null.
     */
    public String setProperty(String key, String value, int ttl, TimeUnit unit){
        Value previous = this.properties.put(key, new Value(key, value, ttl));
        if(previous!=null && previous.isValid()){
            return previous.value;
        }
        return null;
    }

    /**
     * Sets the given property unless there is already a value defined.
     * @param key the key, not null.
     * @param value the value, not null.
     */
    public void setPropertyIfAbsent(String key, String value){
        setPropertyIfAbsent(key, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the given property unless there is already a value defined.
     * @param key the key, not null.
     * @param value the value, not null.
     * @param ttl the time to live. Zero or less than zero means, no timeout.
     * @param unit the target time unit.
     */
    public void setPropertyIfAbsent(String key, String value, long ttl, TimeUnit unit){
        Value prev = this.properties.get(key);
        if(prev==null){
            this.properties.put(key, new Value(key, value, unit.toMillis(ttl)));
        }
    }

    /**
     * Adds all properties given, overriding any existing properties.
     * @param properties the properties, not null.
     */
    public void setProperties(Map<String,String> properties){
        setProperties(properties, 0L, TimeUnit.MILLISECONDS);
    }

    /**
     * Adds all properties given, overriding any existing properties.
     * @param properties the properties, not null.
     * @param ttl the time to live. Zero or less than zero means, no timeout.
     * @param unit the target time unit.
     */
    public void setProperties(Map<String,String> properties, long ttl, TimeUnit unit){
        for(Map.Entry en:properties.entrySet()) {
            this.properties.put(
                    en.getKey().toString(),
                    new Value(en.getKey().toString(), en.getValue().toString(), unit.toMillis(ttl)));
        }
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
        Map<String,String> map = new HashMap<>();
        for(Map.Entry<String,Value> en:properties.entrySet()) {
            Value val = en.getValue();
            if(val.isValid()){
                map.put(en.getKey(), val.value);
            }else{
                this.properties.remove(en.getKey());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaContext)) return false;

        MetaContext context = (MetaContext) o;

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

    private static final class Value{
        String key;
        String value;
        long validUntil;

        Value(String key, String value, long ttl){
            this.key = Objects.requireNonNull(key);
            this.value = Objects.requireNonNull(value);
            if(ttl>0) {
                this.validUntil = System.currentTimeMillis() + ttl;
            }
        }

        /** Method to check if a value is still valid. */
        boolean isValid(){
            return this.validUntil<=0 || this.validUntil>=System.currentTimeMillis();
        }

        /** Method that invalidates a value. */
        void invalidate(){
            this.validUntil = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Value)) return false;
            Value value = (Value) o;
            return Objects.equals(value, value.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
