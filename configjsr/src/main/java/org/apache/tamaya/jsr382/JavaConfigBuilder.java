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
package org.apache.tamaya.jsr382;

import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.*;
import org.apache.tamaya.spisupport.PropertySourceComparator;
import org.apache.tamaya.spisupport.propertysource.EnvironmentPropertySource;
import org.apache.tamaya.spisupport.propertysource.SystemPropertySource;

import javax.config.Config;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigSource;
import javax.config.spi.ConfigSourceProvider;
import javax.config.spi.Converter;
import java.util.Objects;

/**
 * Created by atsticks on 23.03.17.
 */
final class JavaConfigBuilder implements ConfigBuilder{

    private ConfigurationBuilder configBuilder;
    private ClassLoader classLoader;

    JavaConfigBuilder(ConfigurationBuilder contextBuilder){
        this.configBuilder = Objects.requireNonNull(contextBuilder);
        contextBuilder.addDefaultPropertyConverters();
    }

    public ConfigurationBuilder getConfigurationBuilder(){
        return configBuilder;
    }

    /**
     * Add the default config sources appearing on the builder's classpath
     * including:
     * <ol>
     * <li>System properties</li>
     * <li>Environment properties</li>
     * <li>/META-INF/JavaConfig-config.properties</li>
     * </ol>
     *
     * @return the ConfigBuilder with the default config sources
     */
    @Override
    public ConfigBuilder addDefaultSources() {
        configBuilder.addPropertySources(
                new SystemPropertySource(400),
                new EnvironmentPropertySource(300),
                new JavaConfigDefaultProperties());
        configBuilder.sortPropertySources(PropertySourceComparator.getInstance());
        return this;
    }

    /**
     * Add ConfigSources registered using the ServiceLoader.
     * @return the ConfigBuilder with the added config sources
     */
    @Override
    public ConfigBuilder addDiscoveredSources() {
        for(ConfigSource configSource: ServiceContextManager.getServiceContext().getServices(ConfigSource.class)){
            configBuilder.addPropertySources(JavaConfigAdapter.toPropertySource(configSource));
        }
        for(ConfigSourceProvider configSourceProvider: ServiceContextManager.getServiceContext().getServices(ConfigSourceProvider.class)){
            configBuilder.addPropertySources(JavaConfigAdapter.toPropertySources(configSourceProvider.getConfigSources(
                    Thread.currentThread().getContextClassLoader()
            )));
        }
        configBuilder.sortPropertySources(PropertySourceComparator.getInstance());
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
            configBuilder.addPropertyConverters(targetType,
                    JavaConfigAdapter.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        this.classLoader = loader;
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        for(ConfigSource source:sources){
            configBuilder.addPropertySources(JavaConfigAdapter.toPropertySource(source));
        }
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for(Converter<?> converter:converters){
            TypeLiteral lit = TypeLiteral.of(converter.getClass());
            TypeLiteral target = TypeLiteral.of(lit.getType());
            configBuilder.removePropertyConverters(target);
            configBuilder.addPropertyConverters(
                    target,
                    JavaConfigAdapter.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public Config build() {
        return JavaConfigAdapter.toConfig(configBuilder.build());
    }
}
