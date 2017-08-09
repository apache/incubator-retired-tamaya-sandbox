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

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;


/**
 * CDI Extension module that adds injection mechanism for configuration.
 *
 * @see org.eclipse.microprofile.config.Config
 * @see org.eclipse.microprofile.config.inject.ConfigProperty
 */
public class MicroprofileCDIExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(MicroprofileCDIExtension.class.getName());

    private final Set<Type> types = new HashSet<>();
    private Bean<?> convBean;

    /**
     * Constructor for loading logging its load.
     */
    public MicroprofileCDIExtension(){
        LOG.finest("Loading Tamaya Microprofile Support...");
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
                final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
                String key = annotation!=null?annotation.name():injectionPoint.getMember().getName();
                Member member = injectionPoint.getMember();
                if(member instanceof Field){
                    if(annotation!=null){
                        types.add(((Field)member).getType());
                        configured = true;
                        LOG.finest(() -> "Enabling Tamaya Microprofile Configuration on bean: " + configuredType.getName());
                        configuredType.addConfiguredMember(injectionPoint, key);
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
            abd.addBean(new ConverterBean(convBean, types));
        }
    }


    /**
     * Internally used conversion bean.
     */
    private static class ConverterBean implements Bean<Object> {

        private final Bean<Object> delegate;
        private final Set<Type> types;

        public ConverterBean(final Bean convBean, final Set<Type> types) {
            this.types = types;
            this.delegate = Objects.requireNonNull(convBean);
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Class<?> getBeanClass() {
            return delegate.getBeanClass();
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return delegate.getInjectionPoints();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return delegate.getScope();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return delegate.getStereotypes();
        }

        @Override
        public boolean isAlternative() {
            return delegate.isAlternative();
        }

        @Override
        public boolean isNullable() {
            return delegate.isNullable();
        }

        @Override
        public Object create(CreationalContext<Object> creationalContext) {
            return delegate.create(creationalContext);
        }

        @Override
        public void destroy(Object instance, CreationalContext<Object> creationalContext) {
            delegate.destroy(instance, creationalContext);
        }
    }

}
