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
package org.apache.tamaya.metamodel;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;
import org.apache.tamaya.spi.PropertyValueCombinationPolicy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Builder to create or change a {@link Configuration}. This build allows to programmatically
 * assemble any kind of configuration based on {@link PropertySource}s. It also provides
 * methods to change the overall ordering and add the source processors that are configured as
 * ServiceLoader services, either directly, or indirectly using the {@link org.apache.tamaya.spi.PropertySourceProvider}
 * SPI.
 *
 * Additionally this builder also implements the SPI that provides the default configuration
 * as returned by {@link org.apache.tamaya.ConfigurationProvider#getConfiguration()}, since by definition the
 * default configuration equals to the configuration that is created by calling
 * <pre>
 *     ConfigurationContextBuilderFactory factory = ...;
 *     Configuration config = factory.createBuilder().loadDefaults().build();
 * </pre>
 */
public interface ConfigurationContextBuilder {

    /**
     * Adds one or more property filter instances to the configuration to be build.
     *
     * <pre>{@code PropertyFilter quoteReplacingFilter = new QuoteFilter();
     * PropertyFilter commaRemovingFilter = new CommaFilter();
     *
     * builder.addPropertyFilters(commaRemovingFilter, quoteReplacingFilter)};
     * </pre>
     *
     * @param filters list of property filter instances which should be applied
     *                to the properties of the configuration to be build.
     *
     * @return the builder instance currently used
     *
     * @see org.apache.tamaya.spi.PropertyFilter
     * @see #getDefaultPropertyFilters()
     */
    ConfigurationContextBuilder addPropertyFilters(PropertyFilter... filters);

    /**
     * Adds one or more property filter instances to the configuration to be build.
     *
     * <pre>{@code PropertyFilter quoteReplacingFilter = new QuoteFilter();
     * PropertyFilter commaRemovingFilter = new CommaFilter();
     *
     * builder.addPropertyFilters(commaRemovingFilter, quoteReplacingFilter)};
     * </pre>
     *
     * @param filters list of property filter instances which should be applied
     *                to the properties of the configuration to be build.
     *
     * @return the builder instance currently used
     *
     * @see org.apache.tamaya.spi.PropertyFilter
     * @see #getDefaultPropertyFilters()
     */
    ConfigurationContextBuilder addPropertyFilters(Collection<PropertyFilter> filters);

    /**
     * @param propertyValueCombinationPolicy combination policy to use for this builder.
     * @return the builder instance currently in use.
     */
    ConfigurationContextBuilder setPropertyValueCombinationPolicy(
            PropertyValueCombinationPolicy propertyValueCombinationPolicy);

    /**
     * Adds a property converter for the a given type to the configuration to
     * be build.
     *
     * <pre>{@code PropertyConverter<MyType> converter = value -> new MyType(value, 42);
     *
     * builder.addPropertyConverters(MyType.class, converter}
     * </pre>
     *
     * @param <T> the type of the configuration
     * @param type the required target type the converter should be applied to
     * @param converter the converter to be used to convert the string property
     *                  to the given target type, not null.
     *
     * @return the builder instance currently used
     *
     * @see org.apache.tamaya.spi.PropertyConverter
     * @see #getDefaultPropertyConverters()
     */
    <T> ConfigurationContextBuilder addPropertyConverter(Class<T> type, PropertyConverter<T>... converter);

    /**
     * Adds a property converter for the a given type to the configuration to
     * be build.
     *
     * <pre>{@code PropertyConverter<MyType> converter = value -> new MyType(value, 42);
     *
     * builder.addPropertyConverters(MyType.class, converter}
     * </pre>
     *
     * @param <T> the type of the configuration
     * @param type the required target type the converter should be applied to
     * @param converter the converter to be used to convert the string property
     *                  to the given target type, not null.
     *
     * @return the builder instance currently used
     *
     * @see org.apache.tamaya.spi.PropertyConverter
     * @see #getDefaultPropertyConverters()
     */
    <T> ConfigurationContextBuilder addPropertyConverter(Class<T> type, Collection<PropertyConverter<T>> converter);

    /**
     * Adds a propertyConverter of a given type.
     *
     * @param <T> the type of the configuration
     * @param type type literal of this converter.
     * @param propertyConverter property converter, not null.
     * @return the builder instance currently used
     */
    <T> ConfigurationContextBuilder addPropertyConverter(TypeLiteral<T> type, PropertyConverter<T>... propertyConverter);

    /**
     * Adds a propertyConverter of a given type.
     *
     * @param <T> the type of the configuration
     * @param type type literal of this converter.
     * @param propertyConverter property converter, not null.
     * @return the builder instance currently used
     */
    <T> ConfigurationContextBuilder addPropertyConverter(TypeLiteral<T> type, Collection<PropertyConverter<T>> propertyConverter);

    /**
     * Loads the {@link org.apache.tamaya.spi.PropertyConverter}s provided
     * via the SPI API returns them for adding all or parts of it using the
     * builder's add methods.
     *
     * @return the collection of default instances, not yet registered
     * within this builder.
     *
     * @see #addPropertyConverter(TypeLiteral, PropertyConverter...)
     * @see #addPropertyConverter(TypeLiteral, Collection)
     * @see org.apache.tamaya.spi.PropertyConverter
     */
    Map<TypeLiteral<?>, Collection<PropertyConverter<?>>> getDefaultPropertyConverters();


    /**
     * Loads the {@link org.apache.tamaya.spi.PropertySource}s provided
     * via the SPI API, but does not yet register them into this builder.
     *
     * @return the builder instance currently used
     *
     * @see #addPropertySources(Collection)
     * @see #addPropertySources(PropertySource...)
     * @see org.apache.tamaya.spi.PropertySource
     */
    Collection<PropertySource> getDefaultPropertySources();


    /**
     * Loads the {@link org.apache.tamaya.spi.PropertyFilter}s provided via the SPI API,
     * but does not register them into this instance.
     *
     * @return the builder instance currently used
     *
     * @see #addPropertyFilters(Collection<PropertyFilter>)
     * @see org.apache.tamaya.spi.PropertyFilter
     */
    Collection<PropertyFilter> getDefaultPropertyFilters();


    /**
     * Loads the {@link org.apache.tamaya.spi.PropertySourceProvider
     * property source providers} provided via the SPI API and
     * returns them. No property sources are registered.
     *
     * @return the builder instance currently used
     *
     * @see #addPropertySources(Collection)
     * @see org.apache.tamaya.spi.PropertySourceProvider
     */
    Collection<PropertySourceProvider> getDefaultPropertySourceProviders();

    /**
     * This method loads the default configuration, which involves the following
     * steps:
     * <ol>
     *     <li>Loading {@link PropertySource} instances registered with the
     *     {@link java.util.ServiceLoader}.</li>
     *     <li>Loading {@link org.apache.tamaya.spi.PropertySourceProvider} instances registered with the
     *     {@link java.util.ServiceLoader}, extracting the provided processors and add them
     *     to the list of loaded processors</li>
     *     <li>Sorting the processors collecting based on {@link PropertySource#getOrdinal()} and the
     *     fully qualified class name as an additional fallback criteria, when the ordinal
     *     is the same.</li>
     *     <li>Putting together a {@link Configuration} instance incorporating this
     *     chain of processors. Hereby when {@link Configuration#get(String)} is called
     *     the first processor in the chain is asked for a value. If the processor returns {@code null}
     *     the processChain is repeated until the end of the chain is reached, or a processor returns
     *     non non-null value. Hereby each processor can actively decide how and if its parent processors
     *     will be included for subsequent value evaluation for a given key. In other words a processor
     *     can either simply provide configuration properties (values) or implement a more active functionality
     *     such as filtering or value collection.</li>
     * </ol>
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder loadDefaults();

    /**
     * Get the current number of processors on the chain.
     * @return the number of registered processors of this instance.
     */
    int getChainSize();

    /**
     * Increses the priority of the given processor instance, meaning moving the processor one
     * position ahead in the overall processor chain. This will be done regardless of any
     * ordinal value. If the procerssor already has maximal significance (is on the head of
     * processors), this method does not have any effect.
     * @param processor the target procerssor, not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder increasePriority(PropertySource processor);

    /**
     * Decreses the priority of the given processor instance, meaning moving the processor one
     * position back in the overall processor chain. This will be done regardless of any
     * ordinal value. If the procerssor already has minimal significance (is the tail of
     * processors), this method does not have any effect.
     * @param processor the target procerssor, not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder decreasePriority(PropertySource processor);

    /**
     * Maximizes the priority of the given processor instance, moving it to the head of the
     * processor chain. This will be done regardless of any
     * ordinal value. If the procerssor already has minimal significance (is the tail of
     * processors), this method does not have any effect.
     * @param processor the target procerssor, not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder highestPriority(PropertySource processor);

    /**
     * Minimizes the priority of the given property source instance, moving it to the tail of the
     * property source chain. This will be done regardless of any
     * ordinal value. If the property source already has minimal significance (is the tail of
     * processors), this method does not have any effect.
     * @param propertySource the target property source, not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder lowestPriority(PropertySource propertySource);

    /**
     * Add the property sources to the property source chain using thei provided ordinals
     * for ordering.
     * @param propertySources the property sources to add, not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder addPropertySources(PropertySource... propertySources);

    /**
     * Add the property sources to the property source chain.
     * @param propertySources the property sources to add, not null.
     * @param index index where the new property sources should be inserted.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder addPropertySources(int index, PropertySource... propertySources);

    /**
     * Add the property sources to the property source chain using thei provided ordinals
     * for ordering.
     * @param propertySources the property sources to add, not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder addPropertySources(Collection<PropertySource> propertySources);

    /**
     * Add the property sources to the property source chain with highest priority (as new chain head).
     * @param propertySources the property sources to add, not null.
     * @param index index where the new property sources should be inserted.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder addPropertySources(int index, Collection<PropertySource> propertySources);

    /**
     * Removes the given property source from the property source chain.
     * @param propertySource the property source. not null.
     * @return the builder for further chaining of logic.
     */
    ConfigurationContextBuilder removePropertySource(PropertySource propertySource);

    /**
     * Access a processor using it's unique property source name. If no such property source
     * is present, this method has no effect.
     * @param name the property source name, not null.
     * @return the builder for further chaining of logic.
     */
    PropertySource getPropertySource(String name);

    /**
     * Get access to the current chain of property sources. This returns an immutable list. Use the
     * methods of this factory to change the ordering or adding/removing of property sources.
     * @return the current chain of property sources (immutable list).
     */
    List<PropertySource> getPropertySourcesChain();

    /**
     * Get access to the current property sources with a given type. This returns an immutable list. Use the
     * methods of this factory to change the ordering or adding/removing of property sources.
     * @return the current chain of property sources (immutable list).
     */
    List<PropertySource> getPropertySources(Class<? extends PropertySource> type);

    /**
     * Get access to the current chain of property sources. This returns an immutable list. Use the
     * methods of this factory to change the ordering or adding/removing of property sources.
     * @return the current chain of property sources as names (immutable list).
     */
    List<String> getPropertySourcesNames();

    /**
     * Builds a new configuration based on the current property sources chain.
     * @return a new configuration instance, never null.
     */
    Configuration build();
}

