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
import org.apache.tamaya.base.TamayaConfigBuilder;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Priority;
import javax.config.spi.ConfigBuilder;
import java.util.Comparator;
import java.util.logging.Logger;


/**
 * Metaconfiguration reader that reads the configuration combination policy to be used.
 */
@Component
@Priority(Integer.MAX_VALUE)
public class FilterOrderingReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(FilterOrderingReader.class.getName());

    @Override
    public void read(Document document, ConfigBuilder configBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("filter-order");
        if(nodeList.getLength()==0){
            LOG.finer("No filter ordering configured.");
            return;
        }
        if(nodeList.getLength()>1){
            throw new IllegalArgumentException("Only one filter order can be applied.");
        }
        Node node = nodeList.item(0);
        String type = node.getAttributes().getNamedItem("type").getNodeValue();
        ItemFactory<Comparator> comparatorFactory = ItemFactoryManager.getInstance().getFactory(Comparator.class, type);
        Comparator comparator = comparatorFactory.create(ComponentConfigurator.extractParameters(node));
        ComponentConfigurator.configure(comparator, node);
        LOG.finer("Sorting filters using comparator: " + comparator.getClass().getName());
        TamayaConfigBuilder.from(configBuilder).sortFilter(comparator);
    }


}
