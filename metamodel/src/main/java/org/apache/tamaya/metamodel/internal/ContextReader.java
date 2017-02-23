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
import org.apache.tamaya.metamodel.spi.SimpleResolver;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.ServiceContextManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Priority;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * Meta-configuration reader that reads the shared context data.
 */
@Priority(-1)
public class ContextReader implements MetaConfigurationReader {

    private static final Logger LOG = Logger.getLogger(ContextReader.class.getName());

    private Map<String,SimpleResolver> resolvers = new ConcurrentHashMap<>();

    public ContextReader(){
        for(SimpleResolver resolver: ServiceContextManager.getServiceContext()
                .getServices(SimpleResolver.class)){
            this.resolvers.put(resolver.getResolverId(), resolver);
        }
    }

    public void addResolver(SimpleResolver resolver){
        if(!this.resolvers.containsKey(resolver.getResolverId())) {
            this.resolvers.put(resolver.getResolverId(), resolver);
        }
    }

    public void removeResolver(SimpleResolver resolver){
        this.resolvers.remove(resolver.getResolverId());
    }

    public Set<String> getResolverIds(){
        return this.resolvers.keySet();
    }

    public SimpleResolver getResolver(String resolverKey){
        return this.resolvers.get(resolverKey);
    }

    @Override
    public void read(Document document, ConfigurationContextBuilder contextBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("context");
        String contextName = null;
        LOG.finer("Reading " + nodeList.getLength() + " meta context entries...");
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeName().equals("context")){
                Node nameNode = node.getAttributes().getNamedItem("name");
                if(nameNode!=null){
                    contextName = nameNode.getTextContent();
                }
                MetaContext context = contextName!=null?MetaContext.getInstance(contextName):MetaContext.getDefaultInstance();
                NodeList entryNodes = node.getChildNodes();
                for(int c=0;c<entryNodes.getLength();c++){
                    Node entryNode = entryNodes.item(c);
                    if("context-entry".equals(entryNode.getNodeName())){
                        String key = entryNode.getAttributes().getNamedItem("name").getNodeValue();
                        String value = entryNode.getTextContent();
                        resolvePlaceholders(value);
                        LOG.finest("Applying context entry: " + key + '=' + value + " on " + contextName);
                        context.setProperty(key, value);
                    }
                }
            }
        }
    }

    private String resolvePlaceholders(String value) {
        StringBuilder result = new StringBuilder();
        StringBuilder exp = new StringBuilder();
        final int INVALUE = 0;
        final int BEFORE_EXP = 1;
        final int INEXP = 2;
        int state = INVALUE;
        StringTokenizer tokenizer = new StringTokenizer(value, "${}", true);
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            switch(token){
                case "$":
                    switch(state){
                        case INVALUE:
                        default:
                            state = BEFORE_EXP;
                            break;
                        case BEFORE_EXP: // escaped
                            result.append(token);
                            state = INVALUE;
                            break;
                        case INEXP:
                            exp.append(token);
                            break;
                    }
                    break;
                case "{":
                    switch(state){
                        case BEFORE_EXP:
                            state = INEXP;
                            break;
                        case INVALUE:
                        case INEXP:
                        default:
                            result.append(token);
                            break;
                    }
                case "}":
                    switch(state){
                        case INVALUE:
                            result.append(token);
                            break;
                        case INEXP:
                            result.append(evaluateExpression(exp.toString()));
                            exp.setLength(0);
                            state = INVALUE;
                            break;
                        case BEFORE_EXP:
                            result.append("$").append(token);
                            state = INVALUE;
                            break;
                    }
                    break;
                default:
                    result.append(token);
            }
        }
        return result.toString();
    }

    private String evaluateExpression(String exp) {
        String[] parts = exp.split(":", 2);
        if(parts.length<2){
            return "--{MISSING RESOLVER ID in "+exp+"}";
        }
        SimpleResolver resolver = this.resolvers.get(parts[0]);
        if(resolver==null){
            return "--{NO RESOLVER FOUND for "+exp+"}";
        }
        try{
            String resolved = resolver.evaluate(parts[1]);
            if(resolved==null) {
                return "--{NOT RESOLVABLE:" + exp + "}";
            }else{
                return resolved;
            }
        }catch(Exception e){
            return "--{ERROR:"+exp+":"+e+"}";
        }
    }
}
