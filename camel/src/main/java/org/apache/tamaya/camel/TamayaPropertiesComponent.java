/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.camel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.tamaya.Configuration;

/**
 * Default Camel PropertiesComponent that additionally has cfg and tamaya prefixes configured for resolution of
 * entries from tamaya.
 */
public class TamayaPropertiesComponent extends PropertiesComponent{

    private ClassLoader classLoader;
    private List<TamayaPropertyResolver> resolvers = new ArrayList<>();

    /**
     * Constructor similar to parent.
     */
    public TamayaPropertiesComponent(){
        super();
        resolvers.add(new TamayaPropertyResolver("tamaya"));
        resolvers.add(new TamayaPropertyResolver("cfg"));
        for(TamayaPropertyResolver resolver:resolvers) {
            resolver.init(getClassLoader());
            addFunction(resolver);
        }
        setTamayaOverrides(true);
    }

    /**
     * Constructor similar to parent with additional locations.
     * @param locations additional locations for Camel.  
     */
    public TamayaPropertiesComponent(String ... locations){
        super(locations);
        resolvers.add(new TamayaPropertyResolver("tamaya"));
        resolvers.add(new TamayaPropertyResolver("cfg"));
        for(TamayaPropertyResolver resolver:resolvers) {
            resolver.init(getClassLoader());
            addFunction(resolver);
        }
        setTamayaOverrides(true);
    }

    /**
     * Constructor similar to parent with only one location.
     * @param location addition location for Camel.
     */
    public TamayaPropertiesComponent(String location){
        super(location);
        resolvers.add(new TamayaPropertyResolver("tamaya"));
        resolvers.add(new TamayaPropertyResolver("cfg"));
        for(TamayaPropertyResolver resolver:resolvers) {
            resolver.init(getClassLoader());
            addFunction(resolver);
        }
        setTamayaOverrides(true);
    }

    /**
     * Apply the current Tamaya properties (configuration) as override properties evaluated first by camel before
     * evaluating other uris.
     * @param enabled flag to define if tamaya values override everything else.
     */
    public void setTamayaOverrides(boolean enabled){
        if(enabled){
            final Properties props = new Properties();
            props.putAll(Configuration.current(getClassLoader()).getProperties());
            setOverrideProperties(props);
        } else{
            setOverrideProperties(null);
        }
    }

    private ClassLoader getClassLoader(){
        CamelContext camelContext = getCamelContext();
        ClassLoader cl = null;
        if(camelContext!=null){
            cl = camelContext.getApplicationContextClassLoader();
        }
        if(cl==null){
            cl = Thread.currentThread().getContextClassLoader();
        }
        if(cl==null){
            cl = getClass().getClassLoader();
        }
        return cl;
    }
}
