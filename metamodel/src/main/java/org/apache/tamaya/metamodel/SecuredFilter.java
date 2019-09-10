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
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Simple filter that never changes a key/createValue pair returned, regardless if a createValue
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class SecuredFilter implements PropertyFilter{

    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    private String matches;
    private Set<String> roles = new HashSet<>();
    private Supplier<Set<String>> roleSupplier = () -> null;
    private SecurePolicy policy = SecurePolicy.HIDE;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class SecuredFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "secured";
        }

        @Override
        public PropertyFilter create(Map<String,String> parameters) {
            return new SecuredFilter();
        }

        @Override
        public Class<? extends PropertyFilter> getType() {
            return PropertyFilter.class;
        }
    }

    public String getMatches() {
        return matches;
    }

    public SecuredFilter setMatches(String matches) {
        this.matches = matches;
        return this;
    }

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public SecuredFilter setRoles(String... roles) {
        return setRoles(Arrays.asList(roles));
    }

    public SecuredFilter setRoles(Collection<String> roles) {
        this.roles.addAll(roles);
        return this;
    }

    public SecurePolicy getPolicy() {
        return policy;
    }

    public SecuredFilter setPolicy(SecurePolicy policy) {
        this.policy = policy;
        return this;
    }

    public Supplier<Set<String>> getRoleSupplier() {
        return roleSupplier;
    }

    public void setRoleSupplier(Supplier<Set<String>> roleSupplier) {
        this.roleSupplier = Objects.requireNonNull(roleSupplier);
    }

    @Override
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        if(matches !=null){
            if(!value.getKey().matches(matches)) {
                return value;
            }
        }
        Set<String> assignedRoles = this.roleSupplier.get();
        if(assignedRoles!=null) {
            for (String role : this.roles) {
                if (assignedRoles.contains(role)) {
                    return value;
                }
            }
            switch (policy) {
                case THROW_EXCPETION:
                    throw new ConfigException("Unauthorized access to '" + value.getKey() + "', not in " + roles);
                case WARN_ONLY:
                    LOG.warning("Unauthorized access to '" + value.getKey() + "', not in " + roles);
                    return value;
                case HIDE:
                default:
                    return null;
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "SecuredFilter{" +
                "matches='" + matches + '\'' +
                ", roles='" + roles + '\'' +
                ", policy='" + policy + '\'' +
                '}';
    }

    /**
     * The security policy for filtering meta values.
     */
    public enum SecurePolicy{
        HIDE,
        WARN_ONLY,
        THROW_EXCPETION
    }
}
