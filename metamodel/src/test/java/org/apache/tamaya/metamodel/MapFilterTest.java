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
package org.apache.tamaya.metamodel;

import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MapFilterTest {

    private MapFilter filter = new MapFilter();

    @Test
    public void getSetTarget() {
        assertThat(filter.getTarget()).isNull();
        filter.setTarget("target:");
        assertThat(filter.getTarget()).isEqualTo("target:");
    }

    @Test
    public void getSetCutoff() {
        assertThat(filter.getCutoff()).isNull();
        filter.setCutoff("cutoff");
        assertThat(filter.getCutoff()).isEqualTo("cutoff");
    }

    @Test
    public void getSetMatches() {
        filter.setMatches("*.SEC");
        assertThat(filter.getMatches()).isNotNull().isEqualTo("*.SEC");
    }

    @Test
    public void filterProperty_NoMap() {
        PropertyValue pv = PropertyValue.createValue("k", "v");
        PropertyValue value = filter.filterProperty(pv, null);
        assertThat(value).isNotNull().isEqualTo(pv);
    }

    @Test
    public void filterProperty_Mapped() {
        PropertyValue pv = PropertyValue.createValue("k", "v");
        filter.setTarget("a:");
        PropertyValue value = filter.filterProperty(pv, null);
        assertThat(value).isNotNull().isEqualTo(PropertyValue.createValue("a:k", "v"));
    }

    @Test
    public void filterProperty_Cutoff() {
        PropertyValue pv = PropertyValue.createValue("a.b:k", "v");
        filter.setCutoff("a.b:");
        PropertyValue value = filter.filterProperty(pv, null);
        assertThat(value).isNotNull().isEqualTo(PropertyValue.createValue("k", "v"));
    }

    @Test
    public void filterProperty_CutoffAndTarget() {
        PropertyValue pv = PropertyValue.createValue("a.b:k", "v");
        filter.setCutoff("a.b:");
        filter.setTarget("new.");
        PropertyValue value = filter.filterProperty(pv, null);
        assertThat(value).isNotNull().isEqualTo(PropertyValue.createValue("new.k", "v"));
    }


    @Test
    public void testToString() {
        assertThat(filter.toString()).isNotNull().isEqualTo("MapFilter{target='null', cutoff='null', matches='null'}");
        filter.setTarget("tgt").setCutoff("cutoff").setMatches("matches");
        assertThat(filter.toString()).isNotNull().isEqualTo("MapFilter{target='tgt', cutoff='cutoff', matches='matches'}");
    }
}