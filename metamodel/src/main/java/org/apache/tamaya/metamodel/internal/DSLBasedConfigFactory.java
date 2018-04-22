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

import org.apache.tamaya.base.DefaultConfigBuilder;
import org.apache.tamaya.base.configsource.ConfigSourceComparator;
import org.apache.tamaya.base.filter.FilterComparator;
import org.apache.tamaya.core.TamayaConfigProviderResolver;
import org.apache.tamaya.metamodel.MetaConfig;
import org.osgi.service.component.annotations.Component;

import javax.annotation.Priority;
import javax.config.Config;


/**
 * ConfigFactory that uses {@link MetaConfig} to create new Config instances for
 * the Tamaya Configuration System. This class is not usable when an alternate
 * Configuration implementation is active. Instead use the methods in {@link MetaConfig}
 * to configure/setup your configuration system.
 */
@Priority(1)
@Component
public class DSLBasedConfigFactory implements TamayaConfigProviderResolver.ConfigFactory{

    @Override
    public Config createConfig(ClassLoader classLoader) {
        MetaConfig.configure(classLoader);
        return new DefaultConfigBuilder()
                        .addDiscoveredConverters()
                        .addDiscoveredFilters()
                        .addDefaultSources()
                        .addDiscoveredSources()
                        .sortFilter(FilterComparator.getInstance())
                        .sortSources(ConfigSourceComparator.getInstance())
                        .build();
    }
}
