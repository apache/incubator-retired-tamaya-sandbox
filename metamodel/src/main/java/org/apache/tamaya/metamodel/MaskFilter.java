package org.apache.tamaya.metamodel;/*
 * (C) Copyright 2015-2017 Trivadis AG. All rights reserved.
 */

import org.apache.tamaya.metamodel.spi.ItemFactory;
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
public class MaskFilter implements PropertyFilter{

    private String matches;
    private List<String> roles = new ArrayList<>();
    private String mask = "*****";
    private TargetPolicy policy = TargetPolicy.ALL;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class MaskFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "Mask";
        }

        @Override
        public PropertyFilter create(Map<String,String> parameters) {
            return new MaskFilter();
        }

        @Override
        public Class<? extends PropertyFilter> getType() {
            return PropertyFilter.class;
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
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        if(matches !=null){
            if(value.getKey().matches(matches)){
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
