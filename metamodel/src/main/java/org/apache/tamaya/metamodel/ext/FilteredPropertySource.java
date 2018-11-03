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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.DefaultConfigurationBuilder;
import org.apache.tamaya.spisupport.PropertySourceComparator;
import org.apache.tamaya.spisupport.propertysource.BasePropertySource;

/**
 * Property source that allows filtering on property source level. This class is thread-safe, accesses using or
 * changing the filter createList are synchronized.
 */
public final class FilteredPropertySource extends BasePropertySource {

    private PropertySource wrapped;
    private List<PropertyFilter> filters = new ArrayList<>();
    private ConfigurationContext dummyContext = new DefaultConfigurationBuilder().addPropertySources(this).build().getContext();

    /**
     * Constructor used privately. Use {@link #of(PropertySource)} for making a {@link PropertySource} filterable.
     * @param propertySource the property source to be filtered.
     */
    private FilteredPropertySource(PropertySource propertySource){
        this.wrapped = Objects.requireNonNull(propertySource);
    }


    /**
     * Wraps a given property source.
     * @param propertySource the property source to be wrapped.
     * @return a wrapped property source.
     */
    public static FilteredPropertySource of(PropertySource propertySource){
        if(propertySource instanceof FilteredPropertySource){
            return (FilteredPropertySource)propertySource;
        }
        return new FilteredPropertySource(propertySource);
    }

    @Override
    public int getOrdinal() {
        int ordinalSet = super.getOrdinal();
        if(ordinalSet == 0){
            return PropertySourceComparator.getOrdinal(this.wrapped);
        }
        return ordinalSet;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public PropertyValue get(String key) {
        PropertyValue value = wrapped.get(key);
        if(value != null && value.getValue()!=null){
            if(filters!=null){
                try{
                    FilterContext fc = new FilterContext(value, dummyContext);
                    FilterContext.set(fc);
                    PropertyValue filteredValue = value;
                    for(PropertyFilter pf:filters){
                        filteredValue = pf.filterProperty(filteredValue);
                    }
                    if(filteredValue!=null){
                        return filteredValue;
                    }
                }finally {
                    FilterContext.reset();
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String, PropertyValue> props = wrapped.getProperties();
        if(!props.isEmpty()){
            Map<String, PropertyValue> result = new HashMap<>();
            synchronized (filters) {
                for (PropertyValue value : props.values()) {
                    try{
                        FilterContext fc = new FilterContext(value, dummyContext);
                        FilterContext.set(fc);
                        PropertyValue filteredValue = value;
                        for (PropertyFilter pf : filters) {
                            filteredValue = pf.filterProperty(filteredValue);
                        }
                        if (filteredValue != null) {
                            result.put(filteredValue.getKey(), filteredValue);
                        }
                    }finally {
                        FilterContext.reset();
                    }
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean isScannable() {
        return wrapped.isScannable();
    }

    /**
     * Adds the given filters to this property source.
     * @param filter the filters, not null.
     */
    public void addPropertyFilter(PropertyFilter... filter){
        synchronized(filters){
            this.filters.addAll(Arrays.asList(filter));
        }
    }

    /**
     * Removes the given filter, if present.
     * @param filter the filter to remove, not null.
     */
    public void removePropertyFilter(PropertyFilter filter){
        synchronized(filters){
            this.filters.remove(filter);
        }
    }

    /**
     * Removes the (first) given filter, if present.
     * @param filterClass the class of the filter to remove, not null.
     */
    public void removePropertyFilter(Class<? extends PropertyFilter> filterClass){
        synchronized(filters){
            for(PropertyFilter f:filters){
                if(f.getClass().equals(filterClass)){
                    filters.remove(f);
                    break;
                }
            }
        }
    }

    /**
     * Access the current filters present.
     * @return a copy of the current filter createList.
     */
    public List<PropertyFilter> getPropertyFilter(){
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
