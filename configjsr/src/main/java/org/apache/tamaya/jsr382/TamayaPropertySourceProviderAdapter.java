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


import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertySourceProvider;

import javax.config.spi.ConfigSourceProvider;
import java.util.*;

/**
 * Tamaya {@link PropertySourceProvider} implementation that wraps a {@link ConfigSourceProvider} instance.
 */
class TamayaPropertySourceProviderAdapter implements PropertySourceProvider{

    private ConfigSourceProvider delegate;

    /**
     * Creates a new instance.
     * @param configSourceProvider the provider, not null.
     */
    public TamayaPropertySourceProviderAdapter(ConfigSourceProvider configSourceProvider){
        this.delegate = Objects.requireNonNull(configSourceProvider);
    }

    /**
     * Access the underlying provider.
     * @return the provider, not null.
     */
    public ConfigSourceProvider getConfigSourceProvider(){
        return this.delegate;
    }


    @Override
    public Collection<PropertySource> getPropertySources() {
        if(delegate instanceof JavaConfigSourceProvider){
            return ((JavaConfigSourceProvider)delegate).getPropertySourceProvider()
                    .getPropertySources();
        }else {
            return JavaConfigAdapterFactory.toPropertySources(
                    delegate.getConfigSources(Thread.currentThread().getContextClassLoader()));
        }
    }

    @Override
    public String toString() {
        return "TamayaPropertySourceProviderAdapter{" +
                "delegate=" + delegate +
                '}';
    }
}
