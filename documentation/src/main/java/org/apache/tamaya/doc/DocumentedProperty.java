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
    private String name;
    private String defaultValue;
    private boolean required;
    private String description;
    private Class<?> valueType;

    private Set<DocumentedArea> dependsOnGroups = new TreeSet<>();
    private Set<DocumentedProperty> dependsOnProperties = new TreeSet<>();


    public DocumentedProperty(ConfigPropertySpec annot, AnnotatedElement owner){
        this.owner = owner;
        this.propertySpec = annot;
        if(!annot.name().isEmpty()) {
            this.name = annot.name();
        }else{
            if(owner instanceof Field) {
                this.name = String.join(", ", InjectionUtils.getKeys((Field) owner));
            }else if(owner instanceof Method) {
                this.name = String.join(", ", InjectionUtils.getKeys((Method) owner));
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
            this.defaultValue = configAnnot.defaultValue();
        }
    }

    void resolve(DocumentedConfiguration documentation){
        if(propertySpec !=null){
            for(String key: propertySpec.dependsOnAreas()){
                this.dependsOnGroups.add(documentation.getGroup(key));
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        if(description==null){
            return "";
        }
        return description;
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

    public DocumentedProperty path(String path) {
        this.name = path;
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
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependsOnGroups, dependsOnProperties, description, name, valueType);
    }

    @Override
    public String toString() {
        return "ConfigGroup{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", valueType=" + valueType +
                ", dependsOnAreas=" + dependsOnGroups +
                ", dependsOnProperties=" + dependsOnProperties +
                '}';
    }

}
