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

package org.apache.tamaya.metamodel.internal.factories;

import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormats;
import org.apache.tamaya.format.MappedConfigurationDataPropertySource;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.resource.ConfigResources;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.osgi.service.component.annotations.Component;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for configuring resource based property sources.
 */
@Component
public class ResourcePropertySourceProviderFactory implements ItemFactory<PropertySourceProvider>{

    private static final Logger LOG = Logger.getLogger(ResourcePropertySourceProviderFactory.class.getName());

    @Override
    public String getName() {
        return "resources";
    }

    @Override
    public PropertySourceProvider create(Map<String,String> parameters) {
        String location = parameters.get("location");
        if(location==null){
            LOG.warning("Cannot read 'location' from " + parameters + ", example: " + example());
            return null;
        }
        Collection<URL> resources = createResources(location);
        List<PropertySource> propertySources = new ArrayList<>();
        if(resources!=null) {
            String[] formats = getFormats(parameters);
            for(URL resource:resources) {
                ConfigurationData data;
                try {
                    if (formats.length == 0) {
                        data = ConfigurationFormats.readConfigurationData(resource);
                    } else {
                        data = ConfigurationFormats.readConfigurationData(resource,
                                ConfigurationFormats.getFormats(formats));
                    }
                    propertySources.add(new MappedConfigurationDataPropertySource(data));
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to read property source from resource: " + location, e);
                }
            }
        }
        final List<PropertySource> finalPropertySources = Collections.unmodifiableList(propertySources);
        return new PropertySourceProvider() {
            @Override
            public Collection<PropertySource> getPropertySources() {
                return finalPropertySources;
            }
        };
    }

    @Override
    public Class<? extends PropertySourceProvider> getType() {
        return PropertySourceProvider.class;
    }

    protected String[] getFormats(Map<String, String> parameters) {
        String val = parameters.get("formats");
        if(val==null){
            return new String[0];
        }
        String[] formats = val.split(",");
        for(int i=0;i<formats.length;i++){
            formats[i] = formats[i].trim();
        }
        return formats;
    }

    protected String example() {
        return "<resources location\"/META-INF/**/config.xml\"\n" +
                "          formats=\"xml-properties\"\n/>";
    }

    protected Collection<URL> createResources(String location) {
        try {
            return ConfigResources.getResourceResolver().getResources(location.split(":"));
        } catch (Exception e) {
            LOG.warning("Invalid resource expression '" + location + "'.");
            return null;
        }
    }

}
