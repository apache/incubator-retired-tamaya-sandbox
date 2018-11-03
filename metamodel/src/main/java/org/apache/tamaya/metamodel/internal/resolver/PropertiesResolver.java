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
package org.apache.tamaya.metamodel.internal.resolver;

import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.spi.SimpleResolver;
import org.osgi.service.component.annotations.Component;

import java.net.URI;

/**
 * Simple resolver for {@link org.apache.tamaya.metamodel.MetaContext} entries that
 * reads data from system and environment properties.
 *
 * Valid inputs are:
 * <ul>
 *     <li>{@code ${properties:sys:key?default=abcval} } reading a system property.</li>
 *     <li>{@code ${properties:env:key?default=abcval} } reading a environment property.</li>
 *     <li>{@code ${properties:ctx:[ctxName:]key?default=abcval} } reading a <i>default</i> MetaContext entry.</li>
 * </ul>
 *
 * Hereby the _default_ parameter defines the default createValue to be applied, if no createValue was found.
 */
@Component
public final class PropertiesResolver implements SimpleResolver{
    @Override
    public String getResolverId() {
        return "properties";
    }

    @Override
    public String evaluate(String expression) {
        String[] mainParts = expression.split("\\?",2);
        if(mainParts.length==1){
            return evaluate(expression, null);
        }else{
            return evaluate(mainParts[0], mainParts[1].trim().substring("default=".length()));
        }

    }

    private String evaluate(String expression, String defaultValue) {
        String[] parts = expression.split(":", 2);
        if(parts.length<2){
            return null;
        }
        switch(parts[0]){
            case "system":
                return System.getProperty(parts[1],defaultValue);
            case "env":
                String val = System.getenv(parts[1]);
                if(val==null){
                    return defaultValue;
                }
                return val;
            case "ctx":
                return MetaContext.getInstance()
                        .getProperty(parts[1], defaultValue);
            default:
                return null;
        }
    }
}
