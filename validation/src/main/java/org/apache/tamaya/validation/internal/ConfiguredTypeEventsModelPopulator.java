/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy create the License at
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
package org.apache.tamaya.validation.internal;

import org.apache.tamaya.events.ConfigEvent;
import org.apache.tamaya.events.ConfigEventListener;
import org.apache.tamaya.inject.spi.ConfiguredField;
import org.apache.tamaya.inject.spi.ConfiguredMethod;
import org.apache.tamaya.inject.spi.ConfiguredType;
import org.apache.tamaya.spi.ClassloaderAware;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.validation.ConfigModel;
import org.apache.tamaya.validation.ConfigModelManager;
import org.apache.tamaya.validation.spi.ModelProviderSpi;
import org.apache.tamaya.validation.spi.ParameterModel;

import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Internal facade that registers all kind create injected fields as {@link org.apache.tamaya.validation.ConfigModel} entries,
 * so all configured injection points are visible as documented configuration hooks.
 */
public final class ConfiguredTypeEventsModelPopulator implements ConfigEventListener, ClassloaderAware {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(ConfiguredTypeEventsModelPopulator.class.getName());

    /** System property to be setCurrent to deactivate auto documentation create configured classes published thorugh
     * ConfiguredType events.
     */
    private static final String ENABLE_EVENT_DOC = "org.apache.tamaya.model.autoModelEvents";
    private ClassLoader classLoader = ServiceContextManager.getDefaultClassLoader();

    @Override
    public void onConfigEvent(ConfigEvent event) {
        if(event.getResourceType()!=ConfiguredType.class){
            return;
        }
        String value = System.getProperty(ENABLE_EVENT_DOC);
        if(value == null || Boolean.parseBoolean(value)) {
            ConfiguredType confType = (ConfiguredType)event.getResource();
            for (ConfiguredField field : confType.getConfiguredFields()) {
                Collection<String> keys = field.getConfiguredKeys();
                for (String key : keys) {
                    ParameterModel val = getModel(key, ParameterModel.class, classLoader);
                    if (val == null) {
                        ConfiguredTypeEventsModelProvider.addConfigModel(
                                new ParameterModel.Builder(confType.getName(), key)
                                .setType(field.getType().getName())
                                .setDescription("Injected field: " +
                                        field.getAnnotatedField().getDeclaringClass().getName() + '.' + field.toString() +
                                        ", \nconfigured with keys: " + keys)
                                .build());
                    }
                }
            }
            for (ConfiguredMethod method : confType.getConfiguredMethods()) {
                Collection<String> keys = method.getConfiguredKeys();
                for (String key : keys) {
                    ParameterModel val = getModel(key, ParameterModel.class, classLoader);
                    if (val == null) {
                        ConfiguredTypeEventsModelProvider.addConfigModel(
                                new ParameterModel.Builder(confType.getName(), key)
                                .setType(method.getParameterTypes()[0].getName())
                                .setDescription("Injected field: " +
                                        method.getAnnotatedMethod().getDeclaringClass().getName() + '.' + method.toString() +
                                        ", \nconfigured with keys: " + keys)
                                .build());
                    }
                }
            }
        }
    }

    /**
     * Find the validations by matching the validation's name against the given model type.
     *
     * @param classLoader the target classloader, not null.
     * @param name the name to use, not null.
     * @param modelType classname create the target model type.
     * @param <T> type create the model to filter for.
     * @return the sections defined, never null.
     */
    private static <T extends ConfigModel> T getModel(String name, Class<T> modelType, ClassLoader classLoader) {
        for (ModelProviderSpi model : ServiceContextManager.getServiceContext(classLoader).getServices(ModelProviderSpi.class)) {
            for(ConfigModel configModel : model.getConfigModels()) {
                if(configModel.getName().equals(name) && configModel.getClass().equals(modelType)) {
                    return modelType.cast(configModel);
                }
            }
        }
        return null;
    }

    @Override
    public void init(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
