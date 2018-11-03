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

import java.util.Map;

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

/**
 * Simple filter that never changes a key/createValue pair returned, regardless if a createValue
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
