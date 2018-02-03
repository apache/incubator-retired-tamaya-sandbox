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

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.Filter;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class MaskFilter implements Filter{

    private String matches;
    private List<String> roles = new ArrayList<>();
    private String mask = "*****";
    private TargetPolicy policy = TargetPolicy.ALL;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class MaskFilterFactory implements ItemFactory<Filter> {
        @Override
        public String getName() {
            return "Mask";
        }

        @Override
        public Filter create(Map<String,String> parameters) {
            return new MaskFilter();
        }

        @Override
        public Class<? extends Filter> getType() {
            return Filter.class;
        }
    }

    public String getMatches() {
        return matches;
    }

    public MaskFilter setMatches(String matches) {
        this.matches = matches;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public MaskFilter setRoles(List<String> roles) {
        this.roles.clear();
        for(String role:roles) {
            this.roles.add(role.trim());
        }
        return this;
    }

    public MaskFilter setRoles(String... roles) {
        return setRoles(Arrays.asList(roles));
    }

    public MaskFilter setRoles(String roles){
        setRoles(roles.split(","));
        return this;
    }

    public String getMask() {
        return mask;
    }

    public MaskFilter setMask(String mask) {
        this.mask = mask;
        return this;
    }

    public TargetPolicy getPolicy() {
        return policy;
    }

    public MaskFilter setPolicy(TargetPolicy policy) {
        this.policy = policy;
        return this;
    }

    public MaskFilter setPolicy(String policy) {
        return setPolicy(TargetPolicy.valueOf(policy));
    }

    @Override
    public String filterProperty(String key, String value) {
        if(matches !=null){
            if(key.matches(matches)){
                return null;
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "HideFilter{" +
                "matches='" + matches + '\'' +
                '}';
    }

    private enum TargetPolicy {
        ALL,
        SINGLEVALUE,
        MULTIVALUE
    }
}
