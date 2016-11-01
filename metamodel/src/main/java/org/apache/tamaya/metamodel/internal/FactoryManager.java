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

import org.apache.tamaya.metamodel.spi.PropertySourceFactory;
import org.apache.tamaya.metamodel.spi.PropertySourceProviderFactory;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.ServiceContextManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Small manager component to maintain the factories to be referenced by type names.
 */
final class FactoryManager {

    private static final Logger LOG = Logger.getLogger(FactoryManager.class.getName());
    private static final FactoryManager INSTANCE = new FactoryManager();

    private Map<String, PropertySourceFactory> sourceFactories = new HashMap<>();
    private Map<String, PropertySourceProviderFactory> providerFactories = new HashMap<>();

    private FactoryManager(){
        for(PropertySourceFactory f: ServiceContextManager.getServiceContext().getServices(PropertySourceFactory.class)){
            this.sourceFactories.put(f.getType(), f);
        }
        for(PropertySourceProviderFactory f: ServiceContextManager.getServiceContext().getServices(PropertySourceProviderFactory.class)){
            this.providerFactories.put(f.getType(), f);
        }
    }

    public static FactoryManager getInstance(){
        return INSTANCE;
    }

    public PropertySourceFactory getSourceFactory(String type){
        PropertySourceFactory fact = this.sourceFactories.get(type);
        if(fact==null){
            try{
                Class<? extends PropertySource> psType = (Class<? extends PropertySource>)Class.forName(type);
                fact = new ImplicitPropertySourceFactory(psType);
                this.sourceFactories.put(type, fact);
                return fact;
            }catch(Exception e){
                LOG.log(Level.SEVERE, "Failed to load PropertySourceFactory: " + type);
            }
        }
        throw new IllegalArgumentException("No such PropertySourceFactory: " + type);
    }

    public PropertySourceProviderFactory getProviderFactory(String type){
        PropertySourceProviderFactory fact = this.providerFactories.get(type);
        if(fact==null){
            try{
                Class<? extends PropertySourceProvider> psType = (Class<? extends PropertySourceProvider>)Class.forName(type);
                fact = new ImplicitPropertySourceProviderFactory(psType);
                this.providerFactories.put(type, fact);
                return fact;
            }catch(Exception e){
                LOG.log(Level.SEVERE, "Failed to load PropertySourceProviderFactory: " + type);
            }
        }
        throw new IllegalArgumentException("No such PropertySourceProviderFactory: " + type);
    }

    @Override
    public String toString() {
        return "FactoryManager{" +
                "providerFactories=" + providerFactories.keySet() +
                ", sourceFactories=" + sourceFactories.keySet() +
                '}';
    }

    private static class ImplicitPropertySourceFactory implements PropertySourceFactory{

        private Class<? extends PropertySource> type;

        ImplicitPropertySourceFactory(Class<? extends PropertySource> type){
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public PropertySource create(String config, Map<String, String> extendedConfig) {
            try {
                return this.type.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot instantiate PropertySource: " + this.type.getName(), e);
            }
        }

        @Override
        public String getType() {
            return null;
        }
    }

    private static class ImplicitPropertySourceProviderFactory implements PropertySourceProviderFactory{

        private Class<? extends PropertySourceProvider> type;

        ImplicitPropertySourceProviderFactory(Class<? extends PropertySourceProvider> type){
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public PropertySourceProvider create(String config, Map<String, String> extendedConfig) {
            try {
                return this.type.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot instantiate PropertySourceProvider: " + this.type.getName(), e);
            }
        }

        @Override
        public String getType() {
            return null;
        }
    }
}
