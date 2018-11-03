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

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements a ini file format based on the APache Commons
 * {@link org.apache.commons.configuration.HierarchicalINIConfiguration}.
 */
public class IniConfigurationFormat implements ConfigurationFormat {

    @Override
    public String getName() {
        return "ini";
    }

    @Override
    public boolean accepts(URL url) {
        String fileName = url.getFile();
        return fileName.endsWith(".ini") || fileName.endsWith(".INI");
    }

    @Override
    public ConfigurationData readConfiguration(String name, InputStream inputStream) {
        PropertyValue data = PropertyValue.createObject();
        data.setMeta("name", name);
        try {
            HierarchicalINIConfiguration commonIniConfiguration;
            File file = new File(name);
            if (file.exists()) {
                commonIniConfiguration = new HierarchicalINIConfiguration(file);
            }else{
                commonIniConfiguration = new HierarchicalINIConfiguration(new URL(name));
            }
            for (String section : commonIniConfiguration.getSections()) {
                SubnodeConfiguration sectionConfig = commonIniConfiguration.getSection(section);
                PropertyValue sectionNode = ((ObjectValue) data).getOrSetField(section,
                        () -> PropertyValue.createObject(section));
                Map<String, String> properties = new HashMap<>();
                Iterator<String> keyIter = sectionConfig.getKeys();
                while (keyIter.hasNext()) {
                    String key = keyIter.next();
                    ((ObjectValue) sectionNode).setField(key, sectionConfig.getString(key));
                }
            }
        } catch (Exception e) {
            throw new ConfigException("Failed to parse ini-file format from " + name, e);
        }
        return new ConfigurationData(name, this, data);
    }
}
