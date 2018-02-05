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

import org.apache.tamaya.ConfigException;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.base.filter.Filter;
import org.apache.tamaya.base.TamayaConfigBuilder;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.config.spi.ConfigBuilder;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Metaconfiguration reader that reads the configuration filters to be used.
 */
@Component
public class FilterReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(FilterReader.class.getName());

    @Override
    public void read(Document document, ConfigBuilder configBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("filters");
        if(nodeList.getLength()==0){
            LOG.finer("No filters configured.");
            return;
        }
        if(nodeList.getLength()>1){
            throw new ConfigException("Only one single filters section allowed.");
        }
        nodeList = nodeList.item(0).getChildNodes();
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeType()!=Node.ELEMENT_NODE) {
                continue;
            }
            String type = node.getNodeName();
            if ("defaults".equals(type)) {
                LOG.finer("Adding default filters...");
                TamayaConfigBuilder.from(configBuilder).addDiscoveredFilters();
                continue;
            }
            ItemFactory<Filter> filterFactory = ItemFactoryManager.getInstance().getFactory(Filter.class, type);
            if(filterFactory==null){
                LOG.severe("No such filter: " + type);
                continue;
            }
            Map<String,String> params = ComponentConfigurator.extractParameters(node);
            Filter filter = filterFactory.create(params);
            if(filter!=null) {
                ComponentConfigurator.configure(filter, params);
                LOG.finer("Adding configured filter: " + filter.getClass().getName());
                TamayaConfigBuilder.from(configBuilder).withFilters(filter);
            }
        }
    }


}
