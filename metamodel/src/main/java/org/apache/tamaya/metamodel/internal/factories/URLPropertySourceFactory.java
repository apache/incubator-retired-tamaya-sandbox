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
import org.apache.tamaya.functions.Supplier;
import org.apache.tamaya.metamodel.internal.ComponentConfigurator;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spisupport.PropertiesResourcePropertySource;

import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for configuring resource based property sources.
 */
public class URLPropertySourceFactory implements ItemFactory<PropertySource>{

    private static final Logger LOG = Logger.getLogger(FilePropertySourceFactory.class.getName());

    @Override
    public String getName() {
        return "url";
    }

    @Override
    public PropertySource create(Map<String,String> parameters) {
        String location = parameters.get("location");
        if(location==null){
            LOG.warning("Cannot read 'location' from " + parameters + ", example: " + example());
            return null;
        }
        URL resource = createResource(location);
        if(resource!=null) {
            String[] formats = getFormats(parameters);
            String name = resource.toString();
            RefreshablePropertySource ps = new RefreshablePropertySource(name, new LazyDataSupplier(resource, formats));
            ComponentConfigurator.configure(ps, parameters);
            return ps;
        }
        return null;
    }

    protected String example() {
        return "<source type=\""+getName()+"\">\n" +
                "  <param name=\"location\">http://127.0.0.1:1110/config.xml</param>\n" +
                "  <param name=\"formats\">xml-properties</param>\n" +
                "</source>\n";
    }

    protected URL createResource(String location) {
        try {
            return new URL(location);
        } catch (MalformedURLException e) {
            LOG.warning("Invalid url '" + location + "'.");
            return null;
        }
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

    @Override
    public Class<? extends PropertySource> getType() {
        return PropertySource.class;
    }

    private static final class LazyDataSupplier implements Supplier<ConfigurationData> {

        private String[] formats;
        private URL resource;

        public LazyDataSupplier(URL resource, String[] formats) {
            this.formats = Objects.requireNonNull(formats);
            this.resource = Objects.requireNonNull(resource);
        }

        @Override
        public ConfigurationData get() {
            ConfigurationData data;
            try {
                if (formats.length == 0) {
                    data = ConfigurationFormats.readConfigurationData(resource);
                } else {
                    data = ConfigurationFormats.readConfigurationData(resource,
                            ConfigurationFormats.getFormats(formats));
                }
                return data;
            } catch (Exception e) {
                LOG.log(Level.INFO, "Failed to read property source from resource: " + resource, e);
                return null;
            }
        }
    }

    private static final class RefreshablePropertySource extends MappedConfigurationDataPropertySource
    implements Refreshable{

        public RefreshablePropertySource(String name, Supplier<ConfigurationData> dataSupplier) {
            super(name, dataSupplier);
        }

        public RefreshablePropertySource(String name, int defaultOrdinal, Supplier<ConfigurationData> dataSupplier) {
            super(name, dataSupplier);
        }

        @Override
        public boolean isCurrent() {
            return false;
        }

        @Override
        public void refresh() throws RefreshFailedException {
            super.load();
        }
    }
}
