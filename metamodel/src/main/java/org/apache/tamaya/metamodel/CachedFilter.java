package org.apache.tamaya.metamodel;/*
 * (C) Copyright 2015-2017 Trivadis AG. All rights reserved.
 */

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class CachedFilter implements PropertyFilter{

    private String matches;
    private Map<String, CachedEntry> cachedEntries = new ConcurrentHashMap<>();
    private int maxSize = -1;
    private long timeout = TimeUnit.MINUTES.toMillis(5);

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class CachedFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "Cached";
        }

        @Override
        public PropertyFilter create(Map<String,String> parameters) {
            return new CachedFilter();
        }

        @Override
        public Class<? extends PropertyFilter> getType() {
            return PropertyFilter.class;
        }
    }

    public String getMatches() {
        return matches;
    }

    public CachedFilter setMatches(String matches) {
        this.matches = matches;
        return this;
    }

    @Override
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        if(matches !=null){
            if(value.getKey().matches(matches)){
                return resolveCachedEntry(value);
            }
        }
        return value;
    }

    /**
     * Method checks for a cached value. if present and valid the cached value is returned.
     * If not valid the cached entry is removed/updated.
     * @param value
     * @return
     */
    private PropertyValue resolveCachedEntry(PropertyValue value) {
        if(maxSize>0 && maxSize<=this.cachedEntries.size()){
            return value;
        }
        CachedEntry ce = cachedEntries.get(value.getKey());
        if(ce==null || !ce.isValid()){
            if(value!=null) {
                ce = new CachedEntry(value, System.currentTimeMillis() + timeout);
                this.cachedEntries.put(value.getKey(), ce);
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "CachedFilter{" +
                "matches='" + matches + '\'' +
                ", cache-size=" + cachedEntries.size() +
                ", max-size=" + maxSize +
                ", timeout=" + timeout +
                '}';
    }

    private static final class CachedEntry{
        long ttl;
        PropertyValue value;

        public CachedEntry (PropertyValue value, long ttl){
            this.ttl = ttl;
            this.value = value;
        }

        public boolean isValid(){
            return System.currentTimeMillis() > ttl;
        }
    }
}
