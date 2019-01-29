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

import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HJSONVisitorTest {

    @Test
    public void ensureJSONisParsedProperlyWithDifferentValueTypesFilteringOutEmptyValues() {
        JsonObject startNode = new JsonObject().//
                add("key.sub", "createValue").//
                add("anotherKey", true).//
                add("notAnotherKey", false).//
                add("number", 4711).//
                add("null", JsonValue.NULL).//
                add("empty", "");
        HJSONDataBuilder visitor = new HJSONDataBuilder("Test:ensureJSONisParsedProperlyWithDifferentValueTypesFilteringOutEmptyValues", startNode);

        PropertyValue data = visitor.build();
        assertThat(data).isNotNull();

        ObjectValue ov = data.toObjectValue();
        assertThat(ov.getValues()).hasSize(6);
        assertThat(data.toMap()).containsKeys("key.sub", "anotherKey", "notAnotherKey", "number", "null")
                .containsEntry("key.sub", "createValue")
                .containsEntry("null", null)
                .containsEntry("anotherKey", "true")
                .doesNotContainEntry("empty", null);
    }

    @Test
    public void parsingWorksOnEmptyObject() {
        JsonObject startNode = new JsonObject();

        Map<String, String> targetStore = new HashMap<>();

        HJSONDataBuilder visitor = new HJSONDataBuilder("Test:parsingWorksOnEmptyObject", startNode);
        PropertyValue data = visitor.build();
        assertThat(data).isNotNull();
        assertThat(data.isLeaf()).isFalse();
    }

    @Test
    public void arrayInObject() {
        JsonObject startNode = new JsonObject().
                add("arrayKey", new JsonArray());
        HJSONDataBuilder visitor = new HJSONDataBuilder("Test:array", startNode);
        PropertyValue data = visitor.build();
        assertThat(data).isNotNull();
        System.out.println(data.asString());
    }

    @Test
    public void array() {
        JsonArray startNode = new JsonArray().
                add(new JsonObject().add("k1", 1).add("k2", 2)).
                add(new JsonObject().add("k1", 1).add("k2", 2)).
                add(new JsonArray().add(new JsonObject().add("k31", "v31").add("k32", false)));
        HJSONDataBuilder visitor = new HJSONDataBuilder("Test:array", startNode);
        PropertyValue data = visitor.build();
        assertThat(data).isNotNull();
        System.out.println(data.asString());
    }

}
