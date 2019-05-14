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

import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.metamodel.spi.ItemFactory;
import org.apache.tamaya.metamodel.spi.ItemFactoryManager;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ListValue;
import org.apache.tamaya.spi.ObjectValue;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertyValue;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metaconfiguration reader to read property sources and property source providers.
 */
@Component
public class PropertyConverterReader implements MetaConfigurationReader{

    private static final Logger LOG = Logger.getLogger(PropertyConverterReader.class.getName());

    @Override
    public void read(ConfigurationData metaConfig, ConfigurationBuilder configBuilder) {
        ObjectValue root = ObjectValue.from(metaConfig.getData());
        if(root.getPropertyValue("converters")==null){
            LOG.finer("No property converters configured.");
            return;
        }
        ListValue nodeList = root.getPropertyValue("converters").toListValue();
        ListValue listValue = nodeList.toListValue();
        for(PropertyValue converterNode:listValue){

            if(converterNode.getValueType()!= PropertyValue.ValueType.MAP) {
                continue;
            }
            ObjectValue ov = converterNode.toObjectValue();
            ObjectValue propertyValue = null;
            if(ov.getPropertyValue("properties")!=null){
                propertyValue = ov.getPropertyValue("properties").toObjectValue();
            }
            Map<String,String> properties = propertyValue!=null? propertyValue.toLocalMap(): null;
            String type = ItemFactoryManager.getType(ov);
            if("defaults".equals(type)){
                LOG.finer("Adding default property converters...");
                configBuilder.addDefaultPropertyConverters();
                continue;
            }
            try {
                ItemFactory<PropertyConverter> converterFactory = ItemFactoryManager.getInstance().getFactory(PropertyConverter.class, type);
                if(converterFactory==null){
                    LOG.severe("No such property converter: " + type);
                    continue;
                }
                PropertyConverter converter = converterFactory.create(ov.toMap());
                if(converter!=null) {
                    ComponentConfigurator.configure(converter, properties);
                    Class targetType = Class.forName(ov.getValue("targetType"));
                    LOG.finer("Adding converter for type " + targetType.getName() + ": " + converter.getClass());
                    configBuilder.addPropertyConverters(TypeLiteral.of(targetType), converter);
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to configure PropertyConverter: " + type, e);
            }
        }
    }

}
