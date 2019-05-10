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

import org.apache.tamaya.metamodel.spi.ContextInitializer;
import org.apache.tamaya.spi.ServiceContextManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class managing a configuration system's meta-context. This
 * context is used by the configuration system to evaluate the
 * right properties, e.g. by defining the current stage or labels
 * that apply to the current configuration.
 */
public final class MetaContext {

    private static final Logger LOG = Logger.getLogger(MetaContext.class.getName());

    private static final MetaContext INSTANCE = new MetaContext();

    private final Map<String,Object> properties = new ConcurrentHashMap<>();

    /** The unique id of this context. */
    public MetaContext(){

        setStringProperty("_id", UUID.randomUUID().toString());
        initialize();
    }

    /**
     * Get the current metacontext.
     * @return the meta-context, never null.
     */
    public static MetaContext getInstance() {
        return INSTANCE;
    }

    /**
     * Get the context's id.
     * @return the context's id
     */
    public String getId() {
        return getStringProperty("_id").orElse("N/A");
    }

    /**
     * Reads and applies the {@link ContextInitializer}s using the default classloader..
     */
    public void initialize(){
        initialize(ServiceContextManager.getDefaultClassLoader());
    }

    /**
     * Reads and applies the {@link ContextInitializer}s for the given class loader.
     * @param classLoader the target classloader, never null.
     */
    public void initialize(ClassLoader classLoader){
        for(ContextInitializer initializer: ServiceContextManager.getServiceContext(classLoader)
        .getServices(ContextInitializer.class)){
            try{
                initializer.initializeContext(this);
            }catch(Exception e){
                LOG.log(Level.WARNING, "ContextInitializer failed: " + initializer.getClass().getName(), e);
            }
        }
    }

    /**
     * Combine this context with the other contexts given.
     * @param baseContext the base context with which the other contexts will be merged
     * @param contexts the context to merge with this context.
     * @return the newly created Context.
     */
    public static MetaContext combineWith(MetaContext baseContext, MetaContext... contexts) {
        MetaContext newContext = new MetaContext();
        newContext.properties.putAll(baseContext.properties);
        for(MetaContext ctx:contexts) {
            newContext.properties.putAll(ctx.properties);
        }
        return newContext;
    }

    /**
     * Access the given context property.
     * @param key the key, not null
     * @return the createValue, or null.
     */
    public Optional<String> getStringProperty(String key){
        return getProperty(key, String.class);
    }

    /**
     * Access the given context property.
     * @param key the key, not null
     * @return the createValue, or null.
     */
    public Optional<Boolean> getBooleanProperty(String key){
        return getProperty(key, Boolean.class);
    }

    /**
     * Access the given context property.
     * @param key the key, not null
     * @return the createValue, or null.
     */
    public Optional<Number> getNumberProperty(String key){
        return getProperty(key, Number.class);
    }

    /**
     * Access the given context property.
     * @param key the key, not null
     * @return the createValue, or null.
     */
    public <T> Optional<T> getProperty(String key, Class<T> type){
        T value = (T)this.properties.get(key);
        return Optional.ofNullable(value);
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the createValue, not null.
     * @return the previous createValue, or null.
     */
    public String setStringProperty(String key, String value){
        return setProperty(key, String.class, value);
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the createValue, not null.
     * @return the previous createValue, or null.
     */
    public Boolean setBooleanProperty(String key, Boolean value){
        return setProperty(key, Boolean.class, value);
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the createValue, not null.
     * @return the previous createValue, or null.
     */
    public Number setNumberProperty(String key, Number value){
        return setProperty(key, Number.class, value);
    }

    /**
     * Sets the given context property.
     * @param key the key, not null.
     * @param value the createValue, not null.
     * @return the previous createValue, or null.
     */
    public <T> T setProperty(String key, Class<T> type, T value){
        T previous = (T)this.properties.put(key, Objects.requireNonNull(value));
        if(previous!=null){
            return previous;
        }
        return null;
    }

    /**
     * Sets the given property unless there is already a createValue defined.
     * @param key the key, not null.
     * @param value the createValue, not null.
     */
    public <T> T setPropertyIfAbsent(String key, Class<T> type, T value){
        T prev = (T)this.properties.get(key);
        if(prev==null){
            this.properties.put(key, value);
            return prev;
        }
        return null;
    }


    /**
     * Checks if all the given properties are present.
     * @param keys the keys to check, not null.
     * @return true, if all the given keys are existing.
     */
    public boolean checkPropertiesArePresent(String... keys){
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
    public Map<String,Object> getProperties(){
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetaContext)) {
            return false;
        }

        MetaContext context = (MetaContext) o;

        return getProperties().equals(context.getProperties());
    }

    @Override
    public int hashCode() {
        return getProperties().hashCode();
    }

    @Override
    public String toString() {
        return "MetaContext{" +
                "id=" + getId() +
                ", properties=" + properties +
                '}';
    }

}
