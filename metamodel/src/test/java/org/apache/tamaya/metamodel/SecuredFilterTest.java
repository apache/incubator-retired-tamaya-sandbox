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

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import java.security.Security;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class SecuredFilterTest {

    private Set<String> testRoles = new HashSet<>(Collections.singletonList("test"));

    @Test
    public void getSetMatches() {
        SecuredFilter filter = new SecuredFilter();
        filter.setMatches("*.SEC");
        assertThat(filter.getMatches()).isNotNull().isEqualTo("*.SEC");
    }

    @Test
    public void getSetRoleSupplier() {
        Supplier<Set<String>> testSupplier = () -> testRoles;
        SecuredFilter filter = new SecuredFilter();
        assertThat(filter.getRoleSupplier()).isNotNull();
        assertThat(filter.getRoleSupplier().get()).isNull();
        filter.setRoleSupplier(testSupplier);
        assertThat(filter.getRoleSupplier()).isEqualTo(testSupplier);
    }

    @Test
    public void getSetRoles() {
        SecuredFilter filter = new SecuredFilter();
        filter.setRoles("myrole");
        assertThat(filter.getRoles()).containsExactly("myrole");
    }

    @Test
    public void setGetPolicy() {
        SecuredFilter filter = new SecuredFilter();
        filter.setPolicy(SecuredFilter.SecurePolicy.HIDE);
        assertThat(filter.getPolicy()).isEqualTo(SecuredFilter.SecurePolicy.HIDE);
    }

    @Test
    public void filterProperty_Hide() {
        SecuredFilter filter = new SecuredFilter();
        filter.setRoleSupplier(() -> testRoles);
        filter.setRoles("test2");
        filter.setPolicy(SecuredFilter.SecurePolicy.HIDE);
        filter.setMatches(".*\\.SEC");
        assertThat(filter.getMatches()).isEqualTo(".*\\.SEC");
        PropertyValue value = PropertyValue.createValue("foo.SEC", "someValue");
        PropertyValue filtered = filter.filterProperty(value,
                new FilterContext(value, Collections.emptyMap(), ConfigurationContext.EMPTY));
        assertThat(filtered).isNull();
    }

    @Test
    public void filterProperty_ThrowException_matching() {
        SecuredFilter filter = new SecuredFilter();
        filter.setRoleSupplier(() -> testRoles);
        filter.setRoles("test");
        filter.setPolicy(SecuredFilter.SecurePolicy.THROW_EXCPETION);
        filter.setMatches(".*\\.SEC");
        assertThat(filter.getMatches()).isEqualTo(".*\\.SEC");
        PropertyValue value = PropertyValue.createValue("foo.SEC", "someValue");
        assertThat(filter.filterProperty(value,
                new FilterContext(value, Collections.emptyMap(), ConfigurationContext.EMPTY)))
            .isEqualTo(value);
    }

    @Test(expected = ConfigException.class)
    public void filterProperty_ThrowException() {
        SecuredFilter filter = new SecuredFilter();
        filter.setRoleSupplier(() -> testRoles);
        filter.setRoles("test2");
        filter.setPolicy(SecuredFilter.SecurePolicy.THROW_EXCPETION);
        filter.setMatches(".*\\.SEC");
        assertThat(filter.getMatches()).isEqualTo(".*\\.SEC");
        PropertyValue value = PropertyValue.createValue("foo.SEC", "someValue");
        filter.filterProperty(value,
                new FilterContext(value, Collections.emptyMap(), ConfigurationContext.EMPTY));
    }

    @Test
    public void filterProperty_Warn() {
        SecuredFilter filter = new SecuredFilter();
        filter.setRoleSupplier(() -> testRoles);
        filter.setRoles("test2");
        filter.setPolicy(SecuredFilter.SecurePolicy.WARN_ONLY);
        filter.setMatches(".*\\.SEC");
        assertThat(filter.getMatches()).isEqualTo(".*\\.SEC");
        PropertyValue value = PropertyValue.createValue("foo.SEC", "someValue");
        PropertyValue filtered = filter.filterProperty(value,
                new FilterContext(value, Collections.emptyMap(), ConfigurationContext.EMPTY));
        assertThat(filtered).isNotNull();
        assertThat(filtered.getValue()).isNotNull().isEqualTo("someValue");
    }

    @Test
    public void filterProperty_NoRoleSupplier() {
        SecuredFilter filter = new SecuredFilter();
        filter.setRoles("test");
        filter.setPolicy(SecuredFilter.SecurePolicy.THROW_EXCPETION);
        filter.setMatches(".*\\.SEC");
        PropertyValue value = PropertyValue.createValue("foo.SEC", "someValue");
        assertThat(filter.filterProperty(value,
                new FilterContext(value, Collections.emptyMap(), ConfigurationContext.EMPTY)))
                .isEqualTo(value);
    }

    @Test
    public void testToString() {
        SecuredFilter filter = new SecuredFilter();
        filter.setMatches(".*\\.SEC");
        filter.setPolicy(SecuredFilter.SecurePolicy.HIDE);
        assertThat(filter.toString()).isEqualTo("SecuredFilter{matches='.*\\.SEC', roles='[]', policy='HIDE'}");
    }
}