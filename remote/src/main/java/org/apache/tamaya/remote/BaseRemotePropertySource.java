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
package org.apache.tamaya.remote;

import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.apache.tamaya.json.JSONFormat;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for implementing a PropertySource that reads configuration data from a remote resource. It uses
 * by default the JSON format as defined by the JSON module.
 */
public abstract class BaseRemotePropertySource implements PropertySource{

    private static final ConfigurationFormat DEFAULT_FORMAT = new JSONFormat();

    private Map<String,String> properties = new HashMap<>();

    protected BaseRemotePropertySource(){
        reload();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        return PropertyValue.map(properties, getName());
    }

    /**
     * Reloads the remote configuration. If reloads fails to whatever reasons the already loaded configuration will
     * stay untouched.
     */
    public void reload(){
        ConfigurationFormat format = getConfigurationFormat();
        for(URL url:getAccessURLs()) {
            try (InputStream is = url.openStream()) {
                ConfigurationData data = format.readConfiguration(url.toString(), is);
                if(data!=null){
                    Map<String,String> newProperties = mapConfigurationData(data);
                    // the configs served by the tamaya server module has a 'data' root section containing the
                    // config  entries. if not present, we assume an alternate format, which is sued as is...
                    if(!newProperties.isEmpty()){
                        this.properties = newProperties;
                        Logger.getLogger(getClass().getName()).info(
                                "Reloaded remote config from: " + url + ", entries read: " + this.properties.size());
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to load config from url: " + url, e);
            }
        }
    }

    protected abstract Collection<URL> getAccessURLs();

    protected ConfigurationFormat getConfigurationFormat(){
        return DEFAULT_FORMAT;
    }

    protected Map<String,String> mapConfigurationData(ConfigurationData data){
        Map<String,String> readProperties = new HashMap<>();
        if(data!=null){
            for(PropertyValue val:data.getData()) {
                readProperties.putAll(val.toMap());
            }
            Map<String,String> newProperties = new HashMap<>();
            for(Map.Entry<String,String> en:readProperties.entrySet()){
                // filter data entries
                newProperties.put(en.getKey(), en.getValue());
            }
            // the configs served by the tamaya server module has a 'data' root section containing the
            // config  entries. if not present, we assume an alternate format, which is sued as is...
            if(newProperties.isEmpty()){
                Logger.getLogger(getClass().getName()).info(
                        "Loaded remote config from: " + data.getResource() + ", does not have a data section, using as is...");
                newProperties = readProperties;
            }
            Logger.getLogger(getClass().getName()).info(
                    "Reloaded remote config from: " + data.getResource() + ", entriea read: " + this.properties.size());
            return newProperties;
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean isScannable(){
        return true;
    }

    @Override
    public PropertyValue get(String key) {
        return getProperties().get(key);
    }

    public int getOrdinal(){
        PropertyValue configuredOrdinal = get(TAMAYA_ORDINAL);
        if(configuredOrdinal!=null){
            try{
                return Integer.parseInt(configuredOrdinal.getValue());
            } catch(Exception e){
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Configured Ordinal is not an int number: " + configuredOrdinal, e);
            }
        }
        return getDefaultOrdinal();
    }

    /**
     * Returns the  default ordinal used, when no ordinal is setCurrent, or the ordinal was not parseable to an int createValue.
     * @return the  default ordinal used, by default 0.
     */
    public int getDefaultOrdinal(){
        return 0;
    }

}
