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
//package org.apache.tamaya.etcd;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * Singleton that reads the current etcd setup, especially the possible URLs to be used.
// */
//final class EtcdBackendConfig {
//
//	private static final Logger LOG = Logger.getLogger(EtcdBackendConfig.class.getName());
//	private static final String TAMAYA_ETCD_SERVER_URLS = "tamaya.etcd.server";
//	private static final String TAMAYA_ETCD_TIMEOUT = "tamaya.etcd.timeout";
//    private static final String TAMAYA_ETCD_DIRECTORY = "tamaya.etcd.directory";
//
//
//    private EtcdBackendConfig(){}
//
//    /**
//     * Get the default etcd directory selector, default {@code ""}.
//     * @return the default etcd directory selector, never null.
//     */
//    public static String getEtcdDirectory(){
//        String val = System.getProperty(TAMAYA_ETCD_DIRECTORY);
//        if(val == null){
//            val = System.getenv(TAMAYA_ETCD_DIRECTORY);
//        }
//        if(val!=null){
//            return val;
//        }
//        return "";
//    }
//
//    /**
//     * Get the etcd connection timeout from system/enfironment property {@code tamaya.etcd.timeout (=seconds)}
//     * (default 2 seconds).
//     * @return the etcd connection timeout.
//     */
//    public static long getEtcdTimeout(){
//        String val = System.getProperty(TAMAYA_ETCD_TIMEOUT);
//        if(val == null){
//            val = System.getenv(TAMAYA_ETCD_TIMEOUT);
//        }
//        if(val!=null){
//            return TimeUnit.MILLISECONDS.convert(Integer.parseInt(val), TimeUnit.SECONDS);
//        }
//        return 2000L;
//    }
//
//    /**
//     * Evaluate the etcd target servers fomr system/environment property {@code tamaya.etcd.server}.
//     * @return the servers configured, or {@code http://127.0.0.1:4001} (default).
//     */
//    public static List<String> getServers(){
//        String serverURLs = System.getProperty(TAMAYA_ETCD_SERVER_URLS);
//        if(serverURLs==null){
//            serverURLs = System.getenv(TAMAYA_ETCD_SERVER_URLS);
//        }
//        if(serverURLs==null){
//            serverURLs = "http://127.0.0.1:4001";
//        }
//        List<String> servers = new ArrayList<>();
//        for(String url:serverURLs.split("\\,")) {
//            try{
//                servers.add(url.trim());
//                LOG.info("Using etcd endoint: " + url);
//            } catch(Exception e){
//                LOG.log(Level.SEVERE, "Error initializing etcd accessor for URL: " + url, e);
//            }
//        }
//        return servers;
//    }
//
//}
