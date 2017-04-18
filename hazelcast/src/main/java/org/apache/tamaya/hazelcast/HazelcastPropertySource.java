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
package org.apache.tamaya.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import org.apache.tamaya.mutableconfig.ConfigChangeRequest;
import org.apache.tamaya.mutableconfig.spi.MutablePropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.BasePropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Distributed Propertysource using a in-memory hazelcast cluster.
 * Created by atsticks on 03.11.16.
 *
 * Basically all kind of property entris can be stored. Additionally this property source allows
 * to pass additional meta-entries to control the TTL of the data in milliseconds. For illustration
 * the following map will store {@code my.entry} with a TLL of 20000 milliseconds (20 seconds) and
 * store {@code my.otherEntry} with infinite lifetime (as long as the cluster is alive):
 *
 * {@code
 *     my.entry=myvalue
 *     _my.entry.ttl=20000
 *     my.otherEntry=1234
 * }
 *
 * By default a new hazelcast instance is created, but it is also possible to reuse an existing
 * instance of pass a Hazelcast configuration instance.
 */
public class HazelcastPropertySource extends BasePropertySource
implements MutablePropertySource{
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(HazelcastPropertySource.class.getName());
    /** The Hazelcast config map used. */
    private Map<String, String> configMap = new HashMap<>();
    /** The hazelcast API instance. */
    private HazelcastInstance hazelcastInstance;
    /** The hazelcast map reference ID used, by default {@code tamaya.configuration}. */
    private String mapReference = "tamaya.configuration";
    /** Flag if this property source is read-only. */
    private boolean readOnly = false;

    /**
     * Creates a new instance, hereby using {@code "Hazelcast"} as property source name and
     * a default hazelcast backend created by calling {@link Hazelcast#newHazelcastInstance()}.
     */
    public HazelcastPropertySource(){
        super("Hazelcast");
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    /**
     * Creates a new instance, hereby using {@code "Hazelcast"} as property source name and the
     * given hazelcast instance.
     * @param hazelcastInstance the hazelcast instance, not null.
     */
    public HazelcastPropertySource(HazelcastInstance hazelcastInstance){
        this("Hazelcast", hazelcastInstance);
    }

    /**
     * Creates a new instance, hereby using the given property source name and
     * a default hazelcast backend created by calling {@link Hazelcast#newHazelcastInstance()}.
     * @param name the property source name, not null.
     */
    public HazelcastPropertySource(String name){
        super(name);
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    /**
     * Creates a new instance, hereby using the given property source name and
     * a creating a new hazelcast backend using the given Hazelcast {@link Config}.
     * @param config the hazelcast config, not null.
     * @param name the property source name, not null.
     */
    public HazelcastPropertySource(String name, Config config){
        super(name);
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }

    /**
     * Creates a new instance, hereby using the given property source name and the
     * hazelcast instance.
     * @param name
     * @param hazelcastInstance
     */
    public HazelcastPropertySource(String name, HazelcastInstance hazelcastInstance){
        super(name);
        this.hazelcastInstance = Objects.requireNonNull(hazelcastInstance);
    }

    /**
     * Setting the read-only flag for this instance.
     * @param readOnly if true, the property source will not write back any changes to the
     *                 hazelcast backend.
     */
    public void setReadOnly(boolean readOnly){
        this.readOnly = readOnly;
    }

    /**
     * Flag to check if the property source is read-only.
     * @return true, if the instance is read-only.
     */
    public boolean isReadOnly(){
        return readOnly;
    }

    /**
     * Set the Hazelcast reference name for the Tamaya configuration Map.
     * @param mapReference the map reference to be used, not null.
     */
    public void setMapReference(String mapReference){
        this.mapReference = Objects.requireNonNull(mapReference);
    }

    /**
     * Get the Hazelcast reference name for the Tamaya configuration Map.
     * @return the Hazelcast reference name for the Tamaya configuration Map, never null.
     */
    public String getMapReference(){
        return mapReference;
    }

    /**
     * Get access to the hazelcast instance used.
     * @return the hazelcast instance, not null.
     */
    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    @Override
    public PropertyValue get(String key) {
        Config hcConfig = hazelcastInstance.getConfig();
        String value = hcConfig.getProperty(key);
        if(value==null){
            return null;
        }
        return PropertyValue.builder(key, value, getName())
                .addMetaEntry("backend", "Hazelcast")
                .addMetaEntry("instance", hcConfig.getInstanceName())
                .addMetaEntry("mapReference", mapReference)
                .build();
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        Map<String,String> meta = new HashMap<>();
        meta.put("backend", "Hazelcast");
        meta.put("instance", hazelcastInstance.getConfig().getInstanceName());
        meta.put("mapReference", mapReference);
        return PropertyValue.map(this.configMap, getName(), meta);
    }

    @Override
    public boolean isScannable() {
        return true;
    }

    /**
     * Reloads the configuration map from Hazelcast completely.
     */
    public void refresh() {
        IMap<String,String> config = hazelcastInstance.getMap(mapReference);
        Map<String, String> configMap = new HashMap<>(config);
        this.configMap = configMap;
    }

    @Override
    public void applyChange(ConfigChangeRequest configChange) {
        if(readOnly){
            return;
        }
        IMap<String,String> config = hazelcastInstance.getMap(mapReference);
        for(Map.Entry<String, String> en: configChange.getAddedProperties().entrySet()){
            String metaVal = configChange.getAddedProperties().get("_" + en.getKey()+".ttl");
            if(metaVal!=null){
                try {
                    long ms = Long.parseLong(metaVal);
                    config.put(en.getKey(), en.getValue(), ms, TimeUnit.MILLISECONDS);
                }catch(Exception e){
                    LOG.log(Level.WARNING, "Failed to parse TTL in millis: " + metaVal +
                            " for '"+ en.getKey()+"'", e);
                    config.put(en.getKey(), en.getValue());
                }
            }else {
                config.put(en.getKey(), en.getValue());
            }
        }
        for(String key: configChange.getRemovedProperties()){
            config.remove(key);
        }
        IList<String> taList = hazelcastInstance.getList("_tamaya.transactions");
        taList.add(configChange.getTransactionID());
        config.put("_tamaya.transaction.lastId", configChange.getTransactionID(), 1, TimeUnit.DAYS);
        config.put("_tamaya.transaction.startedAt", String.valueOf(configChange.getStartedAt()), 1, TimeUnit.DAYS);
        config.flush();
        refresh();
    }

    @Override
    protected String toStringValues() {
        return super.toStringValues() +
                "\n  hazelcastInstance=" + hazelcastInstance +
                "\n  name='" + getName() + '\'' +
                "\n  mapReference='" + mapReference + '\'' +
                "\n  readOnly=" + readOnly + '\'';
    }

}
