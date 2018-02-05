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
import org.apache.tamaya.base.ConfigValueCombinationPolicy;
import org.apache.tamaya.base.TamayaConfigBuilder;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.config.spi.ConfigBuilder;
import java.util.logging.Logger;


/**
 * Metaconfiguration reader that reads the configuration combination policy to be used.
 */
@Component
public class CombinationPolicyReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(CombinationPolicyReader.class.getName());

    @Override
    public void read(Document document, ConfigBuilder configBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("combination-policy");
        if(nodeList.getLength()==0){
            LOG.finest("No explicit combination policy configured, using default.");
            return;
        }
        if(nodeList.getLength()>1){
            throw new ConfigException("Only one combination policy can be applied.");
        }
        Node node = nodeList.item(0);
        String type = node.getAttributes().getNamedItem("class").getNodeValue();
        LOG.finest("Loading combination policy configured: " + type);
        ItemFactory<ConfigValueCombinationPolicy> policyFactory = ItemFactoryManager.getInstance().getFactory(ConfigValueCombinationPolicy.class, type);
        ConfigValueCombinationPolicy policy = policyFactory.create(ComponentConfigurator.extractParameters(node));
        ComponentConfigurator.configure(policy, node);
        TamayaConfigBuilder.from(configBuilder).withPropertyValueCombinationPolicy(policy);
    }


}
