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
//import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
//import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
//import org.apache.tamaya.spi.ChangeSupport;
//import org.apache.tamaya.spi.PropertyValue;
//import org.apache.tamaya.spisupport.propertysource.BasePropertySource;
//
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * Propertysource that is reading configuration from a configured etcd endpoint. Setting
// * {@code etcd.prefix} as system property maps the etcd based configuration
// * to this prefix namespace. Etcd servers are configured as {@code etcd.server.urls} system or environment property.
// * Etcd can be disabled by setting {@code tamaya.etcdprops.disable} either as environment or system property.
// */
//public abstract class AbstractEtcdPropertySource extends BasePropertySource
//        implements MutablePropertySource{
//
//    private static final Logger LOG = Logger.getLogger(AbstractEtcdPropertySource.class.getName());
//
//    private String directory ="";
//
//    private List<String> servers = new ArrayList<>();
//
//    private List<EtcdAccessor> etcdBackends = new ArrayList<>();
//
//    private Map<String,String> metaData = new HashMap<>();
//
//    private AtomicLong timeoutDuration = new AtomicLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
//
//    private AtomicLong timeout = new AtomicLong();
//
//    /** The Hazelcast config map used. */
//    private Map<String, PropertyValue> configMap = new HashMap<>();
//
//    public AbstractEtcdPropertySource(){
//        this("etcd");
//    }
//
//    public AbstractEtcdPropertySource(String name){
//        super(name);
//        metaData.put("source", "etcd");
//    }
//
//    /**
//     * Get the current timeout, when a reload will be triggered on access.
//     * @return the current timeout, or 0 if no data has been loaded at all.
//     */
//    public long getValidUntil(){
//        return timeout.get();
//    }
//
//    /**
//     * Get the current cache timeout.
//     * @return the timeout duration after which data will be reloaded.
//     */
//    public long getCachePeriod(){
//        return timeoutDuration.get();
//    }
//
//    /**
//     * Set the duration after which the data cache will be reloaded.
//     * @param millis the millis
//     */
//    public void setCacheTimeout(long millis){
//        this.timeoutDuration.set(millis);
//    }
//
//    /**
//     * Get the etc directora accessed.
//     * @return the etc director, not null.
//     */
//    public String getDirectory() {
//        return directory;
//    }
//
//    /**
//     * Sets the etcd directory to read from.
//     * @param directory the directory, not null.
//     */
//    public void setDirectory(String directory) {
//        if(!Objects.equals(this.directory, directory)) {
//            this.directory = Objects.requireNonNull(directory);
//            refresh();
//        }
//    }
//
//    public void setServer(List<String> servers) {
//        if(!Objects.equals(this.servers, servers)) {
//            List<EtcdAccessor> etcdBackends = new ArrayList<>();
//            for (String s : servers) {
//                etcdBackends.add(new EtcdAccessor(s));
//            }
//            this.servers = Collections.unmodifiableList(servers);
//            this.etcdBackends = etcdBackends;
//            metaData.put("backends", servers.toString());
//            refresh();
//        }
//    }
//
//    /**
//     * Get the underlying servers this instance will try to connect to.
//     * @return the server list, not null.
//     */
//    public List<String> getServer(){
//        return servers;
//    }
//
//    /**
//     * Checks for a cache timeout and optionally reloads the data.
//     */
//    public void checkRefresh(){
//        if(this.timeout.get() < System.currentTimeMillis()){
//            refresh();
//        }
//    }
//
//    /**
//     * Reloads the data and updated the cache timeouts.
//     */
//    public void refresh() {
//        for(EtcdAccessor accessor: this.etcdBackends){
//            try{
//                Map<String, String> props = accessor.getProperties(directory);
//                if(!props.containsKey("_ERROR")) {
//                    this.configMap = mapPrefix(props);
//                    this.timeout.set(System.currentTimeMillis() + timeoutDuration.get());
//                } else{
//                    LOG.log(Level.FINE, "etcd error on " + accessor.getUrl() + ": " + props.get("_ERROR"));
//                }
//            } catch(Exception e){
//                LOG.log(Level.FINE, "etcd access failed on " + accessor.getUrl() + ", trying next...", e);
//            }
//        }
//    }
//
//    @Override
//    public int getOrdinal() {
//        PropertyValue configuredOrdinal = get(TAMAYA_ORDINAL);
//        if(configuredOrdinal!=null){
//            try{
//                return Integer.parseInt(configuredOrdinal.getValue());
//            } catch(Exception e){
//                Logger.getLogger(getClass().getName()).log(Level.WARNING,
//                        "Configured ordinal is not an int number: " + configuredOrdinal, e);
//            }
//        }
//        return getDefaultOrdinal();
//    }
//
//    @Override
//    public PropertyValue get(String key) {
//        checkRefresh();
//        return configMap.get(key);
//    }
//
//    @Override
//    public Map<String, PropertyValue> getProperties() {
//        checkRefresh();
//        return configMap;
//    }
//
//    @Override
//    public ChangeSupport getChangeSupport(){
//        return ChangeSupport.SUPPORTED;
//    }
//
//    private Map<String, PropertyValue> mapPrefix(Map<String, String> props) {
//
//        Map<String, PropertyValue> values = new HashMap<>();
//        // Evaluate keys
//        for(Map.Entry<String,String> entry:props.entrySet()) {
//            if (!entry.getKey().startsWith("_")) {
//                PropertyValue val = values.get(entry.getKey());
//                if (val == null) {
//                    val = PropertyValue.createValue(entry.getKey(), "").setMeta("source", getName()).setMeta(metaData);
//                    values.put(entry.getKey(), val);
//                }
//            }
//        }
//        // add getMeta entries
//        for(Map.Entry<String,String> entry:props.entrySet()) {
//            if (entry.getKey().startsWith("_")) {
//                String key = entry.getKey().substring(1);
//                for(String field:new String[]{".createdIndex", ".modifiedIndex", ".ttl",
//                        ".expiration", ".source"}) {
//                    if (key.endsWith(field)) {
//                        key = key.substring(0, key.length() - field.length());
//                        PropertyValue val = values.get(key);
//                        if (val != null) {
//                            val.setMeta(field, entry.getValue());
//                        }
//                    }
//                }
//            }
//        }
//        // Map to createValue map.
////        Map<String, PropertyValue> values = new HashMap<>();
//        for(Map.Entry<String,PropertyValue> en:values.entrySet()) {
//            values.put(en.getKey(), en.getValue());
//        }
//        return values;
//    }
//
//    @Override
//    public void applyChange(ConfigChangeRequest configChange) {
//        for(EtcdAccessor accessor: etcdBackends){
//            try{
//                for(String k: configChange.getRemovedProperties()){
//                    Map<String,String> res = accessor.delete(k);
//                    if(res.get("_ERROR")!=null){
//                        LOG.info("Failed to remove key from etcd: " + k);
//                    }
//                }
//                for(Map.Entry<String,String> en:configChange.getAddedProperties().entrySet()){
//                    String key = en.getKey();
//                    Integer ttl = null;
//                    int index = en.getKey().indexOf('?');
//                    if(index>0){
//                        key = en.getKey().substring(0, index);
//                        String rawQuery = en.getKey().substring(index+1);
//                        String[] queries = rawQuery.split("&");
//                        for(String query:queries){
//                            if(query.contains("ttl")){
//                                int qIdx = query.indexOf('=');
//                                ttl = qIdx>0?Integer.parseInt(query.substring(qIdx+1).trim()):null;
//                            }
//                        }
//                    }
//                    Map<String,String> res = accessor.set(key, en.getValue(), ttl);
//                    if(res.get("_ERROR")!=null){
//                        LOG.info("Failed to add key to etcd: " + en.getKey()  + "=" + en.getValue());
//                    }
//                }
//                // success, stop here
//                break;
//            } catch(Exception e){
//                LOG.log(Level.FINE, "etcd access failed on " + accessor.getUrl() + ", trying next...", e);
//            }
//        }
//    }
//
//
//    @Override
//    protected String toStringValues() {
//        return  super.toStringValues() +
//                "  directory=" + directory + '\n' +
//                "  servers=" + this.servers + '\n';
//    }
//}
