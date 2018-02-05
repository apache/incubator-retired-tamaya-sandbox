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
package org.apache.tamaya.etcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton that reads and stores the current etcd setup, especially the possible URLs to be used.
 */
public final class EtcdBackendConfig {

	private static final Logger LOG = Logger.getLogger(EtcdBackendConfig.class.getName());
	private static final String TAMAYA_ETCD_SERVER_URLS = "tamaya.etcd.server.urls";
	private static final String TAMAYA_ETCD_TIMEOUT = "tamaya.etcd.timeout";
    private static final String TAMAYA_ETCD_DISABLE = "tamaya.etcd.disable";
    private static List<EtcdAccessor> etcdBackends = new ArrayList<>();

    static{
        int timeout = 2;
        String val = System.getProperty(TAMAYA_ETCD_TIMEOUT);
        if(val == null){
            val = System.getenv(TAMAYA_ETCD_TIMEOUT);
        }
        if(val!=null){
            timeout = Integer.parseInt(val);
        }
        String serverURLs = System.getProperty(TAMAYA_ETCD_SERVER_URLS);
        if(serverURLs==null){
            serverURLs = System.getenv(TAMAYA_ETCD_SERVER_URLS);
        }
        if(serverURLs==null){
            serverURLs = "http://127.0.0.1:4001";
        }
        for(String url:serverURLs.split("\\,")) {
            try{
                etcdBackends.add(new EtcdAccessor(url.trim(), timeout));
                LOG.info("Using etcd endoint: " + url);
            } catch(Exception e){
                LOG.log(Level.SEVERE, "Error initializing etcd accessor for URL: " + url, e);
            }
        }
    }

    private EtcdBackendConfig(){}

    private static boolean isEtcdDisabled() {
        String value = System.getProperty(TAMAYA_ETCD_DISABLE);
        if (value == null) {
            value = System.getenv(TAMAYA_ETCD_DISABLE);
        }
        return value != null && (value.isEmpty() || Boolean.parseBoolean(value));
    }

    public static List<EtcdAccessor> getEtcdBackends(){
        if(isEtcdDisabled()){
            return Collections.emptyList();
        }
        return etcdBackends;
    }
}
