package org.apache.tamaya.metamodel;/*
 * (C) Copyright 2015-2017 Trivadis AG. All rights reserved.
 */

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.FilterContext;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;

import javax.security.auth.Subject;
import java.security.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class SecuredFilter implements PropertyFilter{

    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    private String matches;
    private String roles;
    private String[]rolesArray;
    private SecurePolicy policy = SecurePolicy.HIDE;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class SecuredFilterFactory implements ItemFactory<PropertyFilter> {
        @Override
        public String getName() {
            return "Secured";
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

    public String getRoles() {
        return roles;
    }

    public SecuredFilter setRoles(String roles) {
        this.roles = roles;
        this.rolesArray = roles.split(",");
        return this;
    }

    public SecurePolicy getPolicy() {
        return policy;
    }

    public SecuredFilter setPolicy(SecurePolicy policy) {
        this.policy = policy;
        return this;
    }

    @Override
    public PropertyValue filterProperty(PropertyValue value, FilterContext context) {
        if(matches !=null){
            if(!value.getKey().matches(matches)) {
                return value;
            }
        }
        Subject s = javax.security.auth.Subject.getSubject(AccessController.getContext());
        for(Principal principal:s.getPrincipals()){
            for(String role:rolesArray) {
                if(principal.getName().equals(role)){
                    return value;
                }
            }
        }
        switch(policy){
            case THROW_EXCPETION:
                throw new ConfigException("Unauthorized access to '"+value.getKey()+"', not in " + roles);
            case WARN_ONLY:
                LOG.warning("Unauthorized access to '"+value.getKey()+"', not in " + roles);
                return value;
            case HIDE:
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "SecuredFilter{" +
                "matches='" + matches + '\'' +
                ", roles='" + roles + '\'' +
                ", policy='" + policy + '\'' +
                '}';
    }

    public enum SecurePolicy{
        HIDE,
        WARN_ONLY,
        THROW_EXCPETION
    }
}
