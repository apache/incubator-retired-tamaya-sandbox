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
package org.apache.tamaya.jsr382.cdi;

import javax.config.inject.ConfigProperty;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.inject.Provider;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


/**
 * CDI Extension module that adds injection mechanism for configuration.
 *
 * @see javax.config.Config
 */
public class JavaConfigCDIExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(JavaConfigCDIExtension.class.getName());

    private final Set<Type> types = new HashSet<>();
    private Bean<?> convBean;

    /**
     * Constructor for loading logging its load.
     */
    public JavaConfigCDIExtension(){
        LOG.finest("Loading Tamaya JavaConfig Support...");
    }

    /**
     * Method that checks the configuration injection points during deployment for available configuration.
     * @param pb the bean to process.
     * @param beanManager the bean manager to notify about new injections.
     */
    public void retrieveTypes(@Observes final ProcessBean<?> pb, BeanManager beanManager) {

        final Set<InjectionPoint> ips = pb.getBean().getInjectionPoints();
        ConfiguredType configuredType = new ConfiguredType(pb.getBean().getBeanClass());

        boolean configured = false;
        for (InjectionPoint injectionPoint : ips) {
            if (injectionPoint.getAnnotated().isAnnotationPresent(ConfigProperty.class)) {
                LOG.fine("Configuring: " + injectionPoint);
                final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
                String key = !annotation.name().isEmpty()?annotation.name():JavaConfigConfigurationProducer.getDefaultKey(injectionPoint);
                configuredType.addConfiguredMember(injectionPoint, key);
                Type originalType = injectionPoint.getType();
                Type convertedType = unwrapType(originalType);
                types.add(convertedType);
                configured = true;
                LOG.finest(() -> "Enabling Tamaya JavaConfig Configuration on bean: " + configuredType.getName());
            }else if(injectionPoint.getMember() instanceof Method){
                Method method = (Method)injectionPoint.getMember();
                for(AnnotatedType paramType: method.getAnnotatedParameterTypes()){
                    if(paramType.isAnnotationPresent(ConfigProperty.class)) {
                        LOG.fine("Configuring method: " + injectionPoint);
                        final ConfigProperty annotation = paramType.getAnnotation(ConfigProperty.class);
                        String key = !annotation.name().isEmpty() ? annotation.name() : JavaConfigConfigurationProducer.getDefaultKey(injectionPoint);
                        configuredType.addConfiguredMember(injectionPoint, key);
                        Type originalType = paramType.getType();
                        Type convertedType = unwrapType(originalType);
                        types.add(convertedType);
                        configured = true;
                        LOG.finest(() -> "Enabling Tamaya JavaConfig Configuration on bean: " + configuredType.getName());
                    }
                }
            }
        }
        if(configured) {
            beanManager.fireEvent(configuredType);
        }
    }


    public void captureConvertBean(@Observes final ProcessProducerMethod<?, ?> ppm) {
        if (ppm.getAnnotated().isAnnotationPresent(ConfigProperty.class)) {
            convBean = ppm.getBean();
        }
    }

    public void addConverter(@Observes final AfterBeanDiscovery abd, final BeanManager bm) {
        if(!types.isEmpty() && convBean!=null) {
            abd.addBean(new BridgingConfigBean(convBean, types));
        }
    }

    private Type unwrapType(Type type) {
        if(type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if(rawType == Provider.class || rawType == Instance.class) {
                return ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
        return type;
    }

}
