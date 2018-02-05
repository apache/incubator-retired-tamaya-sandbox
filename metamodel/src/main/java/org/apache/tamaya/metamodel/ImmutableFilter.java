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
import org.apache.tamaya.base.filter.Filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class ImmutableFilter implements Filter{

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class ImmutableFilterFactory implements ItemFactory<Filter> {
        @Override
        public String getName() {
            return "Immutable";
        }

        @Override
        public Filter create(Map<String,String> parameters) {
            return new ImmutableFilter();
        }

        @Override
        public Class<? extends Filter> getType() {
            return Filter.class;
        }
    }

    private Map<String,String> map = new ConcurrentHashMap<>();

    @Override
    public String filterProperty(String key, String value) {
//        if(!context.isSinglePropertyScoped()) {
//            key = key + "_all";
//        }
        String val = map.get(key);
        if(val==null){
            map.put(key, value);
            val = value;
        }
        return val;
    }
}
