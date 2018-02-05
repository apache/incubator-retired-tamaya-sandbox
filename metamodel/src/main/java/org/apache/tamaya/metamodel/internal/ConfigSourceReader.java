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

import org.apache.tamaya.metamodel.EnabledConfigSource;
import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.ext.EnabledConfigSourceProvider;
import org.apache.tamaya.metamodel.ext.FilteredConfigSource;
import org.apache.tamaya.metamodel.ext.RefreshableConfigSource;
import org.apache.tamaya.metamodel.ext.RefreshableConfigSourceProvider;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.base.filter.Filter;
import org.apache.tamaya.base.ServiceContext;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigSource;
import javax.config.spi.ConfigSourceProvider;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metaconfiguration reader to read property sources and property source providers.
 */
@Component
public class ConfigSourceReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(ConfigSourceReader.class.getName());

    @Override
    public void read(Document document, ConfigBuilder configBuilder) {
        NodeList nodeList = document.getDocumentElement().getElementsByTagName("sources");
        if(nodeList.getLength()==0){
            LOG.finer("No config sources configured.");
            return;
        }
        if(nodeList.getLength()>1){
            throw new IllegalArgumentException("Only one single sources section allowed.");
        }
        nodeList = nodeList.item(0).getChildNodes();
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeType()!=Node.ELEMENT_NODE) {
                continue;
            }
            String type = node.getNodeName();
            if("defaults".equals(type)){
                LOG.fine("Adding default config sources.");
                configBuilder.addDefaultSources();
                continue;
            }else if("discovered".equals(type)){
                LOG.fine("Adding default config sources.");
                configBuilder.addDiscoveredSources();
                continue;
            }
            try {
                ItemFactory<ConfigSource> sourceFactory = ItemFactoryManager.getInstance().getFactory(ConfigSource.class, type);
                if (sourceFactory != null) {
                    LOG.fine("Config source found: " + type);
                    Map<String, String> params = ComponentConfigurator.extractParameters(node);
                    ConfigSource ps = sourceFactory.create(params);
                    if (ps != null) {
                        ComponentConfigurator.configure(ps, params);
                        ps = decorateConfigSource(ps, node, params);
                        LOG.finer("Adding configured config source: " + ps.getName());
                        configBuilder.withSources(ps);
                        continue;
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure config source: " + type, e);
                continue;
            }
            try {
                ItemFactory<ConfigSourceProvider> providerFactory = ItemFactoryManager.getInstance().getFactory(ConfigSourceProvider.class, type);
                if(providerFactory==null){
                    LOG.fine("No such config source provider: " + type);
                    continue;
                }
                Map<String,String> params = ComponentConfigurator.extractParameters(node);
                ConfigSourceProvider prov = providerFactory.create(params);
                if(prov!=null) {
                    ComponentConfigurator.configure(prov, node);
                    prov = decorateConfigSourceProvider(prov, node, params);
                    LOG.finer("Adding configured config source provider: " + prov.getClass().getName());
                    for(ConfigSource cs:prov.getConfigSources(ServiceContext.defaultClassLoader())){
                        configBuilder.withSources(cs);
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure ConfigSourceProvider: " + type, e);
            }
        }
    }

    /**
     * Decorates a property source to be refreshable or filtered.
     * @param ps the wrapped property source
     *@param configNode the XML config node
     * @param params the extracted parameter list   @return the property source to be added to the context.
     */
    private ConfigSource decorateConfigSource(ConfigSource ps, Node configNode, Map<String, String> params){
        Node refreshableVal = configNode.getAttributes().getNamedItem("refreshable");
        if(refreshableVal!=null && Boolean.parseBoolean(refreshableVal.getNodeValue())){
            ps = RefreshableConfigSource.of(params, ps);
        }
        Node enabledVal = configNode.getAttributes().getNamedItem("enabled");
        if(enabledVal!=null){
            ps = new EnabledConfigSource(ps,
                    MetaContext.getInstance().getProperties(),
                    enabledVal.getNodeValue());
        }
        NodeList childNodes = configNode.getChildNodes();
        for(int i=0;i<childNodes.getLength();i++){
            Node node = childNodes.item(i);
            if("filters".equals(node.getNodeName())){
                ps = FilteredConfigSource.of(ps);
                NodeList filterNodes = node.getChildNodes();
                for(int f=0;f<filterNodes.getLength();f++) {
                    Node filterNode = filterNodes.item(f);
                    configureFilter((FilteredConfigSource) ps, filterNode);
                }
            }
        }
        return ps;
    }

    private void configureFilter(FilteredConfigSource ps, Node filterNode) {
        try {
            String type = filterNode.getNodeName();
            ItemFactory<Filter> filterFactory = ItemFactoryManager.getInstance().getFactory(Filter.class, type);
            if(filterFactory==null){
                LOG.severe("No such filter: " + type);
                return;
            }
            Map<String,String> params = ComponentConfigurator.extractParameters(filterNode);
            Filter filter = filterFactory.create(params);
            if(filter!=null) {
                ComponentConfigurator.configure(filter, params);
                LOG.finer("Adding configured filter: " + filter.getClass().getName());
                ps.addFilter(filter);
            }
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Failed to read filter configuration: " + filterNode, e);
        }
    }

    /**
     * Decorates a property source provider to be refreshable or filtered.
     * @param prov the property source provider to be wrapped.
     * @param configNode the XML config node
     * @param params the extracted parameter list   @return the property source provider to be added to the context.
     */
    private ConfigSourceProvider decorateConfigSourceProvider(ConfigSourceProvider prov, Node configNode, Map<String, String> params){
        Node refreshableVal = configNode.getAttributes().getNamedItem("refreshable");
        // Refreshable
        if(refreshableVal!=null && Boolean.parseBoolean(refreshableVal.getNodeValue())){
            prov = RefreshableConfigSourceProvider.of(params, prov);
        }
        // Enabled
        Node enabledVal = configNode.getAttributes().getNamedItem("enabled");
        if(enabledVal!=null){
            prov = new EnabledConfigSourceProvider(prov,
                    MetaContext.getInstance().getProperties(),
                    enabledVal.getNodeValue());
        }
        return prov;
    }

}
