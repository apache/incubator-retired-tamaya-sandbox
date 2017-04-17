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
public class MapFilter implements PropertyFilter{

    private String target;
    private String cutoff;
    private String matches;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class MapFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "Map";
        }

        @Override
        public PropertyFilter create(Map<String,String> parameters) {
            return new MapFilter();
        }

        @Override
        public Class<? extends PropertyFilter> getType() {
            return PropertyFilter.class;
        }
    }

    public String getTarget() {
        return target;
    }

    public MapFilter setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getCutoff() {
        return cutoff;
    }

    public MapFilter setCutoff(String cutoff) {
        this.cutoff = cutoff;
        return this;
    }

    public String getMatches() {
        return matches;
    }

    public MapFilter setMatches(String matches) {
        this.matches = matches;
        return this;
    }

    @Override
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        PropertyValueBuilder b = value.toBuilder();
        String key = value.getKey();
        if(matches !=null){
            if(!value.getKey().matches(matches)){
                return value;
            }
        }
        if(cutoff !=null){
            if(value.getKey().startsWith(cutoff)){
                key = key.substring(cutoff.length());
                b.setKey(key);
            }
        }
        if(target!=null){
            key = target+key;
            b.setKey(key);
        }
        return b.build();
    }

    @Override
    public String toString() {
        return "MapFilter{" +
                "target='" + target + '\'' +
                ", cutoff='" + cutoff + '\'' +
                ", matches='" + matches + '\'' +
                '}';
    }
}
