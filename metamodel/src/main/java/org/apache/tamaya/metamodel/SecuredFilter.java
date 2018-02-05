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
import org.apache.tamaya.base.filter.Filter;

import javax.security.auth.Subject;
import java.security.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simple filter that never changes a key/value pair returned, regardless if a value
 * is changing underneath, hereby different values for single and multi-property access
 * are considered.
 */
public class SecuredFilter implements Filter{

    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    private String matches;
    private String roles;
    private String[]rolesArray;
    private SecurePolicy policy = SecurePolicy.HIDE;

    /**
     * Factory for configuring immutable property filter.
     */
    public static final class SecuredFilterFactory implements ItemFactory<Filter> {
        @Override
        public String getName() {
            return "Secured";
        }

        @Override
        public Filter create(Map<String,String> parameters) {
            return new SecuredFilter();
        }

        @Override
        public Class<? extends Filter> getType() {
            return Filter.class;
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
    public String filterProperty(String key, String value) {
        if(matches !=null){
            if(!key.matches(matches)) {
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
                throw new IllegalStateException("Unauthorized access to '"+key+"', not in " + roles);
            case WARN_ONLY:
                LOG.warning("Unauthorized access to '"+key+"', not in " + roles);
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
