/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.management;


import org.apache.tamaya.functions.ConfigurationFunctions;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Default implementation of the {@link ManagedConfigMBean} interface. Each bean binds to the
 * current Configuration instance on creation.
 */
public class ManagedConfig implements ManagedConfigMBean {

    /**
     * Classloader that was active when this instance was created.
     */
    private ClassLoader classLoader;

    /**
     * Constructor, which binds this instance to the current TCCL. In the rare cases where
     * the TCCL is null, this class's classloader is used.
     */
    public ManagedConfig() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        if (this.classLoader == null) {
            this.classLoader = ManagedConfigMBean.class.getClassLoader();
        }
    }

    @Override
    public String getJsonConfigurationInfo() {
        return ConfigurationFunctions.jsonInfo().apply(getConfigurationInternal());
    }

    @Override
    public String getXmlConfigurationInfo() {
        return ConfigurationFunctions.xmlInfo().apply(getConfigurationInternal());
    }

    @Override
    public Map<String, String> getConfiguration() {
        Map<String,String> map = new TreeMap<>();
        for(String key:getConfigurationInternal().getPropertyNames()){
            map.put(key, getConfigurationInternal().getValue(key, String.class));
        }
        return map;
    }

    @Override
    public Map<String, String> getSection(String area, boolean recursive) {
        Map<String,String> map = new TreeMap<>();

        Config config = null;
        if(recursive){
            config = ConfigurationFunctions.sectionsRecursive(area).apply(getConfigurationInternal());
        }else{
            config = ConfigurationFunctions.section(area).apply(getConfigurationInternal());
        }
        for(String key:config.getPropertyNames()) {
            map.put(key, config.getValue(key, String.class));
        }
        return map;
    }

    @Override
    public Set<String> getSections() {
        return ConfigurationFunctions.sections().apply(getConfigurationInternal());
    }

    @Override
    public Set<String> getTransitiveSections() {
        return ConfigurationFunctions.transitiveSections().apply(getConfigurationInternal());
    }

    @Override
    public boolean isAreaExisting(String area) {
        return ConfigurationFunctions.section(area).apply(getConfigurationInternal()).getPropertyNames().iterator().hasNext();
    }

    @Override
    public boolean isAreaEmpty(String area) {
        return getSection(area, true).isEmpty();
    }


    /**
     * Evaluate the current configuration. By default this class is temporarely setting the
     * TCCL to the instance active on bean creation and then calls {@link ConfigProvider#getConfig()}.
     *
     * @return the configuration instance to be used.
     */
    protected Config getConfigurationInternal() {
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        try{
            Thread.currentThread().setContextClassLoader(this.classLoader);
            return ConfigProvider.getConfig();
        } finally{
            Thread.currentThread().setContextClassLoader(currentCL);
        }
    }

}

