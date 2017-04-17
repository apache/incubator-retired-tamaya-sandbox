package org.apache.tamaya.metamodel;/*
 * (C) Copyright 2015-2017 Trivadis AG. All rights reserved.
 */

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spi.PropertyValueBuilder;

import java.util.Map;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class HideFilter implements PropertyFilter{

    private String matches;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class HideFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "Hide";
        }

        @Override
        public PropertyFilter create(Map<String,String> parameters) {
            return new HideFilter();
        }

        @Override
        public Class<? extends PropertyFilter> getType() {
            return PropertyFilter.class;
        }
    }

    public String getMatches() {
        return matches;
    }

    public HideFilter setMatches(String matches) {
        this.matches = matches;
        return this;
    }

    @Override
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        if(matches !=null){
            if(value.getKey().matches(matches)){
                return null;
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "HideFilter{" +
                "matches='" + matches + '\'' +
                '}';
    }
}
