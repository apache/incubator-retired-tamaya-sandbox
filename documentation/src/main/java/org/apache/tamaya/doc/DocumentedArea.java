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

import org.apache.tamaya.doc.annot.ConfigAreaSpec;
import org.apache.tamaya.doc.annot.ConfigPropertySpec;
import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.inject.spi.InjectionUtils;
import org.apache.tamaya.spi.PropertyValue;

import java.lang.reflect.*;
import java.util.*;

public final class DocumentedArea {

    private final AnnotatedElement owner;
    private ConfigAreaSpec configArea;
    private String path;
    private String description;
    private PropertyValue.ValueType groupType;
    private Map<String, DocumentedProperty> properties = new HashMap<>();
    private Map<String, DocumentedArea> areas = new HashMap<>();
    private Class<?> valueType;

    private int minCardinality;
    private int maxCardinality;
    private Set<DocumentedArea> dependsOnGroups = new TreeSet<>();
    private Set<DocumentedProperty> dependsOnProperties = new TreeSet<>();

    public DocumentedArea(ConfigAreaSpec areaSpec, AnnotatedElement owner){
        this.owner = owner;
        this.configArea = areaSpec;
        if(!areaSpec.path().isEmpty()) {
            this.path = areaSpec.path();
        }else{
           this.path = evaluatePath(owner);
        }
        if(!areaSpec.description().isEmpty()) {
            this.description = areaSpec.description();
        }
        this.groupType = areaSpec.areaType();
        if(areaSpec.max()>0) {
            this.maxCardinality = areaSpec.max();
        }
        if(areaSpec.min()>0) {
            this.minCardinality = areaSpec.min();
        }
        if(Object.class!=areaSpec.valueType()) {
            this.valueType = areaSpec.valueType();
        }else{
            if(owner instanceof Class){
                this.valueType = ((Class)owner);
            }
        }
        for(ConfigPropertySpec ps:areaSpec.properties()) {
            this.properties.put(ps.name(), new DocumentedProperty(ps, owner));
        }
    }

    private String evaluatePath(AnnotatedElement owner) {
        if(owner instanceof Field) {
            return String.join(", ", InjectionUtils.getKeys((Field) owner));
        }else if(owner instanceof Method) {
            return String.join(", ", InjectionUtils.getKeys((Method) owner));
        }else if(owner instanceof Class) {
            ConfigDefaultSections sectionsAnnot = owner.getAnnotation(ConfigDefaultSections.class);
            if(sectionsAnnot!=null){
                return String.join(", ", sectionsAnnot.value());
            }
            return ((Class)owner).getName()+", "+((Class)owner).getSimpleName();
        }
        return "<root>";
    }

    void resolve(DocumentedConfiguration documentation){
        if(configArea !=null){
            for(String key: configArea.dependsOnAreas()){
                this.dependsOnGroups.add(documentation.getGroup(key));
            }
            for(String key: configArea.dependsOnProperties()){
                this.dependsOnProperties.add(documentation.getProperty(key));
            }
        }
    }

    public DocumentedArea addGroup(DocumentedArea group){
        this.areas.put(group.path, group);
        return this;
    }

    public DocumentedArea addProperty(DocumentedProperty property){
        this.properties.put(property.getName(), property);
        return this;
    }

    public AnnotatedElement getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public PropertyValue.ValueType getGroupType() {
        return groupType;
    }

    public Map<String, DocumentedProperty> getProperties() {
        return properties;
    }

    public List<DocumentedProperty> getPropertiesSorted() {
        List<DocumentedProperty> result = new ArrayList<>(properties.values());
        result.sort(Comparator.comparing(DocumentedProperty::getName));
        return result;
    }

    public Map<String, DocumentedArea> getAreas() {
        return areas;
    }

    public List<DocumentedArea> getAreasSorted() {
        List<DocumentedArea> result = new ArrayList<>(areas.values());
        result.sort(Comparator.comparing(DocumentedArea::getPath));
        return result;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public List<DocumentedArea> getDependsOnAreas() {
        List<DocumentedArea> result = new ArrayList<>(dependsOnGroups);
        result.sort(Comparator.comparing(DocumentedArea::getPath));
        return result;
    }

    public List<DocumentedProperty> getDependsOnProperties() {
        List<DocumentedProperty> result = new ArrayList<>(dependsOnProperties);
        result.sort(Comparator.comparing(DocumentedProperty::getName));
        return result;
    }

    public DocumentedArea path(String path) {
        this.path = path;
        return this;
    }

    public DocumentedArea description(String description) {
        this.description = description;
        return this;
    }

    public DocumentedArea groupType(PropertyValue.ValueType groupType) {
        this.groupType = groupType;
        return this;
    }

    public DocumentedArea properties(Map<String, DocumentedProperty> properties) {
        this.properties = properties;
        return this;
    }

    public DocumentedArea groups(Map<String, DocumentedArea> groups) {
        this.areas = groups;
        return this;
    }

    public DocumentedArea valueType(Class<?> valueType) {
        this.valueType = valueType;
        return this;
    }

    public DocumentedArea minCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
        return this;
    }

    public DocumentedArea maxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
        return this;
    }

    public DocumentedArea dependsOnGroups(Set<DocumentedArea> dependsOnGroups) {
        this.dependsOnGroups = dependsOnGroups;
        return this;
    }

    public DocumentedArea dependsOnProperties(Set<DocumentedProperty> dependsOnProperties) {
        this.dependsOnProperties = dependsOnProperties;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentedArea that = (DocumentedArea) o;

        return Objects.equals(this.dependsOnGroups, that.dependsOnGroups) &&
                Objects.equals(this.dependsOnProperties, that.dependsOnProperties) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.areas, that.areas) &&
                Objects.equals(this.groupType, that.groupType) &&
                Objects.equals(this.maxCardinality, that.maxCardinality) &&
                Objects.equals(this.minCardinality, that.minCardinality) &&
                Objects.equals(this.path, that.path) &&
                Objects.equals(this.properties, that.properties) &&
                Objects.equals(this.valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependsOnGroups, dependsOnProperties, description, areas, groupType, maxCardinality,
                minCardinality, path, properties, valueType);
    }

    @Override
    public String toString() {
        return "ConfigGroup{" +
                "path='" + path + '\'' +
                ", description='" + description + '\'' +
                ", areaType=" + groupType +
                ", properties=" + properties +
                ", areas=" + areas +
                ", valueType=" + valueType +
                ", minCardinality=" + minCardinality +
                ", maxCardinality=" + maxCardinality +
                ", dependsOnAreas=" + dependsOnGroups +
                ", dependsOnProperties=" + dependsOnProperties +
                '}';
    }


}
