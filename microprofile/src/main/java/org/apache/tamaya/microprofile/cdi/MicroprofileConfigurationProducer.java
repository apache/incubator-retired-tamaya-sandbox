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
package org.apache.tamaya.microprofile.cdi;

import org.apache.commons.lang.StringUtils;
import org.apache.tamaya.*;
import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.*;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.WordUtils.uncapitalize;

/**
 * Producer bean for configuration properties.
 */
@ApplicationScoped
public class MicroprofileConfigurationProducer {

    private static final Logger LOGGER = Logger.getLogger(MicroprofileConfigurationProducer.class.getName());

    @Produces
    @ConfigProperty
    public Object resolveAndConvert(final InjectionPoint injectionPoint) {
        LOGGER.finest( () -> "Inject: " + injectionPoint);
        final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
        String key = annotation.name();
        if(key.isEmpty()){
            key = getDefaultKey(injectionPoint);
        }

        // unless the extension is not installed, this should never happen because the extension
        // enforces the resolvability of the config

        String defaultTextValue = annotation.defaultValue().isEmpty() ? null : annotation.defaultValue();
        ConversionContext conversionContext = createConversionContext(key, injectionPoint);
        Object value = resolveValue(defaultTextValue, conversionContext, injectionPoint);
        if (value == null) {
            throw new ConfigException(String.format(
                    "Can't resolve any of the possible config keys: %s to the required target type: %s, supported formats: %s",
                    key, conversionContext.getTargetType(), conversionContext.getSupportedFormats().toString()));
        }
        LOGGER.finest(String.format("Injecting %s for key %s in class %s", key, value.toString(), injectionPoint.toString()));
        return value;
    }

    static String getDefaultKey(InjectionPoint injectionPoint) {
        String memberName = injectionPoint.getMember().getName();
        String beanClassNames[] = injectionPoint.getBean().getBeanClass().getName().split("\\$");
        if(beanClassNames.length==1) {
            return beanClassNames[0] + "." + uncapitalize(memberName);
        }else{
            return beanClassNames[0] + "." + uncapitalize(beanClassNames[1]) + "." + uncapitalize(memberName);
        }
    }

    static ConversionContext createConversionContext(String key, InjectionPoint injectionPoint) {
        final Type targetType = injectionPoint.getAnnotated().getBaseType();
        Configuration config = ConfigurationProvider.getConfiguration();
        ConversionContext.Builder builder = new ConversionContext.Builder(config,
                ConfigurationProvider.getConfiguration().getContext(), key, TypeLiteral.of(targetType));
        if (injectionPoint.getMember() instanceof AnnotatedElement) {
            builder.setAnnotatedElement((AnnotatedElement) injectionPoint.getMember());
        }
        if(targetType instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType)targetType;
            if(pt.getRawType().equals(Provider.class)) {
                builder.setTargetType(TypeLiteral.of(pt.getActualTypeArguments()[0]));
            }
        }
        return builder.build();
    }

    static Object resolveValue(String defaultTextValue, ConversionContext context, InjectionPoint injectionPoint) {
        Config config = ConfigProviderResolver.instance().getConfig();
        String textValue = config.getOptionalValue(context.getKey(), String.class).orElse(defaultTextValue);
        Object value = null;
        if(String.class.equals(context.getTargetType().getRawType())){
            value = textValue;
        }
        if (textValue != null) {
            List<PropertyConverter> converters = ConfigurationProvider.getConfiguration().getContext()
                    .getPropertyConverters((TypeLiteral)context.getTargetType());
            for (PropertyConverter<Object> converter : converters) {
                try {
                    value = converter.convert(textValue, context);
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
        return value;
    }

    @Produces
    public Config getConfiguration(){
        return ConfigProvider.getConfig();
    }

    @Produces
    public ConfigBuilder getConfigBuilder(){
        return ConfigProviderResolver.instance().getBuilder();
    }

//    @Produces
//    @ConfigProperty
//    public Provider getConfiguredProvider(InjectionPoint injectionPoint){
//        final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
//        String key = annotation.name();
//
//        // unless the extension is not installed, this should never happen because the extension
//        // enforces the resolvability of the config
//
//        String defaultTextValue = annotation.defaultValue().isEmpty() ? null : annotation.defaultValue();
//        ConversionContext conversionContext = createConversionContext(key, injectionPoint);
//        return () -> {
//            Object value = resolveValue(defaultTextValue, conversionContext, injectionPoint);
//            if (value == null) {
//                throw new ConfigException(String.format(
//                        "Can't resolve any of the possible config keys: %s to the required target type: %s, supported formats: %s",
//                        key, conversionContext.getTargetType(), conversionContext.getSupportedFormats().toString()));
//            }
//            return value;
//        };
//    }

}
