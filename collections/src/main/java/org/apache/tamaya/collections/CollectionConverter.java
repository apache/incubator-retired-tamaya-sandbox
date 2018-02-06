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

import org.apache.tamaya.meta.MetaPropertyMapper;

import javax.config.spi.Converter;
import java.util.Collection;
import java.util.Collections;

/**
 *  PropertyConverter for gnerating a LIST representation of values.
 */
public class CollectionConverter implements Converter<Collection> {

    @Override
    public Collection convert(String value) {
        String collectionType = MetaPropertyMapper.getOptionalMetaEntry(
                ItemTokenizer.config(),
                ItemTokenizer.key(),
                "collection-type").orElse("List");
        if(collectionType.startsWith("java.util.")){
            collectionType = collectionType.substring("java.util.".length());
        }
        Collection result = null;
        switch(collectionType){
            case "LinkedList":
                result = LinkedListConverter.getInstance().convert(value);
                break;
            case "Set":
            case "HashSet":
                result = HashSetConverter.getInstance().convert(value);
                break;
            case "SortedSet":
            case "TreeSet":
                result = TreeSetConverter.getInstance().convert(value);
                break;
            case "List":
            case "ArrayList":
            default:
                result = ArrayListConverter.getInstance().convert(value);
                break;
        }
        if(MetaPropertyMapper.getOptionalMetaEntry(
                ItemTokenizer.config(),
                ItemTokenizer.key(),
                "read-only", boolean.class).orElse(true)){
            return Collections.unmodifiableCollection(result);
        }
        return result;
    }
}
