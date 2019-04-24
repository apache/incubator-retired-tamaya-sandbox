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

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple filter that never changes a key/createValue pair returned, regardless if a createValue
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
     * Method checks for a cached createValue. if present and valid the cached createValue is returned.
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
                ", cache-getNumChilds=" + cachedEntries.size() +
                ", max-getNumChilds=" + maxSize +
                ", timeout=" + timeout +
                '}';
    }

    /**
     * A cached configuration entry.
     */
    private static final class CachedEntry{
        private long ttl;
        private PropertyValue value;

        public CachedEntry (PropertyValue value, long ttl){
            this.ttl = ttl;
            this.value = value;
        }

        public boolean isValid(){
            return System.currentTimeMillis() > ttl;
        }
    }
}
