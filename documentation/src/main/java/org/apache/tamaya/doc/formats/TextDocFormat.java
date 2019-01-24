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
package org.apache.tamaya.doc.formats;

import org.apache.tamaya.doc.DocFormat;
import org.apache.tamaya.doc.DocumentedConfiguration;
import org.apache.tamaya.doc.DocumentedArea;
import org.apache.tamaya.doc.DocumentedProperty;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A text-based documentation format.
 */
public class TextDocFormat implements DocFormat<String> {
    @Override
    public String apply(DocumentedConfiguration documentedConfiguration) {
        StringBuilder b = new StringBuilder();
        b.append("Configuration:\n");
        b.append("  Spec    : ").append(documentedConfiguration.getName()).append('\n');
        b.append("  Version : ").append(documentedConfiguration.getVersion()).append('\n');
        if(documentedConfiguration.getOwnerClass()!=null){
            b.append("  Owner   : ").append(documentedConfiguration.getOwnerClass().getName()).append('\n');
        }
        if(!documentedConfiguration.getAreas().isEmpty()) {
            b.append("Areas: ").append('\n');
            for (DocumentedArea area : documentedConfiguration.getAreas().values()) {
                printArea("  ", area, b);
            }
        }
        if(!documentedConfiguration.getProperties().isEmpty()) {
            b.append("Properties: ").append('\n');
            for (DocumentedProperty prop : documentedConfiguration.getProperties().values()) {
                printProperty("  ", prop, b);
            }
        }
        return b.toString();
    }

    private void printArea(String inset, DocumentedArea area, StringBuilder b) {
        if(!area.getPath().isEmpty()) {
            b.append(inset).append("- name     : ").append(area.getPath()).append("\n");
        }else{
            b.append(inset).append("- name     : NONE\n");
        }
//        b.append(inset).    append("  Type     : area\n");
        if(area.getOwner()!=null){
            b.append(inset).append("  Owner    : ").append(printOwner(area.getOwner())).append('\n');
        }
        if(area.getDescription()!=null) {
            b.append(inset).append("  Descr    : ").append(area.getDescription()).append('\n');
        }
        b.append(inset).    append("  Areatype : ").append(area.getGroupType()).append('\n');
        if(area.getMinCardinality()!=0) {
            b.append(inset).append("  Min      : ").append(area.getMinCardinality()).append('\n');
        }
        if(area.getMaxCardinality()!=0) {
            b.append(inset).append("  Max      : ").append(area.getMaxCardinality()).append('\n');
        }
        if(area.getValueType()!=Object.class) {
            b.append(inset).append("  Value    : ").append(area.getValueType().getName()).append('\n');
        }
        if(!area.getProperties().isEmpty()) {
            b.append(inset).append("  Properties : ").append('\n');
            for (DocumentedProperty prop : area.getProperties().values()) {
                printProperty(inset + "    ", prop, b);
            }
        }
        if(!area.getAreas().isEmpty()) {
            b.append(inset).append("  Areas   : ").append('\n');
            for (DocumentedArea childArea : area.getAreas().values()) {
                printArea(inset + "  ", childArea, b);
            }
        }
    }

    private void printProperty(String inset, DocumentedProperty prop, StringBuilder b) {
        b.append(inset).append("- Name     : ").append(prop.getName()).append("\n");
//        b.append(inset).append("  Type     : property\n");
        if(prop.getOwner()!=null){
            b.append(inset).append("  Owner    : ").append(printOwner(prop.getOwner())).append('\n');
        }
        if(prop.getDescription()!=null) {
            b.append(inset).append("  Descr    : ").append(prop.getDescription()).append('\n');
        }
        b.append(inset).append("  Value    : ").append(prop.getValueType().getName()).append('\n');
    }

    private String printOwner(AnnotatedElement owner) {
        if (owner instanceof Type) {
            return ((Type)owner).getTypeName();
        } else if (owner instanceof Field) {
            Field f = (Field)owner;
            return f.getDeclaringClass().getName()+ '#' + f.getName()+": " + f.getType().getName();
        } else if (owner instanceof Method) {
            Method m = (Method)owner;
            return m.getDeclaringClass().getName()+ '#' + m.getName()+
                    "("+String.join(", ", Stream.of(m.getParameterTypes()).map(c -> c.getName())
                    .collect(Collectors.toList())) + "): " +
                    m.getReturnType().getName();
        } else {
            return String.valueOf(owner);
        }
    }
}
