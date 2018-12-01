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

import org.apache.tamaya.doc.annot.ConfigSpec;

import java.util.*;

/**
 * Documentation of an application configuration.
 */
public final class DocumentedConfiguration {

    private String name;
    private String version;
    private Class ownerClass;

    private Map<String, DocumentedProperty> properties = new TreeMap<>();
    private Map<String, DocumentedArea> groups = new TreeMap<>();

    /**
     * Creates a new empty configuration documentation.
     */
    public DocumentedConfiguration(){}

    /**
     * Creates a new configuration documentation and initializes it with the values from the annotation given.
     * @param annotation the spec annotation, not null.
     */
    public DocumentedConfiguration(ConfigSpec annotation){
        init(annotation, null);
    }

    /**
     * Initializes the instance with the given annotation.
     * @param annotation the annotation , not null.
     * @param ownerClass the annotaed class.
     */
    public void init(ConfigSpec annotation, Class ownerClass) {
        this.name = annotation.name();
        this.version = annotation.version();
        this.ownerClass = ownerClass;
    }

    /**
     * Get the current configuration name.
     * @return the name, or '<undefined>'.
     */
    public String getName() {
        if(name==null){
            return "<undefined>";
        }
        return name;
    }

    /**
     * Get the current configuration version.
     * @return the version, or '<undefined>'.
     */
    public String getVersion() {
        if(version==null){
            return "<undefined>";
        }
        return version;
    }

    /**
     * Get the annotated class.
     * @return the class, or null.
     */
    public Class getOwnerClass() {
        return ownerClass;
    }

    /**
     * Get the documented properties.
     * @return the properties, never null.
     */
    public Map<String, DocumentedProperty> getProperties() {
        return properties;
    }

    /**
     * Get the documented properties.
     * @return the properties, never null.
     */
    public List<DocumentedArea> getAllAreasSorted() {
        List<DocumentedArea> areas = new ArrayList<>();
        areas.addAll(getAreas().values());
        areas.sort(this::compareAreas);
        return areas;
    }

    private int compareAreas(DocumentedArea area1, DocumentedArea area2) {
        return area1.getPath().compareTo(area2.getPath());
    }

    /**
     * Get the documented properties.
     * @return the properties, never null.
     */
    public List<DocumentedProperty> getAllPropertiesSorted() {
        List<DocumentedProperty> props = new ArrayList<>();
        props.addAll(getProperties().values());
        for(DocumentedArea area:getAreas().values()) {
            props.addAll(area.getProperties().values());
        }
        props.sort(this::compareProperties);
        return props;
    }

    private int compareProperties(DocumentedProperty property, DocumentedProperty property1) {
        return property.getName().compareTo(property1.getName());
    }

    /**
     * Get the documented areas.
     * @return the areas, never null.
     */
    public Map<String, DocumentedArea> getAreas() {
        return groups;
    }

    public DocumentedArea getGroup(String path) {
        return null;
    }

    public DocumentedProperty getProperty(String path) {
        return null;
    }

    public DocumentedConfiguration addProperty(DocumentedProperty property){
        this.properties.put(property.getName(), property);
        return this;
    }

    public DocumentedConfiguration addGroup(DocumentedArea group){
        this.groups.put(group.getPath(), group);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentedConfiguration)) return false;

        DocumentedConfiguration that = (DocumentedConfiguration) o;

        if (!name.equals(that.name)) return false;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DocumentedConfiguration{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", properties=" + properties +
                ", groups=" + groups +
                '}';
    }

}
