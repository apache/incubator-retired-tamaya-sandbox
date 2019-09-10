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

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


public class CachedFilterTest {

    @Test
    public void getSetMatches() {
        CachedFilter filter = new CachedFilter();
        assertThat(filter.getMatches()).isNull();
        filter.setMatches("foo");
        assertThat(filter.getMatches()).isNotNull().isEqualTo("foo");
    }

    @Test
    public void getSetMaxSize() {
        CachedFilter filter = new CachedFilter();
        assertThat(filter.getMaxSize()).isEqualTo(-1);
        filter.setMaxSize(10);
        assertThat(filter.getMaxSize()).isEqualTo(10);
    }

    @Test
    public void getSetTimeout() {
        CachedFilter filter = new CachedFilter();
        assertThat(filter.getTimeout()).isEqualTo(5);
        filter.setTimeout(10);
        assertThat(filter.getTimeout()).isEqualTo(10);
    }

    @Test
    public void getSetTineUnit() {
        CachedFilter filter = new CachedFilter();
        assertThat(filter.getTimeUnit()).isEqualTo(TimeUnit.MINUTES);
        assertThat(filter.setTimeUnit(TimeUnit.HOURS)).isSameAs(filter);
        assertThat(filter.getTimeUnit()).isEqualTo(TimeUnit.HOURS);
    }

    @Test
    public void filterNullProperty() {
        CachedFilter filter = new CachedFilter();
        PropertyValue val = filter.filterProperty(null, null);
        assertThat(val).isNull();
    }

    @Test
    public void filterExceedingProperty() {
        CachedFilter filter = new CachedFilter();
        filter.setMatches(".*");
        filter.setMaxSize(1);
        PropertyValue val = PropertyValue.createValue("a", "b");
        PropertyValue cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        PropertyValue val2 = PropertyValue.createValue("a", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        // exceeding property is not cached anymore
        val = PropertyValue.createValue("b", "b");
        cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        val2 = PropertyValue.createValue("b", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val2);
    }

    @Test
    public void filterWithTimeout() throws InterruptedException {
        CachedFilter filter = new CachedFilter();
        filter.setMatches(".*");
        filter.setTimeout(50).setTimeUnit(TimeUnit.MILLISECONDS);
        PropertyValue val = PropertyValue.createValue("a", "b");
        PropertyValue cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        PropertyValue val2 = PropertyValue.createValue("a", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        // After timeout cache entry is renewed...
        Thread.sleep(100L);
        val = PropertyValue.createValue("a", "b3");
        cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        val2 = PropertyValue.createValue("a", "b4");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val);
    }

    @Test
    public void filterMacthingProperty() {
        CachedFilter filter = new CachedFilter();
        filter.setMatches("a");
        PropertyValue val = PropertyValue.createValue("a", "b");
        PropertyValue cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        PropertyValue val2 = PropertyValue.createValue("a", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val);
    }

    @Test
    public void filterMacthingPropertyExpression() {
        CachedFilter filter = new CachedFilter();
        filter.setMatches("a.*");
        PropertyValue val = PropertyValue.createValue("a", "b");
        PropertyValue cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        PropertyValue val2 = PropertyValue.createValue("a", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val);

        val = PropertyValue.createValue("a2", "b");
        cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        val2 = PropertyValue.createValue("a2", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val);

        val = PropertyValue.createValue("b", "b");
        cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        val2 = PropertyValue.createValue("b", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val2);
    }

    @Test
    public void filterNonMacthingProperty() {
        CachedFilter filter = new CachedFilter();
        filter.setMatches("b");
        PropertyValue val = PropertyValue.createValue("a", "b");
        PropertyValue cached = filter.filterProperty(val, null);
        assertThat(cached).isNotNull().isEqualTo(val);
        PropertyValue val2 = PropertyValue.createValue("a", "b2");
        cached = filter.filterProperty(val2, null);
        assertThat(cached).isNotNull().isEqualTo(val2);
    }
}