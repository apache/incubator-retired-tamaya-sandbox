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

import org.apache.tamaya.base.configsource.BaseConfigSource;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutableConfigSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Propertysource that is reading configuration from a configured etcd endpoint. Setting
 * {@code etcd.prefix} as system property maps the etcd based configuration
 * to this prefix namespace. Etcd servers are configured as {@code etcd.server.urls} system or environment property.
 * Etcd can be disabled by setting {@code tamaya.etcdprops.disable} either as environment or system property.
 */
public class EtcdConfigSource extends BaseConfigSource
        implements MutableConfigSource {
    private static final Logger LOG = Logger.getLogger(EtcdConfigSource.class.getName());

    private String prefix = System.getProperty("tamaya.etcd.prefix", "");

    private List<EtcdAccessor> etcdBackends;

    private Map<String,String> metaData = new HashMap<>();

    public EtcdConfigSource(String prefix, Collection<String> backends){
        this(backends);
        String metadataPrefix = "[meta]datasource."+getName()+".";
        if(prefix!=null && !prefix.isEmpty()){
            this.prefix = prefix;
            metaData.put(metadataPrefix+"prefix", prefix);
        }
    }

    public EtcdConfigSource(Collection<String> backends){
        etcdBackends = new ArrayList<>();
        for(String s:backends){
            etcdBackends.add(new EtcdAccessor(s));
        }
        setDefaultOrdinal(1000);
        setName("etcd");
        String metadataPrefix = "[meta]datasource."+getName()+".";
        metaData.put(metadataPrefix+"backend", "etcd");
        metaData.put(metadataPrefix+"backends", getBackendConfigString());
    }

    public EtcdConfigSource(){
        prefix = System.getProperty("tamaya.etcd.prefix", "");
        setDefaultOrdinal(1000);
        setName("etcd");
        String metadataPrefix = "[meta]datasource."+getName()+".";
        if(prefix!=null && !prefix.isEmpty()){
            metaData.put(metadataPrefix+"prefix", prefix);
        }
        metaData.put(metadataPrefix+"backend", "etcd");
        metaData.put(metadataPrefix+"backends", getBackendConfigString());
    }

    public EtcdConfigSource(String... backends){
        etcdBackends = new ArrayList<>();
        for (String s : backends) {
            etcdBackends.add(new EtcdAccessor(s));
        }
        setDefaultOrdinal(1000);
        setName("etcd");
        String metadataPrefix = "[meta]datasource."+getName()+".";
        if(prefix!=null && !prefix.isEmpty()){
            metaData.put(metadataPrefix+"prefix", prefix);
        }
        metaData.put(metadataPrefix+"backend", "etcd");
        metaData.put(metadataPrefix+"backends", getBackendConfigString());
    }

    public String getPrefix() {
        return prefix;
    }

    public EtcdConfigSource setPrefix(String prefix) {
        this.prefix = prefix==null?"":prefix;
        String metadataPrefix = "[meta]datasource."+getName()+".";
        if(!prefix.isEmpty()){
            metaData.put(metadataPrefix+"prefix", prefix);
        }else{
            metaData.remove(metadataPrefix+"prefix");
        }
        return this;
    }

    @Override
    public int getOrdinal() {
        String configuredOrdinal = getValue(CONFIG_ORDINAL);
        if(configuredOrdinal!=null){
            try{
                return Integer.parseInt(configuredOrdinal);
            } catch(Exception e){
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Configured ordinal is not an int number: " + configuredOrdinal, e);
            }
        }
        return getDefaultOrdinal();
    }

    @Override
    public String getValue(String key) {
        // check prefix, if key does not start with it, it is not part of our name space
        // if so, the prefix part must be removedProperties, so etcd can resolve without it
        if(!key.startsWith(prefix)){
            return null;
        } else{
            key = key.substring(prefix.length());
        }
        Map<String,String> props;
        for(EtcdAccessor accessor: EtcdBackendConfig.getEtcdBackends()){
            try{
                props = accessor.get(key);
                if(!props.containsKey("_ERROR")) {
                    return props.get(key);
                } else{
                    LOG.log(Level.FINE, "etcd error on " + accessor.getUrl() + ": " + props.get("_ERROR"));
                }
            } catch(Exception e){
                LOG.log(Level.FINE, "etcd access failed on " + accessor.getUrl() + ", trying next...", e);
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        for(EtcdAccessor accessor: getEtcdBackends()){
            try{
                Map<String, String> props = accessor.getProperties("");
                if(!props.containsKey("_ERROR")) {
                    return mapMetadata(props);
                } else{
                    LOG.log(Level.FINE, "etcd error on " + accessor.getUrl() + ": " + props.get("_ERROR"));
                }
            } catch(Exception e){
                LOG.log(Level.FINE, "etcd access failed on " + accessor.getUrl() + ", trying next...", e);
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, String> mapMetadata(Map<String, String> props) {
        Map<String, String> values = new HashMap<>();
        values.putAll(metaData);
        // Evaluate keys
        for(Map.Entry<String,String> entry:props.entrySet()) {
            if (!entry.getKey().startsWith("_")) {
                values.put(prefix + "etcd:" + entry.getKey(), entry.getValue());
            }
        }
        return values;
    }

    @Override
    public void applyChange(ConfigChangeRequest configChange) {
        for(EtcdAccessor accessor: EtcdBackendConfig.getEtcdBackends()){
            try{
                for(String k: configChange.getRemovedProperties()){
                    Map<String,String> res = accessor.delete(k);
                    if(res.get("_ERROR")!=null){
                        LOG.info("Failed to remove key from etcd: " + k);
                    }
                }
                for(Map.Entry<String,String> en:configChange.getAddedProperties().entrySet()){
                    String key = en.getKey();
                    Integer ttl = null;
                    int index = en.getKey().indexOf('?');
                    if(index>0){
                        key = en.getKey().substring(0, index);
                        String rawQuery = en.getKey().substring(index+1);
                        String[] queries = rawQuery.split("&");
                        for(String query:queries){
                            if(query.contains("ttl")){
                                int qIdx = query.indexOf('=');
                                ttl = qIdx>0?Integer.parseInt(query.substring(qIdx+1).trim()):null;
                            }
                        }
                    }
                    Map<String,String> res = accessor.set(key, en.getValue(), ttl);
                    if(res.get("_ERROR")!=null){
                        LOG.info("Failed to add key to etcd: " + en.getKey()  + "=" + en.getValue());
                    }
                }
                // success, stop here
                break;
            } catch(Exception e){
                LOG.log(Level.FINE, "etcd access failed on " + accessor.getUrl() + ", trying next...", e);
            }
        }
    }

    private List<EtcdAccessor> getEtcdBackends(){
        if(etcdBackends==null){
            etcdBackends = EtcdBackendConfig.getEtcdBackends();
            LOG.info("Using etcd backends: " + etcdBackends);
        }
        return etcdBackends;
    }

    @Override
    protected String toStringValues() {
        return  super.toStringValues() +
                "  prefix=" + prefix + '\n' +
                "  backends=" + this.etcdBackends + '\n';
    }

    private String getBackendConfigString() {
        String backendProp = "";
        for(EtcdAccessor acc:getEtcdBackends()){
            if(backendProp.isEmpty()){
                backendProp += acc.getUrl();
            }else{
                backendProp += ", " + acc.getUrl();
            }
        }
        return backendProp;
    }
}
