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

import org.apache.tamaya.base.DefaultConfigBuilder;
import org.apache.tamaya.base.configsource.BaseConfigSource;
import org.apache.tamaya.base.configsource.ConfigSourceComparator;
import org.apache.tamaya.spi.*;
import javax.config.Config;

import javax.config.spi.ConfigSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Property source that allows filtering on property source level. This class is thread-safe, accesses using or
 * changing the filter list are synchronized.
 */
public final class FilteredConfigSource extends BaseConfigSource {

    private ConfigSource wrapped;
    private List<Filter> filters = new ArrayList<>();
    private Config dummyContext = new DefaultConfigBuilder()
            .withSources(this).build();

    /**
     * Constructor used privately. Use {@link #of(ConfigSource)} for making a {@link PropertySource} filterable.
     * @param propertySource the property source to be filtered.
     */
    private FilteredConfigSource(ConfigSource propertySource){
        this.wrapped = Objects.requireNonNull(propertySource);
    }


    /**
     * Wraps a given property source.
     * @param configSource the property source to be wrapped.
     * @return a wrapped property source.
     */
    public static FilteredConfigSource of(ConfigSource configSource){
        if(configSource instanceof FilteredConfigSource){
            return (FilteredConfigSource)configSource;
        }
        return new FilteredConfigSource(configSource);
    }

    @Override
    public int getOrdinal() {
        int ordinalSet = super.getOrdinal();
        if(ordinalSet == 0){
            return ConfigSourceComparator.getOrdinal(this.wrapped);
        }
        return ordinalSet;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getValue(String key) {
        String value = wrapped.getValue(key);
        if(value != null){
            if(filters!=null){
                String filteredValue = value;
                for(Filter pf:filters){
                    filteredValue = pf.filterProperty(key, filteredValue);
                }
                if(filteredValue!=null){
                    return filteredValue;
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> props = wrapped.getProperties();
        if(!props.isEmpty()){
            Map<String, String> result = new HashMap<>();
            synchronized (filters) {
                for (Map.Entry<String,String> en : props.entrySet()) {
                    String filteredValue = en.getValue();
                    for (Filter pf : filters) {
                        filteredValue = pf.filterProperty(en.getKey(), filteredValue);
                    }
                    if (filteredValue != null) {
                        result.put(en.getKey(), filteredValue);
                    }
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    /**
     * Adds the given filters to this property source.
     * @param filter the filters, not null.
     */
    public void addFilter(Filter... filter){
        synchronized(filters){
            this.filters.addAll(Arrays.asList(filter));
        }
    }

    /**
     * Removes the given filter, if present.
     * @param filter the filter to remove, not null.
     */
    public void removeFilter(Filter filter){
        synchronized(filters){
            this.filters.remove(filter);
        }
    }

    /**
     * Removes the (first) given filter, if present.
     * @param filterClass the class of the filter to remove, not null.
     */
    public void removeFilter(Class<? extends Filter> filterClass){
        synchronized(filters){
            for(Filter f:filters){
                if(f.getClass().equals(filterClass)){
                    filters.remove(f);
                    break;
                }
            }
        }
    }

    /**
     * Access the current filters present.
     * @return a copy of the current filter list.
     */
    public List<Filter> getFilter(){
        synchronized (filters){
            return new ArrayList<>(filters);
        }
    }

    @Override
    protected String toStringValues() {
        synchronized (filters) {
            return  super.toStringValues() +
                    "  wrapped=" + wrapped + '\n' +
                    "  filters=" + this.filters + '\n';
        }
    }
}
