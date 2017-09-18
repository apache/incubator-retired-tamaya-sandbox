///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.apache.tamaya.osgi;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.logging.Logger;
//
//import org.apache.tamaya.ConfigurationProvider;
//import org.apache.tamaya.functions.ConfigurationFunctions;
//import org.osgi.service.cm.Configuration;
//
///**
// * Tamaya based implementation of an OSGI {@link Configuration}.
// */
//public class TamayaOSGIConfiguration implements Configuration {
//    private static final Logger LOG = Logger.getLogger(TamayaOSGIConfiguration.class.getName());
//    private final Dictionary<String, Object> parentConfig;
//    private final String pid;
//    private final String factoryPid;
//    private OSGIConfigMapper rootMapper;
//    private boolean overriding = true;
//    private String bundleLocation;
//
//    /**
//     * Constructor.
//     * @param confPid the OSGI pid
//     * @param factoryPid the factory pid
//     * @param configRootMapper the mapper that maps the pids to a tamaya root section.
//     * @param parentConfig the OSGI config for the given context, may be null..
//     */
//    TamayaOSGIConfiguration(String confPid, String factoryPid, OSGIConfigMapper configRootMapper,
//                            Dictionary<String, Object> parentConfig) {
//        this.pid = confPid;
//        this.factoryPid = factoryPid;
//        this.parentConfig = parentConfig;
//        this.rootMapper = Objects.requireNonNull(configRootMapper);
//    }
//
//    public boolean isOverriding() {
//        return overriding;
//    }
//
//    public void setOverriding(boolean overriding){
//        this.overriding = overriding;
//    }
//
//    @Override
//    public String getPid() {
//        return pid;
//    }
//
//    @Override
//    public Dictionary<String, Object> getProperties() {
//        Dictionary<String, Object> properties = new Hashtable<>();
//
//        final String rootKey = this.rootMapper.getTamayaConfigRoot(pid, factoryPid);
//        LOG.info("Configuration: Evaluating Tamaya configuration for '" + rootKey + "'.");
//        org.apache.tamaya.Configuration tamayConfig = ConfigurationProvider.getConfiguration();
//        if(overriding){
//            if(parentConfig!=null) {
//                putAll(properties, parentConfig);
//            }
//            putAll(properties, tamayConfig.with(ConfigurationFunctions.section(rootKey, true)).getProperties());
//        }else{
//            putAll(properties, tamayConfig.with(ConfigurationFunctions.section(rootKey, true)).getProperties());
//            if(parentConfig!=null) {
//                putAll(properties, parentConfig);
//            }
//        }
//        return properties;
//    }
//
//    private void putAll(Dictionary<String, Object> target, Dictionary<String, Object> data) {
//        Enumeration<String> keys = data.keys();
//        while(keys.hasMoreElements()){
//            String key = keys.nextElement();
//            target.put(key, data.get(key));
//        }
//    }
//
//    private void putAll(Dictionary<String, Object> target, Map<String, String> data) {
//        for(Map.Entry<String,String> en:data.entrySet()){
//            target.put(en.getKey(), en.getValue());
//        }
//    }
//
//    @Override
//    public void update(Dictionary<String, ?> properties) throws IOException {
//        throw new UnsupportedOperationException("Mutability is not supported.");
//    }
//
//    @Override
//    public void delete() throws IOException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public String getFactoryPid() {
//        return factoryPid;
//    }
//
//    @Override
//    public void update() throws IOException {
//        // Nothing to do since, we load everything dynamically.
//    }
//
//    @Override
//    public void setBundleLocation(String location) {
//        this.bundleLocation = location;
//    }
//
//    @Override
//    public String getBundleLocation() {
//        return this.bundleLocation;
//    }
//
//    @Override
//    public long getChangeCount() {
//        return 0;
//    }
//
//}
