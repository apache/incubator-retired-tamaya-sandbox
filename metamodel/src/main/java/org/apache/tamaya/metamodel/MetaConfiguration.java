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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ServiceContextManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Accessor singleton for accessing/loading getMeta-configuration.
 */
public final class MetaConfiguration {

    private static final String CONFIG_RESOURCE = "tamaya-config.xml";

    private static final Logger LOG = Logger.getLogger(MetaConfiguration.class.getName());

    /**
     * Singleton constructor.
     */
    private MetaConfiguration(){}

    /**
     * Creates a new {@link Configuration} using {@link #createConfiguration(URL)}
     * and applies it as default configuration using {@link Configuration#current()} (Configuration)}.
     */
    public static void configure(){
        LOG.info("TAMAYA: Checking for getMeta-configuration...");
        URL configFile = getDefaultMetaConfig();
        if(configFile==null){
            LOG.warning("TAMAYA: No " + CONFIG_RESOURCE + " found, using defaults.");
        }
        configure(configFile);
    }

    /**
     * Creates a new {@link Configuration} using {@link #createConfiguration(URL)}
     * and applies it as default configuration using {@link Configuration#setCurrent(Configuration)} }.
     * @param metaConfig URL for loading the {@code tamaya-config.xml} getMeta-configuration.
     */
    public static void configure(URL metaConfig){
        try {
            // Let readers do their work
            Configuration config = createConfiguration(metaConfig);
            Configuration.setCurrent(config);
        }catch(Exception e){
            LOG.log(Level.SEVERE, "TAMAYA: Error loading configuration.", e);
        }
    }

    private static URL getDefaultMetaConfig() {
        // 1: check tamaya-config system property
        String tamayaConfig = System.getProperty("tamaya-config");
        if(tamayaConfig!=null){
            File file = new File(tamayaConfig);
            if(!file.exists() || !file.canRead() || !file.isFile()){
                LOG.severe("TAMAYA: Not a valid config file: " + tamayaConfig);
            }else{
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    LOG.severe("TAMAYA: Invalid file name: " + tamayaConfig);
                }
            }
        }
        return MetaConfiguration.class.getClassLoader().getResource(CONFIG_RESOURCE);
    }

    /**
     * Performs initialization of a new configuration
     * context to the {@link MetaConfigurationReader} instances found in the current
     * {@link org.apache.tamaya.spi.ServiceContext} and returns the corresponding builder
     * instance.
     * @param metaConfig URL for loading the {@code tamaya-config.xml} getMeta-configuration.
     * @return a new configuration context builder, never null.
     * @throws ConfigException If the URL cannot be read.
     */
    public static ConfigurationBuilder createConfigBuilder(URL metaConfig){
        URL configFile = Objects.requireNonNull(metaConfig);
        LOG.info("TAMAYA: Loading tamaya-config.xml...");
        Document document = null;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);

            document = factory.newDocumentBuilder().parse(configFile.openStream());
            ConfigurationBuilder builder = Configuration.createConfigurationBuilder();
            for(MetaConfigurationReader reader: ServiceContextManager.getServiceContext().getServices(
                    MetaConfigurationReader.class)){
                LOG.fine("TAMAYA: Executing MetaConfig-Reader: " + reader.getClass().getName() + "...");
                reader.read(document, builder);
            }
            return builder;
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new ConfigException("Cannot read getMeta-config deom " + metaConfig, e);
        }
    }

    /**
     * Reads the getMeta-configuration and delegates initialization of the current configuration
     * context to the {@link MetaConfigurationReader} instances found in the current
     * {@link org.apache.tamaya.spi.ServiceContext}.
     * @param metaConfig URL for loading the {@code tamaya-config.xml} getMeta-configuration.
     * @return the new configuration instance.
     */
    public static Configuration createConfiguration(URL metaConfig){
        return createConfigBuilder(metaConfig).build();
    }

}
