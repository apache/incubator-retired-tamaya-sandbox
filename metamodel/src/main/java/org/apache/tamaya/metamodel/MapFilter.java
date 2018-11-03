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

/**
 * Simple filter that never changes a key/createValue pair returned, regardless if a createValue
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
        value = value.mutable();
        String key = value.getKey();
        if(matches !=null){
            if(!value.getKey().matches(matches)){
                return value;
            }
        }
        if(cutoff !=null){
            if(value.getKey().startsWith(cutoff)){
                key = key.substring(cutoff.length());
                value.setKey(key);
            }
        }
        if(target!=null){
            key = target+key;
            value.setKey(key);
        }
        return value;
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
