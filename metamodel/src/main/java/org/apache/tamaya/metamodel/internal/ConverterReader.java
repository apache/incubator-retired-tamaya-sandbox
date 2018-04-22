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
import org.apache.tamaya.metamodel.spi.MetaConfigReader;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.config.spi.ConfigBuilder;
import javax.config.spi.Converter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metaconfiguration reader to read property sources and property source providers.
 */
@Component
public class ConverterReader implements MetaConfigReader {

    private static final Logger LOG = Logger.getLogger(ConverterReader.class.getName());

    @Override
    public void read(Document document, ConfigBuilder configBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("converters");
        if(nodeList.getLength()==0){
            LOG.finer("No converters configured");
            return;
        }
        if(nodeList.getLength()>1){
            throw new IllegalArgumentException("Only one single converters section allowed.");
        }
        nodeList = nodeList.item(0).getChildNodes();
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeType()!=Node.ELEMENT_NODE) {
                continue;
            }
            String type = node.getNodeName();
            if("defaults".equals(type)){
                LOG.finer("Adding default converters...");
                configBuilder.addDiscoveredConverters();
                continue;
            }
            try {
                ItemFactory<Converter> converterFactory = ItemFactoryManager.getInstance().getFactory(Converter.class, type);
                if(converterFactory==null){
                    LOG.severe("No such property converter: " + type);
                    continue;
                }
                Map<String,String> params = ComponentConfigurator.extractParameters(node);
                Converter converter = converterFactory.create(params);
                if(converter!=null) {
                    ComponentConfigurator.configure(converter, node);
                    Class targetType = Class.forName(params.get("targetType"));
                    LOG.finer("Adding converter for type " + targetType.getName() + ": " + converter.getClass());
                    configBuilder.withConverters(converter);
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure PropertyConverter: " + type, e);
            }
        }
    }

}
