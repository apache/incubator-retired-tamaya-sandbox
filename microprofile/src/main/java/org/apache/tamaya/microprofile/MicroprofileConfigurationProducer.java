/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tamaya.microprofile;

import org.apache.tamaya.*;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Producer bean for configuration properties.
 */
@ApplicationScoped
public class MicroprofileConfigurationProducer {

    private static final Logger LOGGER = Logger.getLogger(MicroprofileConfigurationProducer.class.getName());

    @Produces
    @ConfigProperty
    public Object resolveAndConvert(final InjectionPoint injectionPoint) {
        final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
        String key = annotation.name();

        // unless the extension is not installed, this should never happen because the extension
        // enforces the resolvability of the config
        Configuration config = ConfigurationProvider.getConfiguration();
        final Class<?> toType = (Class<?>) injectionPoint.getAnnotated().getBaseType();
        String defaultTextValue = annotation.defaultValue().isEmpty() ? null : annotation.defaultValue();
        String textValue = config.get(key);
        ConversionContext.Builder builder = new ConversionContext.Builder(config,
                ConfigurationProvider.getConfiguration().getContext(), key, TypeLiteral.of(toType));
        if (injectionPoint.getMember() instanceof AnnotatedElement) {
            builder.setAnnotatedElement((AnnotatedElement) injectionPoint.getMember());
        }
        ConversionContext conversionContext = builder.build();
        if (textValue == null) {
            textValue = defaultTextValue;
        }
        Object value = null;
        if (textValue != null) {
            List<PropertyConverter<Object>> converters = ConfigurationProvider.getConfiguration().getContext()
                    .getPropertyConverters(TypeLiteral.of(toType));
            for (PropertyConverter<Object> converter : converters) {
                try {
                    value = converter.convert(textValue, conversionContext);
                    if (value != null) {
                        LOGGER.log(Level.FINEST, "Parsed default value from '" + textValue + "' into " +
                                injectionPoint);
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.FINEST, "Failed to convert value '" + textValue + "' for " +
                            injectionPoint, e);
                }
            }
        }
        if (value == null) {
            throw new ConfigException(String.format(
                    "Can't resolve any of the possible config keys: %s to the required target type: %s, supported formats: %s",
                    key, toType.getName(), conversionContext.getSupportedFormats().toString()));
        }
        LOGGER.finest(String.format("Injecting %s for key %s in class %s", key, value.toString(), injectionPoint.toString()));
        return value;
    }

    @Produces
    public Config getConfiguration(){
        return ConfigProvider.getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Produces
    public ConfigBuilder getConfigBuilder(){
        return ConfigProviderResolver.instance().getBuilder();
    }

}
