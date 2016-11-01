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

import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;

import java.util.Collection;
import java.util.Objects;

/**
 * Wrapped property source that allows dynamically reassigning the property source's
 * ordinal value. This is needed for reordering the property sources to
 * match the DSL configured ordering.
 */
public final class DefaultRefreshablePropertySourceProvider implements PropertySourceProvider, Refreshable {

    private SourceConfig sourceConfig;
    private PropertySourceProvider wrapped;
    private Collection<PropertySource> propertSources;

    public DefaultRefreshablePropertySourceProvider(SourceConfig sourceConfig)
            throws IllegalAccessException, InstantiationException,
                    ClassNotFoundException {
        this.sourceConfig = Objects.requireNonNull(sourceConfig);
        this.wrapped = Objects.requireNonNull(sourceConfig.create(PropertySourceProvider.class));
        this.propertSources = Objects.requireNonNull(wrapped.getPropertySources());
    }

    @Override
    public Collection<PropertySource> getPropertySources() {
        return this.propertSources;
    }

    @Override
    public ConfigurationContext refresh(ConfigurationContext context) {
        Collection<PropertySource> newPropertySources =
                Objects.requireNonNull(wrapped.getPropertySources());
        ConfigurationContextBuilder builder = context.toBuilder();
        // remove previous sources
        builder.removePropertySources(this.propertSources);
        // add new sources
        builder.addPropertySources(newPropertySources);
        return builder.build();
    }

    @Override
    public String toString() {
        return "WrappedPropertySource{" +
                "sourceConfig=" + sourceConfig +
                ", wrapped=" + wrapped +
                '}';
    }
}
