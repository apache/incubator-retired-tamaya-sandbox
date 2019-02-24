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
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ServiceContextManager;
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
final class JavaConfigBuilderAdapter implements ConfigBuilder {

    private ConfigurationBuilder configBuilder;

    /**
     * Create a new ConfigBuilder using the given Tamaya config builder.
     *
     * @param configBuilder
     */
    JavaConfigBuilderAdapter(ConfigurationBuilder configBuilder) {
        this.configBuilder = Objects.requireNonNull(configBuilder);
        configBuilder.addDefaultPropertyConverters();
    }

    /**
     * Access the underlying Tamaya {@link ConfigurationBuilder}.
     *
     * @return the Tamaya builder, not null.
     */
    public ConfigurationBuilder getConfigurationBuilder() {
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
                new JavaConfigDefaultPropertiesPropertySource());
        configBuilder.sortPropertySources(PropertySourceComparator.getInstance());
        return this;
    }

    /**
     * Add ConfigSources registered using the ServiceLoader.
     *
     * @return the ConfigBuilder with the added config sources
     */
    @Override
    public ConfigBuilder addDiscoveredSources() {
        for (ConfigSource configSource : ServiceContextManager.getServiceContext().getServices(ConfigSource.class)) {
            configBuilder.addPropertySources(JavaConfigAdapterFactory.toPropertySource(configSource));
        }
        for (ConfigSourceProvider configSourceProvider : ServiceContextManager.getServiceContext().getServices(ConfigSourceProvider.class)) {
            configBuilder.addPropertySources(JavaConfigAdapterFactory.toPropertySources(configSourceProvider.getConfigSources(
                    Thread.currentThread().getContextClassLoader()
            )));
        }
        configBuilder.sortPropertySources(PropertySourceComparator.getInstance());
        return this;
    }

    /**
     * Add Converters registered using the ServiceLoader.
     *
     * @return the ConfigBuilder with the added config converters
     */
    @Override
    public ConfigBuilder addDiscoveredConverters() {
        for (Converter<?> converter : ServiceContextManager.getServiceContext().getServices(Converter.class)) {
            TypeLiteral targetType = TypeLiteral.of(
                    TypeLiteral.getGenericInterfaceTypeParameters(converter.getClass(), Converter.class)[0]);
            configBuilder.addPropertyConverters(targetType,
                    JavaConfigAdapterFactory.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader classLoader) {
        this.configBuilder.setClassLoader(classLoader);
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        for (ConfigSource source : sources) {
            configBuilder.addPropertySources(JavaConfigAdapterFactory.toPropertySource(source));
        }
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> aClass, int priority, Converter<T> converter) {
        TypeLiteral lit = TypeLiteral.of(aClass);
        TypeLiteral target = TypeLiteral.of(aClass);
        configBuilder.removePropertyConverters(target);
        configBuilder.addPropertyConverters(
                target,
                JavaConfigAdapterFactory.toPropertyConverter(converter, priority));
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for (Converter<?> converter : converters) {
            TypeLiteral lit = TypeLiteral.of(converter.getClass());
            TypeLiteral target = TypeLiteral.of(lit.getType());
            configBuilder.removePropertyConverters(target);
            configBuilder.addPropertyConverters(
                    target,
                    JavaConfigAdapterFactory.toPropertyConverter(converter));
        }
        return this;
    }

    @Override
    public Config build() {
        return JavaConfigAdapterFactory.toConfig(configBuilder.build());
    }

    @Override
    public String toString() {
        return "JavaConfigBuilderAdapter{" +
                "configBuilder=" + configBuilder +
                '}';
    }
}
