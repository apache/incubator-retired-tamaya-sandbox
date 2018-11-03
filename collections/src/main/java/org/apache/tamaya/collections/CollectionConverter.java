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

import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyValue;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 *  PropertyConverter for gnerating a LIST representation of values.
 */
public final class CollectionConverter implements PropertyConverter<Collection> {

    private static final Logger LOG = Logger.getLogger(CollectionConverter.class.getName());

    private enum MappingType{
        /** The list values are identiified by parsing the node value(s) into items.
         * Hereby only the items of the most significant config entry are considered. */
        value,
        /** The list values are identiified by parsing the node value(s) into items, hereby
         * the items of all values are combined. */
        value_all,
        /** The list values are identiified by using the node's child value(s) as items.
         * Hereby only the items of the most significant config entry are considered.*/
        node,
        /** The list values are identiified by using the node's child value(s) as items. Hereby
         * the items of all values are combined. */
        node_all
    }

    private MappingType mappingType = MappingType.value_all;

    public static <T extends Collection> T convertList(ConversionContext context,
                                                       Supplier<T> collectionSupplier) {
        MappingType mappingType = MappingType.valueOf((String)context.getMeta()
                .getOrDefault("mapping", "value_all"));
        TypeLiteral<?> targetType = context.getTargetType();
        Type[] types = TypeLiteral.getTypeParameters(targetType.getType());
        TypeLiteral<?> collectionTargetType;
        if(types.length>0) {
            collectionTargetType = TypeLiteral.of(types[0]);
        }else {
            collectionTargetType = TypeLiteral.of(String.class);
        }
        T result = collectionSupplier.get();
        switch (mappingType) {
            case node_all:
                return convertListByNodes(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, true);
            case node:
                return convertListByNodes(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, false);
            case value:
                return convertListByValues(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, false);
            default:
            case value_all:
                return convertListByValues(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, true);
        }
    }

    private static <T extends Collection> T convertListByValues(List<PropertyValue> values,
                                                                Configuration config,
                                                                TypeLiteral<?> targetType,
                                                                T result,
                                                                boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
        }
        for (PropertyValue val : values) {
            List<String> tokenList = ItemTokenizer.split(val.getValue());
            for (String token : tokenList) {
                Object o = ItemTokenizer.convertValue(token, targetType);
                if (o != null) {
                    result.add(o);
                }else{
                    LOG.warning(String.format("Failed to convert '{0}' to type: {1}, key: {2}",
                            token, targetType, val.getQualifiedKey()));
                }
            }
        }
        return result;
    }

    private static <T extends Collection> T convertListByNodes(List<PropertyValue> values,
                                                               Configuration config,
                                                               TypeLiteral<?> targetType,
                                                               T result,
                                                               boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
        }
        for (PropertyValue val : values) {
            for(PropertyValue itemNode:val) {
                if(targetType.equals(TypeLiteral.of(String.class))){
                    result.add(itemNode.getValue());
                }else {
                    Object o = ItemTokenizer.convertValue(itemNode.getValue(), targetType);
                    if (o != null) {
                        result.add(o);
                    }else{
                        LOG.warning(String.format("Failed to convert '{0}' to type: {1}, key: {2}",
                                itemNode.getValue(), targetType, itemNode.getQualifiedKey()));
                    }
                }
            }
        }
        return result;
    }

    public static <T extends Map> T convertMap(ConversionContext context,
                                               Supplier<T> collectionSupplier) {
        TypeLiteral<?> targetType = context.getTargetType();
        Type[] types = TypeLiteral.getTypeParameters(targetType.getType());
        TypeLiteral<?> collectionTargetType;
        if (types.length > 1) {
            collectionTargetType = TypeLiteral.of(types[1]);
        } else {
            collectionTargetType = TypeLiteral.of(String.class);
        }
        MappingType mappingType = MappingType.valueOf((String) context.getMeta()
                .getOrDefault("mapping", "value_all"));
        T result = collectionSupplier.get();
        switch (mappingType) {
            case node_all:
                return convertMapByNodes(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, true);
            case node:
                return convertMapByNodes(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, false);
            case value:
                return convertMapByValues(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, false);
            default:
            case value_all:
                return convertMapByValues(context.getValues(), context.getConfiguration(),
                        collectionTargetType, result, true);
        }
    }


    private static <T extends Map> T convertMapByValues(List<PropertyValue> values,
                                                                Configuration config,
                                                                TypeLiteral<?> targetType,
                                                                T result,
                                                                boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
        }
        for (PropertyValue val : values) {
            List<String> tokenList = ItemTokenizer.split(val.getValue());
            for(String token:tokenList) {
                String[] keyValue = ItemTokenizer.splitMapEntry(token);
                Object o = ItemTokenizer.convertValue(keyValue[1], targetType);
                if (o != null) {
                    result.put(keyValue[0], o);
                }else{
                    LOG.warning(String.format("Failed to convert '{0}' to type: {1}, key: {2}",
                            keyValue[1], targetType, val.getQualifiedKey()));
                }
            }
        }
        return result;
    }

    private static <T extends Map> T convertMapByNodes(List<PropertyValue> values,
                                                               Configuration config,
                                                               TypeLiteral<?> targetType,
                                                               T result,
                                                               boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
        }
        for (PropertyValue val : values) {
            for(PropertyValue itemNode:val) {
                Object o = ItemTokenizer.convertValue(itemNode.getValue(), targetType);
                if (o != null) {
                    result.put(itemNode.getKey(), o);
                }else{
                    LOG.warning(String.format("Failed to convert '{0}' to type: {1}, key: {2}",
                            itemNode.getValue(), targetType, itemNode.getQualifiedKey()));
                }
            }
        }
        return result;
    }


    public static ArrayList<String> convertSimpleList(String value) {
        List<String> rawList = ItemTokenizer.split(value);
        ArrayList<String> mlist = new ArrayList<>();
        for(String raw:rawList){
            String convValue = raw;
            if (convValue != null) {
                mlist.add(convValue);
            }
        }
        return mlist;
    }

    public static Map<String,String> convertSimpleMap(String value) {
        List<String> rawList = ItemTokenizer.split(value);
        HashMap<String,String> result = new HashMap(rawList.size());
        for(String raw:rawList){
            String[] items = ItemTokenizer.splitMapEntry(raw);
            if(items.length==1){
                result.put(items[0], items[0]);
            }else{
                result.put(items[0], items[1]);
            }
        }
        return result;
    }

    @Override
    public Collection convert(String value) {
        ConversionContext context = ConversionContext.current();
        String collectionType = "List";
        if(context!=null) {
            collectionType = (String)context.getMeta().getOrDefault("collection-type", "List");
            if (collectionType.startsWith("java.util.")) {
                collectionType = collectionType.substring("java.util.".length());
            }
        }
        Collection result = null;
        switch (collectionType) {
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
        return result;
    }
}
