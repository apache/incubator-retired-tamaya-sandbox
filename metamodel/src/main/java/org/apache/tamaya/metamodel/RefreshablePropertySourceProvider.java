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

import org.apache.tamaya.metamodel.internal.ComponentConfigurator;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapped property source provider that allows refreshing/reloading a property source provider. Hereby a provider must
 * either
 * <ul>
 *     <li>have a public parameterless constructor, used for reloading a new instance.</li>
 *     <li>implement itself {@link Refreshable}.</li>
 * </ul>
 */
public final class RefreshablePropertySourceProvider
        implements PropertySourceProvider, Refreshable {

    private static final Logger LOG = Logger.getLogger(RefreshablePropertySourceProvider.class.getName());
    private Map<String,String> metaConfig;
    private PropertySourceProvider wrapped;
    private Collection<PropertySource> propertSources;

    private RefreshablePropertySourceProvider(Map<String,String> metaConfig, PropertySourceProvider wrapped) {
        this.metaConfig = Objects.requireNonNull(metaConfig);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.propertSources = Objects.requireNonNull(wrapped.getPropertySources());
    }

    /**
     * Makes a property source provider refreshable. If the given property source provider is already an instance of
     * RefreshablePropertySourceProvider, the property source provider is returned unchanged.
     * @param metaConfig the configuration parameters to be applied when a new PropertySourceProvider is created, not null.
     * @param provider the property source provider, not null.
     * @return a new instance, not null.
     */
    public static RefreshablePropertySourceProvider of(Map<String,String> metaConfig, PropertySourceProvider provider) {
        if(provider instanceof RefreshablePropertySourceProvider){
            return (RefreshablePropertySourceProvider)provider;
        }
        return new RefreshablePropertySourceProvider(metaConfig, provider);
    }

    /**
     * Makes a property source refreshable. If the given property source is already an instance of
     * RefreshablePropertySource, the property source is returned.
     * @param provider the property source provider, not null.
     * @return a new instance, not null.
     */
    public static RefreshablePropertySourceProvider of(PropertySourceProvider provider) {
        return of(Collections.<String, String>emptyMap(), provider);
    }

    @Override
    public Collection<PropertySource> getPropertySources() {
        return this.propertSources;
    }

    @Override
    public void refresh() {
        try {
            if(this.wrapped instanceof Refreshable){
                ((Refreshable) this.wrapped).refresh();
            }else {
                this.wrapped = this.wrapped.getClass().newInstance();
                ComponentConfigurator.configure(this.wrapped, metaConfig);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to refresh PropertySourceProvider: " +
                    wrapped.getClass().getName(), e);
        }
        this.propertSources = Objects.requireNonNull(wrapped.getPropertySources());
    }

    @Override
    public String toString() {
        return "RefreshablePropertySourceProvider{" +
                "\n metaConfig=" + metaConfig +
                "\n wrapped=" + wrapped +
                '}';
    }
}
