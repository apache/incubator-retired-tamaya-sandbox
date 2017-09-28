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
import org.apache.tamaya.spisupport.PropertiesResourcePropertySource;
import org.apache.tamaya.spisupport.SimplePropertySource;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for configuring file based property sources.
 */
@Component
public final class FilePropertySourceFactory extends ResourcePropertySourceFactory{

    private static final Logger LOG = Logger.getLogger(FilePropertySourceFactory.class.getName());

    @Override
    public String getName() {
        return "file";
    }

    @Override
    protected String example() {
        return "<file location=\"c:/temp/config.xml\"\n" +
                "     formats=\"xml-properties\")>\n";
    }

    @Override
    protected URL createResource(String location) {
        try {
            Path path = Paths.get(location);
            if(!path.toFile().exists()){
                LOG.info("Cannot read file '" + location + "': no such file.");
            }else if(!path.toFile().canRead()){
                LOG.info("Cannot read file '" + location + "': not readable.");
            }
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            LOG.warning("Invalid file '" + location + "'.");
            return null;
        }
    }

}
