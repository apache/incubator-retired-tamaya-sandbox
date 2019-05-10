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

import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.metamodel.EnabledPropertySource;
import org.apache.tamaya.metamodel.ext.EnabledPropertySourceProvider;
import org.apache.tamaya.metamodel.ext.FilteredPropertySource;
import org.apache.tamaya.metamodel.ext.RefreshablePropertySource;
import org.apache.tamaya.metamodel.ext.RefreshablePropertySourceProvider;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.PropertyValue;
import org.osgi.service.component.annotations.Component;

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
    public void read(ConfigurationData metaConfig, ConfigurationBuilder configBuilder) {
        ObjectValue root = ObjectValue.from(metaConfig.getData());
        PropertyValue nodeList = root.getPropertyValue("sources");
        if (nodeList == null) {
            LOG.finer("No property sources configured");
            return;
        }
        ListValue listValue = nodeList.toListValue();
        for (PropertyValue node : listValue) {
            if(node.getValueType()!= PropertyValue.ValueType.MAP) {
                continue;
            }
            ObjectValue ov = node.toObjectValue();
            ObjectValue propertyValue = ov.getPropertyValue("properties").toObjectValue();
            Map<String,String> params = propertyValue!=null? propertyValue.toLocalMap(): null;
            String type = ItemFactoryManager.getType(ov);
            if ("defaults".equals(type)) {
                LOG.finer("Adding default property sources...");
                configBuilder.addDefaultPropertySources();
                continue;
            }
            boolean isProvider = ov.getValue("provider")!=null?Boolean.valueOf(ov.getValue("provider")):false;
            if(isProvider) {
                try {
                    ItemFactory<PropertySourceProvider> providerFactory = ItemFactoryManager.getInstance().getFactory(PropertySourceProvider.class, type);
                    if(providerFactory==null){
                        LOG.fine("No such property source provider: " + type);
                        continue;
                    }
                    PropertySourceProvider prov = providerFactory.create(params);
                    if(prov!=null) {
                        ComponentConfigurator.configure(prov, params);
                        prov = decoratePropertySourceProvider(prov, ov.toLocalMap());
                        LOG.finer("Adding configured property source provider: " + prov.getClass().getName());
                        configBuilder.addPropertySources(prov.getPropertySources());
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to configure PropertySourceProvider: " + type, e);
                }
            }else{
                try {
                    ItemFactory<PropertySource> sourceFactory = ItemFactoryManager.getInstance().getFactory(PropertySource.class, type);
                    if (sourceFactory == null) {
                        LOG.severe("No such property source: " + type);
                        continue;
                    }
                    PropertySource ps = sourceFactory.create(params);
                    if (ps != null) {
                        ComponentConfigurator.configure(ps, params);
                        ps = decoratePropertySource(ps, ov);
                        LOG.finer("Adding configured property source: " + ps.getName());
                        configBuilder.addPropertySources(ps);
                        continue;
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to configure PropertySource: " + type, e);
                    continue;
                }
            }
        }
    }

    /**
     * Decorates a property source to be refreshable or filtered.
     * @param ps the wrapped property source
     * @param configNode the config value
     * @return the property source to be added to the context.
     */
    private PropertySource decoratePropertySource(PropertySource ps, ObjectValue configNode){
        Map<String,String> params = configNode.toMap();
        boolean refreshable = Boolean.parseBoolean(params.get("refreshable"));
        if(refreshable){
            ps = RefreshablePropertySource.of(ps);
        }
        String enabledVal = params.get("enabled");
        if(enabledVal!=null){
            ps = new EnabledPropertySource(ps, enabledVal);
        }
        PropertyValue childNodes = configNode.getPropertyValue("filters");
        if(childNodes!=null) {
            ListValue listValue = childNodes.toListValue();
            ps = FilteredPropertySource.of(ps);
            for (PropertyValue filterNode:listValue) {
                ObjectValue ov = filterNode.toObjectValue();
                configureFilter((FilteredPropertySource) ps, ov);
            }
        }
        return ps;
    }

    private void configureFilter(FilteredPropertySource ps, ObjectValue filterNode) {
        try {
            String type = ItemFactoryManager.getType(filterNode);
            if(type==null){
                return;
            }
            ItemFactory<PropertyFilter> filterFactory = ItemFactoryManager.getInstance()
                    .getFactory(PropertyFilter.class, type);
            if(filterFactory==null){
                LOG.severe("No such property filter: " + type);
                return;
            }
            Map<String,String> properties = filterNode.toMap();
            PropertyFilter filter = filterFactory.create(properties);
            if(filter!=null) {
                ComponentConfigurator.configure(filter, properties);
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
     * @param properties the config properties
     */
    private PropertySourceProvider decoratePropertySourceProvider(PropertySourceProvider prov, Map<String, String> properties){
        boolean refreshable = Boolean.parseBoolean(properties.get("refreshable"));
        // Refreshable
        if(refreshable){
            prov = RefreshablePropertySourceProvider.of(prov);
        }
        // Enabled
        String enabled = properties.get("enabled");
        if(enabled!=null){
            prov = new EnabledPropertySourceProvider(prov,
                    enabled);
        }
        return prov;
    }

}
