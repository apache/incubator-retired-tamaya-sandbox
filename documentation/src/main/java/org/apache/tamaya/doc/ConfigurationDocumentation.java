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
public final class ConfigurationDocumentation {

    private String name;
    private String version;
    private Class ownerClass;

    private Map<String, DocumentedProperty> properties = new TreeMap<>();
    private Map<String, DocumentedProperty> allProperties = new TreeMap<>();
    private Map<String, DocumentedArea> groups = new TreeMap<>();
    private Map<String, DocumentedArea> allGroups = new TreeMap<>();

    /**
     * Creates a new empty configuration documentation.
     */
    public ConfigurationDocumentation(){}

    /**
     * Creates a new configuration documentation and initializes it with the values from the annotation given.
     * @param annotation the spec annotation, not null.
     */
    public ConfigurationDocumentation(ConfigSpec annotation){
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

    public void resolveReferences(){
        DocumentedArea currentArea = null;
        for(DocumentedProperty prop:getAllPropertiesSorted()){
            String[] parentCandidates = getCandidates(prop.getMainKey());
            for(String candidateId:parentCandidates){
                currentArea = getArea(candidateId);
                if(currentArea!=null){
                    prop.parentArea(currentArea);
                }
            }
        }
    }

    private String[] getCandidates(String mainKey) {
        List<String> candidates = new ArrayList<>();
        String[] parts = mainKey.split("\\.");
        if(parts.length==1){
            candidates.add("");
        }else {
            for (int max = parts.length-1; max >= 0; max--) {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < max; i++) {
                    b.append(parts[i]).append(".");
                }
                b.setLength(b.length() - 1);
                candidates.add(b.toString());
            }
            candidates.add("");
        }
        return candidates.toArray(new String[candidates.size()]);
    }

    /**
     * Get the current configuration name.
     * @return the name, or {@code <undefined>}.
     */
    public String getName() {
        if(name==null){
            return "<undefined>";
        }
        return name;
    }

    /**
     * Get the current configuration version.
     * @return the version, or {@code <undefined>}.
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
        return area1.getMainBasePath().compareTo(area2.getMainBasePath());
    }

    /**
     * Get the documented properties.
     * @return the properties, never null.
     */
    public List<DocumentedProperty> getAllPropertiesSorted() {
        List<DocumentedProperty> props = new ArrayList<>();
        props.addAll(getProperties().values());
        props.sort(this::compareProperties);
        return props;
    }

    private int compareProperties(DocumentedProperty property, DocumentedProperty property1) {
        return property.getMainKey().compareTo(property1.getMainKey());
    }

    /**
     * Get the documented areas.
     * @return the areas, never null.
     */
    public Map<String, DocumentedArea> getAreas() {
        return groups;
    }

    public DocumentedArea getArea(String path) {
        return allGroups.get(path);
    }

    public DocumentedProperty getProperty(String path) {
        return allProperties.get(path);
    }

    public ConfigurationDocumentation addProperty(DocumentedProperty property){
        for(String key:property.getKeys()) {
            this.allProperties.put(key, property);
        }
        this.properties.put(property.getMainKey(), property);
        return this;
    }

    public ConfigurationDocumentation addGroup(DocumentedArea group){
        for(String basePath:group.getBasePaths()) {
            this.allGroups.put(basePath, group);
        }
        this.groups.put(group.getMainBasePath(), group);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationDocumentation)) return false;

        ConfigurationDocumentation that = (ConfigurationDocumentation) o;

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
                ", areas=" + groups +
                '}';
    }

}
