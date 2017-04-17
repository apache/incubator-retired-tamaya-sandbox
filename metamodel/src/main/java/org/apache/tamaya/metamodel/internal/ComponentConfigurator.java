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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.logging.Logger;

/**
 * Small helper class for loading of configured instances.
 */
public final class ComponentConfigurator<T> {

    private static final Logger LOG = Logger.getLogger(ComponentConfigurator.class.getName());

    private ComponentConfigurator(){}

    /**
     * Configures the given instance with whatever is defined in the current child nodes.
     * @param instance the instance to be configured, not null.
     * @param node the node containing any configuration child nodes, not null.
     */
    public static void configure(Object instance, Node node) {
        NodeList entryNodes = node.getChildNodes();
        Map<String,String> params = new HashMap<>();
        for(int c=0;c<node.getAttributes().getLength();c++){
            Node attr = node.getAttributes().item(c);
            String key = attr.getNodeName();
            String value = attr.getNodeValue();
            params.put(key, value);
        }
        for(int c=0;c<entryNodes.getLength();c++) {
            Node filterNode = entryNodes.item(c);
            if(filterNode.getNodeType()!=Node.ELEMENT_NODE){
                continue;
            }
            if ("param".equals(filterNode.getNodeName())) {
                String key = filterNode.getAttributes().getNamedItem("name").getNodeValue();
                String value = filterNode.getTextContent();
                params.put(key, value);
            }
        }
        configure(instance, params);
    }

    /**
     * Configures the given instance with whatever is defined in the current child nodes.
     * @param instance the instance to be configured, not null.
     * @param params the node containing any configuration child nodes, not null.
     */
    public static void configure(Object instance, Map<String,String> params) {
        LOG.finest("Configuring instance: " + instance + " with " + params);
        for(Map.Entry<String,String> en:params.entrySet()){
            if(!params.isEmpty()){
                applyParam(instance, en.getKey(), en.getValue());
            }
        }
    }

    private static void applyParam(Object instance, String param, String value) {
        // TODO apply parameters to instance using reflection ,only if found.
    }

    public static Map<String, String> extractParameters(Node node) {
        Map<String,String> params = new HashMap<>();
        NamedNodeMap attributes = node.getAttributes();
        for(int c=0;c<attributes.getLength();c++) {
            Node pn = attributes.item(c);
            String key = pn.getNodeName();
            String value = pn.getNodeValue();
            params.put(key, value);
        }
        NodeList entryNodes = node.getChildNodes();
        for(int c=0;c<entryNodes.getLength();c++) {
            Node filterNode = entryNodes.item(c);
            if ("param".equals(filterNode.getNodeName())) {
                String key = filterNode.getAttributes().getNamedItem("name").getNodeValue();
                String value = filterNode.getTextContent();
                params.put(key, value);
            }
        }
        return params;
    }

}
