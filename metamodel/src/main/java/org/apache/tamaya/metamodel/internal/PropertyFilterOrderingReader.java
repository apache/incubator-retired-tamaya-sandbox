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
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyValue;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Priority;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Metaconfiguration reader that reads the configuration combination policy to be used.
 */
@Component
@Priority(Integer.MAX_VALUE)
public class PropertyFilterOrderingReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(PropertyFilterOrderingReader.class.getName());

    @Override
    public void read(ConfigurationData metaConfig, ConfigurationBuilder configBuilder) {
        ObjectValue root = ObjectValue.from(metaConfig.getData());
        if(root.getPropertyValue("filter-order")==null){
            LOG.finer("No property filter ordering defined.");
            return;
        }
        ObjectValue value = root.getPropertyValue("filter-order").toObjectValue();
        if(value.getValueType()== PropertyValue.ValueType.MAP){
            ObjectValue ov = value.toObjectValue();
            PropertyValue type = ov.getPropertyValue("type");
            if(type==null){
                type = ov.getPropertyValue("class");
                ItemFactory<Comparator> comparatorFactory = ItemFactoryManager.getInstance()
                        .getFactory(Comparator.class, type.getValue());
                Map<String,String> properties = ov.toLocalMap();
                Comparator comparator = comparatorFactory.create(properties);
                ComponentConfigurator.configure(comparator, properties);
                LOG.finer("Sorting property filters using comparator: " + comparator.getClass().getName());
                configBuilder.sortPropertyFilter(comparator);
            }
        }
    }

}
