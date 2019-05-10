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
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertyValue;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Metaconfiguration reader that reads the configuration filters to be used.
 */
@Component
public class PropertyFilterReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(PropertyFilterReader.class.getName());

    @Override
    public void read(ConfigurationData metaConfig, ConfigurationBuilder configBuilder) {
        ObjectValue root = ObjectValue.from(metaConfig.getData());
        if (root.getPropertyValue("filters") == null) {
            LOG.finer("No property filters configured");
            return;
        }
        ListValue nodeList = root.getPropertyValue("filters").toListValue();
        ListValue listValue = nodeList.toListValue();
        for (PropertyValue filterNode : listValue) {

            if (filterNode.getValueType() != PropertyValue.ValueType.MAP) {
                continue;
            }
            ObjectValue ov = filterNode.toObjectValue();
            String type = ItemFactoryManager.getType(ov);
            if ("defaults".equals(type)) {
                LOG.finer("Adding default property filters...");
                configBuilder.addDefaultPropertyFilters();
                continue;
            }
            ObjectValue propertyValue = ov.getPropertyValue("properties").toObjectValue();
            Map<String,String> properties = propertyValue!=null? propertyValue.toLocalMap(): null;
            try {
                ItemFactory<PropertyFilter> filterFactory = ItemFactoryManager.getInstance().getFactory(PropertyFilter.class, type);
                if (filterFactory == null) {
                    LOG.severe("No such property filter: " + type);
                    continue;
                }
                PropertyFilter filter = filterFactory.create(properties);
                if (filter != null) {
                    ComponentConfigurator.configure(filter, properties);
                    LOG.finer("Adding filter: " + filter.getClass());
                    configBuilder.addPropertyFilters(filter);
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure PropertyFilter: " + type, e);
            }
        }
    }


}
