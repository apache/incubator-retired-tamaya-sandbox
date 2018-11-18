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
package org.apache.tamaya.jsr382;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.events.FrozenConfiguration;

import javax.config.Config;
import javax.config.ConfigAccessor;
import javax.config.ConfigSnapshot;
import javax.config.spi.ConfigSource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * JavaConfig {@link javax.config.Config} implementation that wraps a Tamaya {@link Configuration} instance.
 */
public class JavaConfigAdapter implements Config, Serializable {

    private Configuration delegate;

    /**
     * Creates a new JSR configuration instance based on the given Tamaya configuration.
     * @param delegate the configuration, not null.s
     */
    public JavaConfigAdapter(Configuration delegate){
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * Access the current configuration delegate.
     * @return the Tamaya configuration delegate, never null.
     */
    public Configuration getConfiguration(){
        return this.delegate;
    }


    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        T value = null;
        try{
            value = delegate.get(propertyName, propertyType);
        }catch(ConfigException e){
            if(e.toString().contains("Unparseable")){
                throw new IllegalArgumentException("Invalid type: " + propertyType.getName());
            }
        }
        if(value == null){
            throw new NoSuchElementException("No such config property: " + propertyName);
        }
        return value;
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        return delegate.getOptional(propertyName, propertyType);
    }

    @Override
    public ConfigAccessor<String> access(String name) {
        return new TamayaConfigAccessor(this, name);
    }

    @Override
    public ConfigSnapshot snapshotFor(ConfigAccessor<?>... configAccessors) {
        return new TamayaConfigSnapshot(this.delegate, configAccessors);
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return delegate.getProperties().keySet();
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return JavaConfigAdapterFactory.toConfigSources(delegate.getContext().getPropertySources());
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        if(!(this.delegate instanceof Serializable)){
            out.writeObject(this.delegate.getSnapshot());
        }else {
            out.writeObject(this.delegate);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        this.delegate = (Configuration)in.readObject();
    }

    @Override
    public String toString() {
        return "Tamaya Config{" +
                "delegate=" + delegate +
                '}';
    }

}
