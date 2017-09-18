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
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.Filter;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceReference;
//import org.osgi.service.cm.Configuration;
//import org.osgi.service.cm.ConfigurationAdmin;
//
///**
// * Tamaya based implementation of an OSGI {@link ConfigurationAdmin}.
// */
//public class TamayaConfigAdminImpl implements ConfigurationAdmin {
//    /** the logger. */
//    private static final Logger LOG = Logger.getLogger(TamayaConfigAdminImpl.class.getName());
//
//    /** The OSGI context. */
//    private final BundleContext context;
//    /** The cached configurations. */
//    private Map<String,Configuration> configs = new ConcurrentHashMap<>();
//    /** The configuration section mapper. */
//    private OSGIConfigMapper configRootMapper;
//
//    /**
//     * Create a new config.
//     * @param context the OSGI context
//     */
//    TamayaConfigAdminImpl(BundleContext context) {
//        this.context = context;
//        this.configRootMapper = loadConfigRootMapper();
//    }
//
//    @Override
//    public Configuration createFactoryConfiguration(String factoryPid) throws IOException {
//        return createFactoryConfiguration(factoryPid, null);
//    }
//
//    @Override
//    public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException {
//        String key = "factory:"+factoryPid;
//        if(location!=null){
//            key += "::"+location;
//        }
//        Configuration config = this.configs.get(key);
//        if (config == null) {
//            Dictionary<String, Object> parentConfig = getParentConfig(null, factoryPid, location);
//            config = new TamayaOSGIConfiguration(null, factoryPid, configRootMapper, parentConfig);
//            this.configs.put(key, config);
//        }
//        return config;
//    }
//
//    @Override
//    public Configuration getConfiguration(String pid, String location) throws IOException {
//        String key = "config:"+pid;
//        if(location!=null){
//            key += "::"+location;
//        }
//        Configuration  config = this.configs.get(key);
//        if (config == null) {
//            Dictionary<String, Object> parentConfig = getParentConfig(pid, null, location);
//            config = new TamayaOSGIConfiguration(pid, null, configRootMapper, parentConfig);
//            this.configs.put(key, config);
//        }
//        return config;
//    }
//
//    @Override
//    public Configuration getConfiguration(String pid) throws IOException {
//        return getConfiguration(pid, null);
//    }
//
//    private Dictionary<String, Object> getParentConfig(String pid, String factoryPid, String location) {
//        Dictionary<String, Object> parentConfig = null;
//        if (context != null) {
//            try {
//                ServiceReference[] refs = context.getAllServiceReferences(ConfigurationAdmin.class.getName(), null);
//                for (ServiceReference<ConfigurationAdmin> ref : refs) {
//                    ConfigurationAdmin parentCand = context.getService(ref);
//                    if (parentCand != null && !(parentCand instanceof TamayaConfigAdminImpl)) {
//                        try {
//                            parentConfig = parentCand.getConfiguration(pid, factoryPid).getProperties();
//                        } catch (IOException e) {
//                            LOG.log(Level.WARNING, "Error reading parent OSGI config.", e);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                LOG.log(Level.SEVERE, "Cannot not evaluate parent/base OSGI config.", e);
//            }
//        }
//        return parentConfig;
//    }
//
//    @Override
//    public Configuration[] listConfigurations(String filter) throws IOException, InvalidSyntaxException {
//        List<Configuration> result = new ArrayList<>();
//        if (filter == null || context == null) {
//            return this.configs.values().toArray(new Configuration[this.configs.size()]);
//        } else {
//            Filter flt = context.createFilter(filter);
//            for(Configuration config:this.configs.values()) {
//                if (flt.match(config.getProperties())) {
//                    result.add(config);
//                }
//            }
//            return result.toArray(new Configuration[result.size()]);
//        }
//    }
//
//    /**
//     * Loads the configuration toor mapper using the OSGIConfigRootMapper OSGI service resolving mechanism. If no
//     * such service is available it loads the default mapper.
//     * @return the mapper to be used, bever null.
//     */
//    private OSGIConfigMapper loadConfigRootMapper() {
//        OSGIConfigMapper mapper = null;
//        if(context!=null) {
//            ServiceReference<OSGIConfigMapper> ref = context.getServiceReference(OSGIConfigMapper.class);
//            if (ref != null) {
//                mapper = context.getService(ref);
//            }
//        }
//        if(mapper==null){
//            mapper = new OSGIConfigMapper() {
//                @Override
//                public String getTamayaConfigRoot(String pid, String factoryPid) {
//                    if(pid!=null) {
//                        return "[" + pid +']';
//                    } else{
//                        return "[" + factoryPid +']';
//                    }
//                }
//                @Override
//                public String toString(){
//                    return "Default OSGIConfigRootMapper(pid -> [bundle:pid], factoryPid -> [bundle:factoryPid]";
//                }
//            };
//        }
//        return mapper;
//    }
//
//}
