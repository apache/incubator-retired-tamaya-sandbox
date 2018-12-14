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
package org.apache.tamaya.hjson;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.Objects;

/**
 * Visitor implementation to read a HJSON asString input source.
 */
class HJSONDataBuilder {

    private String resource;
    private PropertyValue data;
    private JsonValue root;

    HJSONDataBuilder(String resource, JsonValue root) {
        this.resource = Objects.requireNonNull(resource);
        this.root = root;
    }

    private void addJsonObject(JsonObject jsonObject, ObjectValue dataNode){
        jsonObject.forEach((m) -> {
            switch(m.getValue().getType()) {
                case BOOLEAN:
                    dataNode.setValue(m.getName(), String.valueOf(m.getValue().asBoolean()));
                    break;
                case NUMBER:
                    dataNode.setValue(m.getName(), String.valueOf(m.getValue().asDouble()));
                    break;
                case STRING:
                    dataNode.setValue(m.getName(),  m.getValue().asString());
                    break;
                case NULL:
                    dataNode.setValue(m.getName(), null);
                    break;
                case OBJECT:
                    ObjectValue oval = dataNode.setObject(m.getName());
                    addJsonObject((JsonObject)m.getValue(), oval);
                    break;
                case ARRAY:
                    ListValue aval = dataNode.setList(m.getName());
                    addArray((JsonArray)m.getValue(), aval);
                    break;
                default:
                    throw new ConfigException("Internal failure while processing JSON document.");
            }
        });
    }

    private void addArray(JsonArray array, ListValue dataNode) {
        array.forEach(val -> {
            switch(val.getType()) {
                case NULL:
                    break;
                case BOOLEAN:
                    dataNode.addValue(String.valueOf(val.asBoolean()));
                    break;
                case NUMBER:
                case STRING:
                    dataNode.addValue(val.toString());
                    break;
                case OBJECT:
                    ObjectValue oval = dataNode.addObject();
                    addJsonObject((JsonObject)val, oval);
                    break;
                case ARRAY:
                    ListValue aval = dataNode.addList();
                    addArray((JsonArray)val, aval);
                    break;
                default:
                    throw new ConfigException("Internal failure while processing JSON document.");
            }
        });
    }

    public PropertyValue build() {
        if (root instanceof JsonObject) {
            data = PropertyValue.createObject("");
            addJsonObject((JsonObject)root, (ObjectValue) data);
        } else if (root instanceof JsonArray) {
            JsonArray array = (JsonArray)root;
            data = PropertyValue.createList("");
            addArray(array, (ListValue)data);
        } else {
            throw new ConfigException("Unknown JsonType encountered: " + root.getClass().getName());
        }
        data.setMeta("resource", resource);
        data.setMeta("format", "json");
        return data;
    }

}
