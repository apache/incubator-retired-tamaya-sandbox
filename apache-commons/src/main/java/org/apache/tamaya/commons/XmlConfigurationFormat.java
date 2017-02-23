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
package org.apache.tamaya.commons;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationDataBuilder;
import org.apache.tamaya.format.ConfigurationFormat;

import java.net.URL;
import java.util.Iterator;

/**
 * Implements a ini file format based on the APache Commons
 * {@link XMLConfiguration}.
 */
public class XmlConfigurationFormat implements ConfigurationFormat {

    @Override
    public String getName() {
        return "xml";
    }

    @Override
    public boolean accepts(URL url) {
        String fileName = url.getFile().toLowerCase();
        return fileName.endsWith(".xml");
    }

    @Override
    public ConfigurationData readConfiguration(URL url) {
        ConfigurationDataBuilder builder = ConfigurationDataBuilder.of(url.toString(), this);
        try {
            XMLConfiguration commonXmlConfiguration = new XMLConfiguration(url);
                Iterator<String> keyIter = commonXmlConfiguration.getKeys();
                while(keyIter.hasNext()){
                    String key = keyIter.next();
                    builder.addDefaultProperty(key, commonXmlConfiguration.getString(key));
            }
        } catch (ConfigurationException e) {
            throw new ConfigException("Failed to parse xml-file format from " + url, e);
        }
        return builder.build();
    }
}
