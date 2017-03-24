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
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.Objects;

/**
 * Created by atsticks on 23.03.17.
 */
final class MicroprofileConfigBuilder implements ConfigBuilder{

    private ConfigurationContextBuilder contextBuilder;

    MicroprofileConfigBuilder(ConfigurationContextBuilder contextBuilder){
        this.contextBuilder = Objects.requireNonNull(contextBuilder);
    }

    public ConfigurationContextBuilder getConfigurationContextBuilder(){
        return contextBuilder;
    }

    @Override
    public ConfigBuilder addDefaultSources() {
        contextBuilder.addDefaultPropertySources();
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
