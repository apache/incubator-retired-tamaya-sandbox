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
package org.apache.tamaya.jsr382;

import org.apache.tamaya.TypeLiteral;

import javax.config.ConfigAccessor;
import javax.config.ConfigSnapshot;
import javax.config.spi.Converter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Tamaya implementation type for the {@link ConfigAccessor} type.
 * @param <T> the target type.
 */
class TamayaConfigAccessor<T> implements ConfigAccessor<T> {

    private JavaConfigAdapter javaConfigAdapter;

    private String name;
    private Converter<T> customConverter;
    private TypeLiteral<T> targetType;
    private T typedDefaultValue;
    private String stringDefaultValue;
    private List<ConfigAccessor<String>> fallbackAccessors = new ArrayList<>();
    private List<String> lookupSuffixes = new ArrayList<>();
    private boolean evaluateVariables = true;
    private long timeout = 0;
    private T cachedValue;


    /**
     * Constructor.
     * @param javaConfigAdapter
     * @param name
     */
    TamayaConfigAccessor(JavaConfigAdapter javaConfigAdapter, String name) {
        this.javaConfigAdapter = Objects.requireNonNull(javaConfigAdapter);
        this.name = Objects.requireNonNull(name);
        this.targetType = TypeLiteral.of(String.class);
    }

    private TamayaConfigAccessor(TamayaConfigAccessor<?> accessor, TypeLiteral<T> target) {
        this.name = accessor.name;
        this.targetType = target;
        this.stringDefaultValue = accessor.stringDefaultValue;
        this.javaConfigAdapter = javaConfigAdapter;
        this.evaluateVariables = accessor.evaluateVariables;
        this.lookupSuffixes.addAll(accessor.lookupSuffixes);
        // What to do if the fallback accessors do not match, e.g. with collection types...
        this.fallbackAccessors.addAll(accessor.fallbackAccessors);
    }

    /**
     * Access the list of al current possible candidate keys to evaluate a value for the given accessor.
     * @return the list of al current possible candidate keys, not null.
     */
    public List<String> getCandidateKeys() {
        List<String> keys = new ArrayList<>();
        List<List<String>> listList = new ArrayList<>();
        List<String> maxList = new ArrayList<>(lookupSuffixes);
        while(!maxList.isEmpty()){
            listList.add(new ArrayList(maxList));
            maxList.remove(0);
        }
        for(List<String> list:listList) {
            keys.addAll(getSuffixKeys(list));
        }
        keys.add(getPropertyName());
        return keys;
    }

    private List<String> getSuffixKeys(List<String> list) {
        List<String> result = new ArrayList<>();
        while(!list.isEmpty()){
            result.add(getPropertyName()+'.'+ String.join(".", list));
            if(list.size()>1) {
                list.remove(list.size() - 2);
            }else{
                list.remove(0);
            }
        }
        return result;
    }

    @Override
    public <N> ConfigAccessor<N> as(Class<N> aClass) {
        return new TamayaConfigAccessor<N>(this, TypeLiteral.of(aClass));
    }

    @Override
    public ConfigAccessor<List<T>> asList() {
        TypeLiteral<List<T>> target = new TypeLiteral<List<T>>();
        return new TamayaConfigAccessor<List<T>>(this, target);
    }

    @Override
    public ConfigAccessor<Set<T>> asSet() {
        TypeLiteral<Set<T>> target = new TypeLiteral<Set<T>>();
        return new TamayaConfigAccessor<Set<T>>(this, target);
    }

    @Override
    public ConfigAccessor<T> useConverter(Converter<T> converter) {
        this.customConverter = converter;
        return this;
    }

    @Override
    public ConfigAccessor<T> withDefault(T defaultValue) {
        this.typedDefaultValue = defaultValue;
        return this;
    }

    @Override
    public ConfigAccessor<T> withStringDefault(String defaultValue) {
        this.stringDefaultValue = defaultValue;
        return this;
    }

    @Override
    public ConfigAccessor<T> cacheFor(long duration, TimeUnit timeUnit) {
        this.cachedValue = getValue();
        this.timeout = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(duration, timeUnit);
        return this;
    }

    @Override
    public ConfigAccessor<T> evaluateVariables(boolean evaluateVariables) {
        this.evaluateVariables = evaluateVariables;
        return this;
    }

    @Override
    public ConfigAccessor<T> addLookupSuffix(String lookupSuffix) {
        this.lookupSuffixes.add(lookupSuffix);
        return this;
    }

    @Override
    public ConfigAccessor<T> addLookupSuffix(ConfigAccessor<String> configAccessor) {
        this.fallbackAccessors.add(configAccessor);
        return this;
    }

    @Override
    public T getValue() {
        if(this.timeout > System.currentTimeMillis()){
            return this.cachedValue;
        }
        T value = getValueInternal(name);
        if(value==null){
            value = getValueInternalFromDefaults();
        }
        if(value==null){
            throw new IllegalArgumentException("No such value: " + name);
        }
        return value;
    }

    private T getValueInternal(String key) {
        T value = null;
        if(customConverter!=null){
            String textVal = javaConfigAdapter.getConfiguration().getOrDefault(name, String.class, null);
            if(textVal==null){
                textVal = stringDefaultValue;
            }
            if(textVal!=null) {
                value = customConverter.convert(textVal);
            }
        }else {
            value = javaConfigAdapter.getConfiguration().getOrDefault(name, targetType, null);
        }
        return value;
    }

    private T getValueInternalFromDefaults() {
        T value = null;
        if(customConverter!=null){
            String textVal = stringDefaultValue;
            if(textVal!=null) {
                value = customConverter.convert(textVal);
            }
        }
        // Should we also try to convert with the String default value and existing converters?
        if(value==null){
            value = typedDefaultValue;
        }
        return value;
    }

    @Override
    public T getValue(ConfigSnapshot configSnapshot) {
        return ((TamayaConfigSnapshot)configSnapshot).getConfiguration()
                .get(name, targetType);
    }

    @Override
    public Optional<T> getOptionalValue(ConfigSnapshot configSnapshot) {
        return Optional.ofNullable(((TamayaConfigSnapshot)configSnapshot).getConfiguration()
                .getOrDefault(name, targetType, null));
    }

    @Override
    public Optional<T> getOptionalValue() {
        return Optional.empty();
    }

    @Override
    public String getPropertyName() {
        return this.name;
    }

    @Override
    public String getResolvedPropertyName() {
        return null;
    }

    @Override
    public T getDefaultValue() {
        return typedDefaultValue;
    }

    @Override
    public String toString() {
        return "TamayaConfigAccessor{" +
                "javaConfigAdapter=" + javaConfigAdapter +
                ", name='" + name + '\'' +
                ", customConverter=" + customConverter +
                ", targetType=" + targetType +
                ", typedDefaultValue=" + typedDefaultValue +
                ", stringDefaultValue='" + stringDefaultValue + '\'' +
                ", fallbackAccessors=" + fallbackAccessors +
                ", lookupSuffixes=" + lookupSuffixes +
                ", evaluateVariables=" + evaluateVariables +
                ", timeout=" + timeout +
                ", cachedValue=" + cachedValue +
                '}';
    }

}
