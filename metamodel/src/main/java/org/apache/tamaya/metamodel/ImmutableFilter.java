package org.apache.tamaya.metamodel;/*
 * (C) Copyright 2015-2017 Trivadis AG. All rights reserved.
 */

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class ImmutableFilter implements PropertyFilter{

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class ImmutableFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "Immutable";
        }

        @Override
        public PropertyFilter create(Map<String,String> parameters) {
            return new ImmutableFilter();
        }

        @Override
        public Class<? extends PropertyFilter> getType() {
            return PropertyFilter.class;
        }
    }

    private Map<String,PropertyValue> map = new ConcurrentHashMap<>();

    @Override
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        String key = value.getKey();
        if(!context.isSinglePropertyScoped()) {
            key = value.getKey() + "_all";
        }
        PropertyValue val = map.get(key);
        if(val==null){
            map.put(key, value);
            val = value;
        }
        return val;
    }
}
