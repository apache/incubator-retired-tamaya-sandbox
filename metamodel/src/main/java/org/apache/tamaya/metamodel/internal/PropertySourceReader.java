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
package org.apache.tamaya.metamodel.internal;

import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metaconfiguration reader to read property sources and property source providers.
 */
public class PropertySourceReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(PropertySourceReader.class.getName());

    @Override
    public void read(Document document, ConfigurationContextBuilder contextBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("property-sources");
        if(nodeList.getLength()==0){
            LOG.finer("No property sources configured.");
            return;
        }
        if(nodeList.getLength()>1){
            LOG.warning("Multiple property-sources sections configured, onyl reading first...");
            return;
        }
        nodeList = nodeList.item(0).getChildNodes();
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            try{
                if(node.getNodeName().equals("source")){
                    String type = node.getAttributes().getNamedItem("type").getNodeValue();
                    try {
                        ItemFactory<PropertySource> sourceFactory = ItemFactoryManager.getInstance().getFactory(PropertySource.class, type);
                        if(sourceFactory==null){
                            LOG.severe("No such property source: " + type);
                            continue;
                        }
                        Map<String,String> params = ComponentConfigurator.extractParameters(node);
                        PropertySource ps = sourceFactory.create(params);
                        if(ps!=null) {
                            ComponentConfigurator.configure(ps, params);
                            LOG.finer("Adding configured property source: " + ps.getName());
                            contextBuilder.addPropertySources(ps);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Failed to configure PropertySource: " + type, e);
                    }
                }else if(node.getNodeName().equals("source-provider")){
                    String type = node.getAttributes().getNamedItem("type").getNodeValue();
                    try {
                        ItemFactory<PropertySourceProvider> providerFactory = ItemFactoryManager.getInstance().getFactory(PropertySourceProvider.class, type);
                        if(providerFactory==null){
                            LOG.severe("No such property source provider: " + type);
                            continue;
                        }
                        Map<String,String> params = ComponentConfigurator.extractParameters(node);
                        PropertySourceProvider prov = providerFactory.create(params);
                        if(prov!=null) {
                            ComponentConfigurator.configure(prov, node);
                            LOG.finer("Adding configured property source provider: " + prov.getClass().getName());
                            contextBuilder.addPropertySources(prov.getPropertySources());
                        }
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Failed to configure PropertySourceProvider: " + type, e);
                    }
                }else if(node.getNodeName().equals("default-sources")){
                    LOG.finer("Adding default property sources.");
                    contextBuilder.addDefaultPropertySources();
                }
            }catch(Exception e){
                LOG.log(Level.SEVERE, "Failed to read property source configuration: " + node, e);
            }
        }
    }

}
