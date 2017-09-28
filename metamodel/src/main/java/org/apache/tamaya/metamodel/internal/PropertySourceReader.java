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
import org.apache.tamaya.metamodel.EnabledPropertySource;
import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.ext.EnabledPropertySourceProvider;
import org.apache.tamaya.metamodel.ext.FilteredPropertySource;
import org.apache.tamaya.metamodel.ext.RefreshablePropertySource;
import org.apache.tamaya.metamodel.ext.RefreshablePropertySourceProvider;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metaconfiguration reader to read property sources and property source providers.
 */
@Component
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
            throw new ConfigException("Only one single property-source section allowed.");
        }
        nodeList = nodeList.item(0).getChildNodes();
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeType()!=Node.ELEMENT_NODE) {
                continue;
            }
            String type = node.getNodeName();
            if("defaults".equals(type)){
                LOG.fine("Adding default property sources.");
                contextBuilder.addDefaultPropertySources();
                continue;
            }
            try {
                ItemFactory<PropertySource> sourceFactory = ItemFactoryManager.getInstance().getFactory(PropertySource.class, type);
                if (sourceFactory != null) {
                    LOG.fine("Property source found: " + type);
                    Map<String, String> params = ComponentConfigurator.extractParameters(node);
                    PropertySource ps = sourceFactory.create(params);
                    if (ps != null) {
                        ComponentConfigurator.configure(ps, params);
                        ps = decoratePropertySource(ps, node, params);
                        LOG.finer("Adding configured property source: " + ps.getName());
                        contextBuilder.addPropertySources(ps);
                        continue;
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure PropertySource: " + type, e);
                continue;
            }
            try {
                ItemFactory<PropertySourceProvider> providerFactory = ItemFactoryManager.getInstance().getFactory(PropertySourceProvider.class, type);
                if(providerFactory==null){
                    LOG.fine("No such property source provider: " + type);
                    continue;
                }
                Map<String,String> params = ComponentConfigurator.extractParameters(node);
                PropertySourceProvider prov = providerFactory.create(params);
                if(prov!=null) {
                    ComponentConfigurator.configure(prov, node);
                    prov = decoratePropertySourceProvider(prov, node, params);
                    LOG.finer("Adding configured property source provider: " + prov.getClass().getName());
                    contextBuilder.addPropertySources(prov.getPropertySources());
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure PropertySourceProvider: " + type, e);
            }
        }
    }

    /**
     * Decorates a property source to be refreshable or filtered.
     * @param ps the wrapped property source
     *@param configNode the XML config node
     * @param params the extracted parameter list   @return the property source to be added to the context.
     */
    private PropertySource decoratePropertySource(PropertySource ps, Node configNode, Map<String, String> params){
        Node refreshableVal = configNode.getAttributes().getNamedItem("refreshable");
        if(refreshableVal!=null && Boolean.parseBoolean(refreshableVal.getNodeValue())){
            ps = RefreshablePropertySource.of(params, ps);
        }
        Node enabledVal = configNode.getAttributes().getNamedItem("enabled");
        if(enabledVal!=null){
            ps = new EnabledPropertySource(ps,
                    MetaContext.getInstance().getProperties(),
                    enabledVal.getNodeValue());
        }
        NodeList childNodes = configNode.getChildNodes();
        for(int i=0;i<childNodes.getLength();i++){
            Node node = childNodes.item(i);
            if("filters".equals(node.getNodeName())){
                ps = FilteredPropertySource.of(ps);
                NodeList filterNodes = node.getChildNodes();
                for(int f=0;f<filterNodes.getLength();f++) {
                    Node filterNode = filterNodes.item(f);
                    configureFilter((FilteredPropertySource) ps, filterNode);
                }
            }
        }
        return ps;
    }

    private void configureFilter(FilteredPropertySource ps, Node filterNode) {
        try {
            String type = filterNode.getNodeName();
            ItemFactory<PropertyFilter> filterFactory = ItemFactoryManager.getInstance().getFactory(PropertyFilter.class, type);
            if(filterFactory==null){
                LOG.severe("No such property filter: " + type);
                return;
            }
            Map<String,String> params = ComponentConfigurator.extractParameters(filterNode);
            PropertyFilter filter = filterFactory.create(params);
            if(filter!=null) {
                ComponentConfigurator.configure(filter, params);
                LOG.finer("Adding configured property filter: " + filter.getClass().getName());
                ps.addPropertyFilter(filter);
            }
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Failed to read property filter configuration: " + filterNode, e);
        }
    }

    /**
     * Decorates a property source provider to be refreshable or filtered.
     * @param prov the property source provider to be wrapped.
     * @param configNode the XML config node
     * @param params the extracted parameter list   @return the property source provider to be added to the context.
     */
    private PropertySourceProvider decoratePropertySourceProvider(PropertySourceProvider prov, Node configNode, Map<String, String> params){
        Node refreshableVal = configNode.getAttributes().getNamedItem("refreshable");
        // Refreshable
        if(refreshableVal!=null && Boolean.parseBoolean(refreshableVal.getNodeValue())){
            prov = RefreshablePropertySourceProvider.of(params, prov);
        }
        // Enabled
        Node enabledVal = configNode.getAttributes().getNamedItem("enabled");
        if(enabledVal!=null){
            prov = new EnabledPropertySourceProvider(prov,
                    MetaContext.getInstance().getProperties(),
                    enabledVal.getNodeValue());
        }
        return prov;
    }

}
