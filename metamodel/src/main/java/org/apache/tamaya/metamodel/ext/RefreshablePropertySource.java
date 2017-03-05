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
package org.apache.tamaya.metamodel.ext;

import org.apache.tamaya.metamodel.Refreshable;
import org.apache.tamaya.metamodel.internal.ComponentConfigurator;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.PropertySourceComparator;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Wrapped property source that allows refreshing/reloading a property source. Hereby a property source must
 * either
 * <ul>
 *     <li>have a public parameterless constructor, used for reloading a new instance.</li>
 *     <li>implement itself {@link Refreshable}.</li>
 * </ul>
 */
public final class RefreshablePropertySource
        implements PropertySource, Refreshable {

    private static final Logger LOG = Logger.getLogger(RefreshablePropertySource.class.getName());
    private Map<String,String> metaConfig;
    private PropertySource wrapped;
    private AtomicLong nextRefresh = new AtomicLong();
    private AtomicLong refreshPeriod = new AtomicLong();

    private RefreshablePropertySource(Map<String,String> metaConfig, PropertySource wrapped) {
        this.metaConfig = Objects.requireNonNull(metaConfig);
        this.wrapped = Objects.requireNonNull(wrapped);
    }

    /**
     * Makes a property source refreshable. If the given property source is already an instance of
     * RefreshablePropertySource, the property source is returned.
     * @param metaConfig the configuration parameters to be applied when a new PropertySource is created, not null.
     * @param propertySource the property source, not null.
     * @return a new instance, not null.
     */
    public static RefreshablePropertySource of(Map<String,String> metaConfig, PropertySource propertySource) {
        if(propertySource instanceof RefreshablePropertySource){
            return (RefreshablePropertySource)propertySource;
        }
        return new RefreshablePropertySource(metaConfig, propertySource);
    }

    /**
     * Makes a property source refreshable. If the given property source is already an instance of
     * RefreshablePropertySource, the property source is returned.
     * @param propertySource the property source, not null.
     * @return a new instance, not null.
     */
    public static RefreshablePropertySource of(PropertySource propertySource) {
        return of(Collections.<String, String>emptyMap(), propertySource);
    }

    /**
     * Checks if the property source should autorefresh, if so {@link #refresh()} is called.
     */
    private void checkRefresh(){
        long next = nextRefresh.get();
        if(next > 0 && next<System.currentTimeMillis()){
            nextRefresh.set(next + refreshPeriod.get());
            refresh();
        }
    }

    /**
     * Set the refresh period. This will be immedately applied from now. No explicit
     * refresh will be triggered now.
     * @param units
     * @param timeUnit
     */
    public void setRefreshPeriod(long units, TimeUnit timeUnit){
        this.refreshPeriod.set(timeUnit.toMillis(units));
        this.nextRefresh.set(System.currentTimeMillis() + this.refreshPeriod.get());
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
            LOG.log(Level.WARNING, "Failed to reload/refresh PropertySource: " +
                    wrapped.getClass().getName(), e);
        }
    }

    @Override
    public int getOrdinal() {
        return PropertySourceComparator.getOrdinal(this.wrapped);
    }

    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    @Override
    public PropertyValue get(String key) {
        return this.wrapped.get(key);
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        return this.wrapped.getProperties();
    }

    @Override
    public boolean isScannable() {
        return this.wrapped.isScannable();
    }

    @Override
    public String toString() {
        return "RefreshablePropertySource{" +
                "\n metaConfig=" + metaConfig +
                "\n wrapped=" + wrapped +
                '}';
    }
}
