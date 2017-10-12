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
package org.apache.tamaya.osgi;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class storing the history of changers done to the OSGI configuration by Tamaya.
 * This class can be used in the future to restore the previous state, if needed.
 */
public final class ConfigHistory implements Serializable{

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ConfigHistory.class.getName());
    /** The key of the plugin OSGI configuration, where the history is stored/retrieved. */
    private static final String HISTORY_KEY = "tamaya.history";

    public enum TaskType{
        PROPERTY,
        BEGIN,
        END,
    }
    /** The max number of changes tracked. */
    private static int maxHistory = 10000;
    /** The overall history. */
    private static List<ConfigHistory> history = new LinkedList<ConfigHistory>();

    /** The entry timestamp. */
    private long timestamp = System.currentTimeMillis();
    /** The entry type. */
    private TaskType type;
    /** The previous value. */
    private Object previousValue;
    /** The current value. */
    private Object value;
    /** The key. */
    private String key;
    /** The target PID. */
    private String pid;

    private ConfigHistory(TaskType taskType, String pid){
        this.type = Objects.requireNonNull(taskType);
        this.pid = Objects.requireNonNull(pid);
    }

    /**
     * Creates and registers an entry when starting to configure a bundle.
     * @param pid the PID
     * @param info any info.
     * @return the entry, never null.
     */
    public static ConfigHistory configuring(String pid, String info){
        ConfigHistory h = new ConfigHistory(TaskType.BEGIN, pid)
                .setValue(info);
        synchronized (history){
            history.add(h);
            checkHistorySize();
        }
        return h;
    }

    /**
     * Creates and registers an entry when finished to configure a bundle.
     * @param pid the PID
     * @param info any info.
     * @return the entry, never null.
     */
    public static ConfigHistory configured(String pid, String info){
        ConfigHistory h = new ConfigHistory(TaskType.END, pid)
                .setValue(info);
        synchronized (history){
            history.add(h);
            checkHistorySize();
        }
        return h;
    }

    /**
     * Creates and registers an entry when a property has been changed.
     * @param pid the PID
     * @param key the key, not null.
     * @param previousValue the previous value.
     * @param value the new value.
     * @return the entry, never null.
     */
    public static ConfigHistory propertySet(String pid, String key, Object value, Object previousValue){
        ConfigHistory h = new ConfigHistory(TaskType.PROPERTY, pid)
                .setKey(key)
                .setPreviousValue(previousValue)
                .setValue(value);
        synchronized (history){
            history.add(h);
            checkHistorySize();
        }
        return h;
    }

    /**
     * Sets the maximum history size.
     * @param maxHistory the size
     */
    static void setMaxHistory(int maxHistory){
        ConfigHistory.maxHistory = maxHistory;
    }

    /**
     * Get the max history size.
     * @return the max size
     */
    static int getMaxHistory(){
        return maxHistory;
    }

    /**
     * Access the current history.
     * @return the current history, never null.
     */
    static List<ConfigHistory> getHistory(){
        return getHistory(null);
    }

    /**
     * Clears the history.
     */
    static void clearHistory(){
        clearHistory(null);
    }

    /**
     * Clears the history for a PID.
     * @param pid the pid, null clears the full history.
     */
    static void clearHistory(String pid){
        synchronized (history){
            if("*".equals(pid)) {
                history.clear();
            }else{
                history.removeAll(getHistory(pid));
            }
        }
    }

    /**
     * Get the history for a PID.
     * @param pid the pid, null returns the full history.
     * @return
     */
    public static List<ConfigHistory> getHistory(String pid) {
        if(pid==null || pid.isEmpty()){
            return new ArrayList<>(history);
        }
        synchronized (history) {
            List<ConfigHistory> result = new ArrayList<>();
            for (ConfigHistory h : history) {
                if (h.getPid().startsWith(pid)) {
                    result.add(h);
                }
            }
            return result;
        }
    }

    public TaskType getType(){
        return type;
    }

    public String getPid() {
        return pid;
    }

    public Object getPreviousValue() {
        return previousValue;
    }

    public ConfigHistory setPreviousValue(Object previousValue) {
        this.previousValue = previousValue;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ConfigHistory setValue(Object value) {
        this.value = value;
        return this;
    }

    public String getKey() {
        return key;
    }

    public ConfigHistory setKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public String toString() {
        return "ConfigHistory{" +
                "timestamp=" + timestamp +
                ", previousValue=" + previousValue +
                ", value=" + value +
                ", key='" + key + '\'' +
                '}';
    }


    /**
     * This methd saves the (serialized) history in the plugin's OSGI configuration using
     * the HISTORY_KEY key.
     * @param osgiConfig the plugin config, not null.
     */
    static void save(Dictionary<String,Object> osgiConfig){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(history);
            oos.flush();
            osgiConfig.put(HISTORY_KEY, Base64.getEncoder().encodeToString(bos.toByteArray()));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to store getConfig change history.", e);
        }
    }

    /**
     * Restores the history from the plugin's OSGI configuration.
     * @param osgiConfig
     */
    static void restore(Dictionary<String,Object> osgiConfig){
        try{
            String serialized = (String)osgiConfig.get(HISTORY_KEY);
            if(serialized!=null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(serialized));
                ObjectInputStream ois = new ObjectInputStream(bis);
                ConfigHistory.history = (List<ConfigHistory>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to store getConfig change history.", e);
        }
    }

    private static void checkHistorySize(){
        while(history.size() > maxHistory){
            history.remove(0);
        }
    }
}
