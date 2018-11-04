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
    public static final String VALUE_MAPPING = "collection-mapping";

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
        node_all,
        /** This will guess the matching evaluation policy (value,bode) for each {@code PropertyValue} and
         * combine the items of all values. */
        combine,
        /** This will guess the matching evaluation policy (value,bode) on the most significant {@code PropertyValue}
         * only. */
        override,
    }

    private MappingType mappingType = MappingType.value_all;

    public static <T extends Collection> T convertList(ConversionContext context,
                                                       Supplier<T> collectionSupplier) {
        MappingType mappingType = MappingType.valueOf((String)context.getMeta()
                .getOrDefault(VALUE_MAPPING, MappingType.combine.toString()));
        TypeLiteral<?> targetType = context.getTargetType();
        Type[] types = TypeLiteral.getTypeParameters(targetType.getType());
        TypeLiteral<?> collectionTargetType;
        if(types.length>0) {
            collectionTargetType = TypeLiteral.of(types[0]);
        }else {
            LOG.warning(String.format("No type information for Collection item type in '{0}', using String.",
                    context.getKey()));
            collectionTargetType = TypeLiteral.of(String.class);
        }
        T result = collectionSupplier.get();
        switch (mappingType) {
            case node_all:
                return convertListByNodes(context.getValues(), context,
                        collectionTargetType, result, true);
            case node:
                return convertListByNodes(context.getValues(), context,
                        collectionTargetType, result, false);
            case value:
                return convertListByValues(context.getValues(), context,
                        collectionTargetType, result, false);
            case value_all:
                return convertListByValues(context.getValues(), context,
                        collectionTargetType, result, true);
            case override:
                return convertListWithBestGuess(context.getValues(), context,
                        collectionTargetType, result,false);
            case combine:
            default:
                return convertListWithBestGuess(context.getValues(), context,
                        collectionTargetType, result,true);
        }
    }

    private static <T extends Collection> T convertListWithBestGuess(List<PropertyValue> values,
                                                                ConversionContext context,
                                                                TypeLiteral<?> targetType,
                                                                T result,
                                                                boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
            LOG.finest(String.format("Combine deactivated, only checking for collection values in {0}.", values.get(0)));
        }else{
            LOG.finest(String.format("Combine activated, checking for collection values in {0}.", values));
        }
        // First: try value based approach
        for (PropertyValue val : values) {
            int valuesFound = 0;
            List<String> tokenList = ItemTokenizer.split(val.getValue(), context);
            for (String token : tokenList) {
                Object o = ItemTokenizer.convertValue(token, targetType, context);
                if (o != null) {
                    valuesFound++;
                    result.add(o);
                }
            }
            if(valuesFound==0) {
                LOG.finest(() -> String.format("No values found in {0} using value evaluation, checking for child nodes...", val));
                for(PropertyValue itemNode:val) {
                    String textValue = itemNode.getValue();
                    if(textValue!=null) {
                        if (targetType.equals(TypeLiteral.of(String.class))) {
                            valuesFound++;
                            result.add(textValue);
                        } else {
                            Object o = ItemTokenizer.convertValue(itemNode.getValue(), targetType, context);
                            if (o != null) {
                                valuesFound++;
                                result.add(o);
                            }
                        }
                    }
                }
            }
            if(valuesFound==0){
                LOG.warning(String.format("Failed to convert key '{0}' to type: {1}: no values found.",
                            val.getKey(), targetType));
            }else{
                LOG.finest(String.format("Found {2} collection values for key '{0}' with type: {1}: no values found.",
                        val.getKey(), targetType, valuesFound));
            }
        }

        return result;
    }

    private static <T extends Collection> T convertListByValues(List<PropertyValue> values,
                                                                ConversionContext context,
                                                                TypeLiteral<?> targetType,
                                                                T result,
                                                                boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
            LOG.finest(String.format("Combine deactivated, only checking for collection values in {0}.", values.get(0)));
        }else{
            LOG.finest(String.format("Combine activated, checking for collection values in {0}.", values));
        }
        for (PropertyValue val : values) {
            List<String> tokenList = ItemTokenizer.split(val.getValue(), context);
            for (String token : tokenList) {
                Object o = ItemTokenizer.convertValue(token, targetType, context);
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
                                                               ConversionContext context,
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
                    Object o = ItemTokenizer.convertValue(itemNode.getValue(), targetType, context);
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
            LOG.warning(String.format("No type information for Map parameter types in '{0}', using String.",
                    context.getKey()));
            collectionTargetType = TypeLiteral.of(String.class);
        }
        MappingType mappingType = MappingType.valueOf((String) context.getMeta()
                .getOrDefault("mapping", "value_all"));
        T result = collectionSupplier.get();
        switch (mappingType) {
            case node_all:
                return convertMapByNodes(context.getValues(), context,
                        collectionTargetType, result, true);
            case node:
                return convertMapByNodes(context.getValues(), context,
                        collectionTargetType, result, false);
            case value:
                return convertMapByValues(context.getValues(), context,
                        collectionTargetType, result, false);
            default:
            case value_all:
                return convertMapByValues(context.getValues(), context,
                        collectionTargetType, result, true);
        }
    }


    private static <T extends Map> T convertMapByValues(List<PropertyValue> values,
                                                        ConversionContext context,
                                                        TypeLiteral<?> targetType,
                                                        T result,
                                                        boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
            LOG.finest(String.format("Combine deactivated, only checking for collection values in {0}.", values.get(0)));
        }else{
            LOG.finest(String.format("Combine activated, checking for collection values in {0}.", values));
        }
        for (PropertyValue val : values) {
            List<String> tokenList = ItemTokenizer.split(val.getValue(), context);
            for(String token:tokenList) {
                String[] keyValue = ItemTokenizer.splitMapEntry(token, context);
                Object o = ItemTokenizer.convertValue(keyValue[1], targetType, context);
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
                                                       ConversionContext context,
                                                       TypeLiteral<?> targetType,
                                                       T result,
                                                       boolean combine) {
        if(!combine){
            values = Collections.singletonList(values.get(0));
            LOG.finest(String.format("Combine deactivated, only checking for collection values in {0}.", values.get(0)));
        }else{
            LOG.finest(String.format("Combine activated, checking for collection values in {0}.", values));
        }
        for (PropertyValue val : values) {
            for(PropertyValue itemNode:val) {
                Object o = ItemTokenizer.convertValue(itemNode.getValue(), targetType, context);
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


    @Override
    public Collection convert(String value, ConversionContext context) {
        String collectionType = (String)context.getMeta().getOrDefault("collection-type", "List");
        if (collectionType.startsWith("java.util.")) {
            collectionType = collectionType.substring("java.util.".length());
        }
        Collection result = null;
        switch (collectionType) {
            case "LinkedList":
                result = LinkedListConverter.getInstance().convert(value, context);
                break;
            case "Set":
            case "HashSet":
                result = HashSetConverter.getInstance().convert(value, context);
                break;
            case "SortedSet":
            case "TreeSet":
                result = TreeSetConverter.getInstance().convert(value, context);
                break;
            case "List":
            case "ArrayList":
            default:
                result = ArrayListConverter.getInstance().convert(value, context);
                break;
        }
        return result;
    }
}
