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
package org.apache.tamaya.metamodel.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tamaya.spi.ServiceContextManager;

/**
 * Created by atsticks on 04.12.16.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ItemFactoryManager {

    private static final Logger LOG = Logger.getLogger(ItemFactoryManager.class.getName());

	private Map<Class, List<ItemFactory<?>>> factoryRegistry = new ConcurrentHashMap<>();

    private static ItemFactoryManager INSTANCE = new ItemFactoryManager();

    private ItemFactoryManager(){
    }

    public static ItemFactoryManager getInstance(){
        return INSTANCE;
    }

	public <T> List<ItemFactory<T>> getFactories(Class<T> type){
        List<ItemFactory<?>> factories = factoryRegistry.get(type);
        if(factories==null){
            Collection<ItemFactory> allFactories =
                    ServiceContextManager.getServiceContext().getServices(ItemFactory.class);
            for(ItemFactory fact:allFactories){
                registerItemFactory(fact);
            }
        }
        factories = factoryRegistry.get(type);
        if(factories==null){
            return Collections.emptyList();
        }
        return List.class.cast(factories);
    }

    public <T> ItemFactory<T> getFactory(Class<T> type, String id) {
        List<ItemFactory<T>> factories = getFactories(type);
        for(ItemFactory<T> f:factories){
            if(id.equals(f.getName())){
                return f;
            }
        }
        // try creating a new factory with the given id as fully qualified class name...
        try{
            Class<? extends ItemFactory> instanceType = (Class<? extends ItemFactory>) Class.forName(id);
            ItemFactory<T> factory = new SimpleItemFactory<T>(type, instanceType);
            registerItemFactory(factory);
            return factory;
        }catch(Exception e){
            LOG.severe("Failed to create factory for configured class: " + type.getName() +
                    " and type: " + id);
            return null;
        }
    }

    public <T> void registerItemFactory(ItemFactory<T> factory) {
        List<ItemFactory<?>> factories = factoryRegistry.get(factory.getType());
        if(factories==null){
            factories = new ArrayList<>();
            factoryRegistry.put(factory.getType(), factories);
        }
        factories.add(factory);
    }

    private static class SimpleItemFactory<I> implements ItemFactory<I> {

        private Class<I> type;
        private Class<? extends I> instanceType;

        public SimpleItemFactory(Class<I> type, Class<? extends I> instanceType) {
            this.type = Objects.requireNonNull(type);
            this.instanceType = Objects.requireNonNull(instanceType);
        }

        @Override
        public String getName() {
            return getType().getName();
        }

        @Override
        public I create(Map<String, String> parameters) {
            try {
                return instanceType.newInstance();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to create configured instance of type:" + instanceType, e);
                return null;
            }
        }

        @Override
        public Class<I> getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleItemFactory)) return false;
            SimpleItemFactory<?> that = (SimpleItemFactory<?>) o;
            return Objects.equals(getType(), that.getType()) &&
                    Objects.equals(instanceType, that.instanceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getType(), instanceType);
        }

        @Override
        public String toString() {
            return "SimpleItemFactory{" +
                    "type=" + type +
                    ", instanceType=" + instanceType +
                    '}';
        }
    }
}
