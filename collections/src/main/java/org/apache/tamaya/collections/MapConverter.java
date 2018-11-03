/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.collections;

import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.ConversionContext;

import java.util.Collections;
import java.util.Map;

/**
 *  PropertyConverter for gnerating HashMap representation of a values.
 */
public class MapConverter implements PropertyConverter<Map> {

    @Override
    public Map convert(String value) {
        ConversionContext context = ConversionContext.current();
        String collectionType = "HashMap";
        boolean readOnly = false;
        if(context!=null) {
            collectionType = (String)context.getMeta().getOrDefault("collection-type", "HashMap");
            if (collectionType.startsWith("java.util.")) {
                collectionType = collectionType.substring("java.util.".length());
            }
            readOnly = Boolean.parseBoolean((String)context.getMeta().getOrDefault("read-only", "false"));
        }
        Map result = null;
        switch(collectionType){
            case "TreeMap":
            case "SortedMap":
                result = TreeMapConverter.getInstance().convert(value);
                break;
            case "ConcurrentHashMap":
                result = ConcurrentHashMapConverter.getInstance().convert(value);
                break;
            case "Map":
            case "HashMap":
            default:
                result = HashMapConverter.getInstance().convert(value);
                break;
        }
        if(readOnly){
            return Collections.unmodifiableMap(result);
        }
        return result;
    }
}
