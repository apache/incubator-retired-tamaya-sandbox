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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

/**
 * Internally used conversion bean.
 */
class BridgingConfigBean implements Bean<Object> {

    private final Bean<Object> delegate;
    private final Set<Type> types;

    public BridgingConfigBean(final Bean delegate, final Set<Type> types) {
        this.types = types;
        this.delegate = Objects.requireNonNull(delegate);
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
//        Set<InjectionPoint> injectionPoints = delegate.getInjectionPoints();
//        for(InjectionPoint injectionPoint:injectionPoints){
//            final ConfigProperty annotation = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
//            String key = annotation.name();
//            ConversionContext context =
//                    MicroprofileConfigurationProducer.createConversionContext(key, injectionPoint);
//            Object result = MicroprofileConfigurationProducer.resolveValue(annotation.defaultValue(), context, injectionPoint);
//            creationalContext.push(result);
//            return result;
//        }
        return this.delegate.create(creationalContext);
    }

    @Override
    public void destroy(Object instance, CreationalContext<Object> creationalContext) {
        delegate.destroy(instance, creationalContext);
    }
}
