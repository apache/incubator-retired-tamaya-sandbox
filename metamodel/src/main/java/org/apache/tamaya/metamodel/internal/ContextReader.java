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

import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Priority;
import java.util.logging.Logger;

/**
 * Meta-configuration reader that reads the shared context data.
 */
@Priority(-1)
public class ContextReader implements MetaConfigurationReader {

    private static final Logger LOG = Logger.getLogger(ContextReader.class.getName());

    @Override
    public void read(Document document, ConfigurationContextBuilder contextBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("context");
        String contextName = "DEFAULT";
        LOG.finer("Reading " + nodeList.getLength() + " meta context entries...");
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeName().equals("context")){
                Node nameNode = node.getAttributes().getNamedItem("name");
                if(nameNode!=null){
                    contextName = nameNode.getTextContent();
                }
                MetaContext context = MetaContext.getInstance(contextName);
                NodeList entryNodes = node.getChildNodes();
                for(int c=0;c<entryNodes.getLength();c++){
                    Node entryNode = entryNodes.item(c);
                    if("context-entry".equals(entryNode.getNodeName())){
                        String key = entryNode.getAttributes().getNamedItem("name").getNodeValue();
                        String value = entryNode.getTextContent();
                        // TODO add support for placeholders here...
                        LOG.finest("Applying context entry: " + key + '=' + value + " on " + contextName);
                        context.setProperty(key, value);
                    }
                }
            }
        }
    }
}
