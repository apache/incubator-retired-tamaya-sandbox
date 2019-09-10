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
package org.apache.tamaya.metamodel.spi;

import org.apache.tamaya.metamodel.internal.factories.SysPropertiesFactory;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class ItemFactoryManagerTest {

    @Test
    public void getInstance() {
        assertThat(ItemFactoryManager.getInstance()).isNotNull();
    }

    @Test
    public void getType() {
        assertThat(ItemFactoryManager.getType(PropertyValue.createObject())).isNull();
        assertThat(ItemFactoryManager.getType(PropertyValue.createObject().setValue("type", "test")))
                .isNotNull().isEqualTo("test");
        assertThat(ItemFactoryManager.getType(PropertyValue.createObject().setValue("class", "test")))
                .isNotNull().isEqualTo("test");
    }

    @Test
    public void getFactories() {
        assertThat(ItemFactoryManager.getInstance().getFactories(PropertySource.class))
                .isNotEmpty()
                .hasSize(9);
    }

    @Test
    public void getFactory() {
        ItemFactory ifact = new SysPropertiesFactory();
        assertThat(ItemFactoryManager.getInstance().getFactory(PropertySource.class, ifact.getName()))
                .isNotNull();
    }

    @Test
    public void registerItemFactory() {
        ItemFactory ifact = new ItemFactory(){
            @Override
            public String getName() {
                return "foo";
            }

            @Override
            public Class getType() {
                return String.class;
            }

            @Override
            public Object create(Map parameters) {
                return Collections.emptyMap();
            }
        };
        ItemFactoryManager.getInstance().registerItemFactory(ifact);
        assertThat(ItemFactoryManager.getInstance().getFactory(String.class, ifact.getName()))
                .isNotNull()
                .isEqualTo(ifact);
    }
}