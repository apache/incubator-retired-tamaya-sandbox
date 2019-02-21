/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy createObject the License at
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
package org.apache.tamaya.doc;

import org.apache.tamaya.doc.annot.ConfigPropertySpec;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.spi.InjectionUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class DocumentedProperty {

    private final AnnotatedElement owner;
    private ConfigPropertySpec propertySpec;
    private String mainKey;
    private Set<String> keys = new TreeSet<>();
    private String defaultValue;
    private boolean required;
    private String description;
    private Class<?> valueType;
    private DocumentedArea parentArea;

    private Set<DocumentedArea> dependsOnGroups = new TreeSet<>();
    private Set<DocumentedProperty> dependsOnProperties = new TreeSet<>();


    public DocumentedProperty(ConfigPropertySpec annot, AnnotatedElement owner){
        this.owner = owner;
        this.propertySpec = annot;
        if(!(annot.keys().length==0)) {
            this.mainKey = annot.keys()[0];
            this.keys.addAll(Arrays.asList(annot.keys()));
        }else{
            if(owner instanceof Field) {
                List<String> evalKeys = InjectionUtils.getKeys((Field) owner);
                this.mainKey = evalKeys.get(0);
                this.keys.addAll(evalKeys);
            }else if(owner instanceof Method) {
                List<String> evalKeys = InjectionUtils.getKeys((Method) owner);
                this.mainKey = evalKeys.get(0);
                this.keys.addAll(evalKeys);
            }
        }
        this.description = annot.description();
        this.valueType = annot.valueType();
        if(String.class.equals(this.valueType)){
            if(owner instanceof Field){
                Field f = (Field)owner;
                this.valueType = f.getType();
            }else if(owner instanceof Method){
                Method m = (Method)owner;
                this.valueType = m.getParameterTypes()[0];
            }
        }
        Config configAnnot = owner.getAnnotation(Config.class);
        if(configAnnot!=null){
            this.required = configAnnot.required();
            if(!Config.UNCONFIGURED_VALUE.equals(configAnnot.defaultValue())) {
                this.defaultValue = configAnnot.defaultValue();
            }
        }
    }

    void resolve(ConfigurationDocumentation documentation){
        if(propertySpec !=null){
            for(String key: propertySpec.dependsOnAreas()){
                this.dependsOnGroups.add(documentation.getArea(key));
            }
            for(String key: propertySpec.dependsOnProperties()){
                this.dependsOnProperties.add(documentation.getProperty(key));
            }
        }
    }

    public AnnotatedElement getOwner() {
        return owner;
    }

    public String getDefaultValue() {
        if(defaultValue==null){
            return "";
        }
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(keys);
    }

    public Set<String> getBackupKeys() {
        Set<String> result = new TreeSet<>(keys);
        result.remove(getMainKey());
        return result;
    }

    public String getMainKey() {
        return this.mainKey;
    }

    public String getDescription() {
        if(description==null){
            return "";
        }
        return description;
    }

    public DocumentedArea getParentArea(){
        return parentArea;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public Set<DocumentedArea> getDependsOnGroups() {
        return dependsOnGroups;
    }

    public Set<DocumentedProperty> getDependsOnProperties() {
        return dependsOnProperties;
    }

    public DocumentedProperty addKey(String key) {
        this.keys.add(key);
        return this;
    }

    public DocumentedProperty parentArea(DocumentedArea parentArea) {
        this.parentArea = parentArea;
        return this;
    }

    public DocumentedProperty description(String description) {
        this.description = description;
        return this;
    }

    public DocumentedProperty valueType(Class<?> valueType) {
        this.valueType = valueType;
        return this;
    }

    public DocumentedProperty dependsOnGroups(Collection<DocumentedArea> dependsOnGroups) {
        this.dependsOnGroups.addAll(dependsOnGroups);
        return this;
    }

    public DocumentedProperty dependsOnGroups(DocumentedArea... dependsOnGroups) {
        this.dependsOnGroups.addAll(Arrays.asList(dependsOnGroups));
        return this;
    }

    public DocumentedProperty dependsOnProperties(Collection<DocumentedProperty> dependsOnProperties) {
        this.dependsOnProperties.addAll(dependsOnProperties);
        return this;
    }

    public DocumentedProperty dependsOnProperties(DocumentedProperty... dependsOnProperties) {
        this.dependsOnProperties.addAll(Arrays.asList(dependsOnProperties));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentedProperty that = (DocumentedProperty) o;

        return Objects.equals(this.dependsOnGroups, that.dependsOnGroups) &&
                Objects.equals(this.dependsOnProperties, that.dependsOnProperties) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.keys, that.keys) &&
                Objects.equals(this.valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependsOnGroups, dependsOnProperties, description, keys, valueType);
    }

    @Override
    public String toString() {
        return "ConfigGroup{" +
                "keys='" + keys + '\'' +
                ", description='" + description + '\'' +
                ", valueType=" + valueType +
                ", dependsOnAreas=" + dependsOnGroups +
                ", dependsOnProperties=" + dependsOnProperties +
                '}';
    }

}
