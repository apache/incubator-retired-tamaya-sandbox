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
package org.apache.tamaya.propertysources;


import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormats;
import org.apache.tamaya.format.MappedConfigurationDataConfigSource;
import org.apache.tamaya.resource.AbstractPathConfigSourceProvider;

import javax.config.spi.ConfigSource;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A property provider implementation that tries to read all files in a directory as
 * configuration.
 */
public class ConfigDirConfigSourceProvider extends AbstractPathConfigSourceProvider {

    public ConfigDirConfigSourceProvider() {
        super(getConfigLocation());
    }

    private static String getConfigLocation() {
        String location = System.getProperty("configdir");
        if (location == null) {
            location = "./config";
        }
        if (!location.endsWith("/")) {
            location += "/";
        }
        if (!location.startsWith("file:")) {
            location = "file:" + location;
        }
        return location + "**/*.*";
    }

    @Override
    protected Collection<ConfigSource> getConfigSources(URL url) {
        try {
            ConfigurationData config = ConfigurationFormats.readConfigurationData(url);
            if (config == null) {
                Logger.getLogger(getClass().getName()).log(Level.INFO,
                        "Failed to read configuration from " + url);
                return Collections.emptySet();
            }
            return Collections.singleton(new MappedConfigurationDataConfigSource(config));
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Failed to read configuration from " + url, e);
            return Collections.emptySet();
        }
    }

}
