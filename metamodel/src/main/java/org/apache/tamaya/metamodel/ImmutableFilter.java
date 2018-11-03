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

/**
 * Simple filter that never changes a key/createValue pair returned, regardless if a createValue
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
    public PropertyValue filterProperty(PropertyValue value) {
        String key = value.getKey();
        FilterContext context = FilterContext.get();
        if(context!=null && !context.isSinglePropertyScoped()) {
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
