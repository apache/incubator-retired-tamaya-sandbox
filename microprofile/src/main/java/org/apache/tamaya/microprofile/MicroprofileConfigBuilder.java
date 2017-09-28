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
package org.apache.tamaya.microprofile;

import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.spisupport.EnvironmentPropertySource;
import org.apache.tamaya.spisupport.PropertySourceComparator;
import org.apache.tamaya.spisupport.SystemPropertySource;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.Objects;

/**
 * Created by atsticks on 23.03.17.
 */
final class MicroprofileConfigBuilder implements ConfigBuilder{

    private ConfigurationContextBuilder contextBuilder;

    MicroprofileConfigBuilder(ConfigurationContextBuilder contextBuilder){
        this.contextBuilder = Objects.requireNonNull(contextBuilder);
        contextBuilder.addDefaultPropertyConverters();
    }

    public ConfigurationContextBuilder getConfigurationContextBuilder(){
        return contextBuilder;
    }

    /**
     * Add the default config sources appearing on the builder's classpath
     * including:
     * <ol>
     * <li>System properties</li>
     * <li>Environment properties</li>
     * <li>/META-INF/microprofile-config.properties</li>
     * </ol>
     *
     * @return the ConfigBuilder with the default config sources
     */
    @Override
    public ConfigBuilder addDefaultSources() {
        contextBuilder.addPropertySources(
                new SystemPropertySource(400),
                new EnvironmentPropertySource(300),
                new MicroprofileDefaultProperties());
        contextBuilder.sortPropertySources(PropertySourceComparator.getInstance()
                .setOrdinalKey("config_ordinal"));
        return this;
    }

    /**
     * Add ConfigSources registered using the ServiceLoader.
     * @return the ConfigBuilder with the added config sources
     */
    @Override
    public ConfigBuilder addDiscoveredSources() {
        for(ConfigSource configSource: ServiceContextManager.getServiceContext().getServices(ConfigSource.class)){
            contextBuilder.addPropertySources(MicroprofileAdapter.toPropertySource(configSource));
        }
        for(ConfigSourceProvider configSourceProvider: ServiceContextManager.getServiceContext().getServices(ConfigSourceProvider.class)){
            contextBuilder.addPropertySources(MicroprofileAdapter.toPropertySources(configSourceProvider.getConfigSources(
                    Thread.currentThread().getContextClassLoader()
            )));
        }
        contextBuilder.sortPropertySources(PropertySourceComparator.getInstance());
        return this;
    }

    /**
     * Add Converters registered using the ServiceLoader.
     * @return the ConfigBuilder with the added config converters
     */
    @Override
    public ConfigBuilder addDiscoveredConverters() {
        for(Converter<?> converter: ServiceContextManager.getServiceContext().getServices(Converter.class)){
            TypeLiteral targetType = TypeLiteral.of(
                    TypeLiteral.getGenericInterfaceTypeParameters(converter.getClass(),Converter.class)[0]);
            contextBuilder.addPropertyConverters(targetType,
                    MicroprofileAdapter.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        return null;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        for(ConfigSource source:sources){
            contextBuilder.addPropertySources(MicroprofileAdapter.toPropertySource(source));
        }
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for(Converter<?> converter:converters){
            TypeLiteral lit = TypeLiteral.of(converter.getClass());
            TypeLiteral target = TypeLiteral.of(lit.getType());
            contextBuilder.removePropertyConverters(target);
            contextBuilder.addPropertyConverters(
                    target,
                    MicroprofileAdapter.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public Config build() {
        return MicroprofileAdapter.toConfig(ConfigurationProvider.createConfiguration(
                contextBuilder.build()
        ));
    }
}
