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
import org.apache.tamaya.inject.api.ConfigSection;
import org.apache.tamaya.inject.spi.InjectionUtils;
import org.apache.tamaya.spi.PropertyValue;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A class representing the documented area of a configuration.
 */
public final class DocumentedArea {

    private final AnnotatedElement owner;
    private ConfigAreaSpec configArea;
    private String mainBasePath;
    private Set<String> basePaths = new TreeSet<>();
    private String description;
    private PropertyValue.ValueType groupType;
    private Class<?> valueType;

    private int minCardinality;
    private int maxCardinality;
    private Set<DocumentedArea> dependsOnGroups = new TreeSet<>();
    private Set<DocumentedProperty> dependsOnProperties = new TreeSet<>();

    public DocumentedArea(ConfigAreaSpec areaSpec, AnnotatedElement owner){
        this.owner = owner;
        this.configArea = areaSpec;
        if(!(areaSpec.basePaths().length==0)) {
            this.mainBasePath = areaSpec.basePaths()[0];
            this.basePaths.addAll(Arrays.asList(areaSpec.basePaths()));
        }else{
           this.mainBasePath = evaluatePath(owner);
           this.basePaths.add(mainBasePath);
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
    }

    private String evaluatePath(AnnotatedElement owner) {
        if(owner instanceof Field) {
            return String.join(", ", InjectionUtils.getKeys((Field) owner));
        }else if(owner instanceof Method) {
            return String.join(", ", InjectionUtils.getKeys((Method) owner));
        }else if(owner instanceof Class) {
            ConfigSection sectionsAnnot = owner.getAnnotation(ConfigSection.class);
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
                this.dependsOnGroups.add(documentation.getArea(key));
            }
            for(String key: configArea.dependsOnProperties()){
                this.dependsOnProperties.add(documentation.getProperty(key));
            }
        }
    }

    public AnnotatedElement getOwner() {
        return owner;
    }

    public Set<String> getBasePaths() {
        return Collections.unmodifiableSet(basePaths);
    }

    public String getDescription() {
        return description;
    }

    public PropertyValue.ValueType getGroupType() {
        return groupType;
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
        result.sort(Comparator.comparing(DocumentedArea::getMainBasePath));
        return result;
    }

    public String getMainBasePath() {
        return this.mainBasePath;
    }

    public List<DocumentedProperty> getDependsOnProperties() {
        List<DocumentedProperty> result = new ArrayList<>(dependsOnProperties);
        result.sort(Comparator.comparing(DocumentedProperty::getMainKey));
        return result;
    }

    public DocumentedArea addBasePath(String path) {
        this.basePaths.add(path);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentedArea that = (DocumentedArea) o;

        return Objects.equals(this.dependsOnGroups, that.dependsOnGroups) &&
                Objects.equals(this.dependsOnProperties, that.dependsOnProperties) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.groupType, that.groupType) &&
                Objects.equals(this.maxCardinality, that.maxCardinality) &&
                Objects.equals(this.minCardinality, that.minCardinality) &&
                Objects.equals(this.basePaths, that.basePaths) &&
                Objects.equals(this.valueType, that.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependsOnGroups, dependsOnProperties, description, groupType, maxCardinality,
                minCardinality, basePaths, valueType);
    }

    @Override
    public String toString() {
        return "ConfigGroup{" +
                "basePaths='" + basePaths + '\'' +
                ", description='" + description + '\'' +
                ", areaType=" + groupType +
                ", valueType=" + valueType +
                ", minCardinality=" + minCardinality +
                ", maxCardinality=" + maxCardinality +
                ", dependsOnAreas=" + dependsOnGroups +
                ", dependsOnProperties=" + dependsOnProperties +
                '}';
    }


}
