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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
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

    public enum TaskType{
        PROPERTY,
        BEGIN,
        END,
    }

    private static int maxHistory = 10000;
    private static List<ConfigHistory> history = new LinkedList<ConfigHistory>();

    private long timestamp = System.currentTimeMillis();

    private TaskType type;
    private Object previousValue;
    private Object value;
    private String key;
    private String pid;

    private ConfigHistory(TaskType taskType, String pid){
        this.type = Objects.requireNonNull(taskType);
        this.pid = Objects.requireNonNull(pid);
    }

    public static ConfigHistory configuring(String pid, String info){
        ConfigHistory h = new ConfigHistory(TaskType.BEGIN, pid)
                .setValue(info);
        synchronized (history){
            history.add(h);
            checkHistorySize();
        }
        return h;
    }
    public static ConfigHistory configured(String pid, String info){
        ConfigHistory h = new ConfigHistory(TaskType.END, pid)
                .setValue(info);
        synchronized (history){
            history.add(h);
            checkHistorySize();
        }
        return h;
    }
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

    public static void setMaxHistory(int maxHistory){
        ConfigHistory.maxHistory = maxHistory;
    }

    public static int getMaxHistory(){
        return maxHistory;
    }

    public static List<ConfigHistory> history(){
        return history(null);
    }

    public static void clearHistory(){
        clearHistory(null);
    }

    public static void clearHistory(String pid){
        synchronized (history){
            if(pid==null || pid.isEmpty()) {
                history.clear();
            }else{
                history.removeAll(history(pid));
            }
        }
    }

    public static List<ConfigHistory> history(String pid) {
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

    public static void save(TamayaConfigPlugin plugin){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(history);
            oos.flush();
            Base64.getEncoder().encode(bos.toByteArray());
            plugin.setConfigValue("history", Base64.getEncoder().encode(bos.toByteArray()));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to store config change history.", e);
        }
    }

    public static void restore(TamayaConfigPlugin plugin){
        try{
            String serialized = (String)plugin.getConfigValue("history");
            if(serialized!=null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(serialized));
                ObjectInputStream ois = new ObjectInputStream(bis);
                ConfigHistory.history = (List<ConfigHistory>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to store config change history.", e);
        }
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

    private static void checkHistorySize(){
        while(history.size() > maxHistory){
            history.remove(0);
        }
    }
}
