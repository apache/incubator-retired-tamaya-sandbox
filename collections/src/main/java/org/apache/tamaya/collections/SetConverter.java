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
import java.util.Set;

/**
 *  PropertyConverter for gnerating a LIST representation of values.
 */
public class SetConverter implements PropertyConverter<Set> {

    @Override
    public Set convert(String value) {
        ConversionContext context = ConversionContext.current();
        String collectionType = "Set";
        boolean readOnly = false;
        if(context!=null) {
            collectionType = (String)context.getMeta().getOrDefault("collection-type", "HashSet");
            if (collectionType.startsWith("java.util.")) {
                collectionType = collectionType.substring("java.util.".length());
            }
            readOnly = Boolean.parseBoolean((String)context.getMeta().getOrDefault("read-only", "false"));
        }
        Set result;
        switch(collectionType){
            case "TreeSet":
                result = TreeSetConverter.getInstance().convert(value);
                break;
            case "Set":
            case "HashSet":
            default:
                result = HashSetConverter.getInstance().convert(value);
                break;
        }
        if(readOnly){
            return Collections.unmodifiableSet(result);
        }
        return result;
    }
}
