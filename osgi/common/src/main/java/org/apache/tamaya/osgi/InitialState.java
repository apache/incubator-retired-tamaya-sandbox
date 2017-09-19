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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by atsticks on 19.09.17.
 */
public final class InitialState {

    private static Map<String, Dictionary<String,?>> initialConfigState = new ConcurrentHashMap<>();

    private InitialState(){}

    public static void set(String pid, Dictionary<String,?> config){
        initialConfigState.put(pid, config);
    }

    public static Dictionary<String,?> remove(String pid){
        return initialConfigState.remove(pid);
    }

    public static void removeAll(){
        initialConfigState.clear();
    }

    public static Dictionary<String,?> get(String pid){
        return initialConfigState.get(pid);
    }

    public static Map<String,Dictionary<String,?>> get(){
        return new HashMap<>(initialConfigState);
    }

    public static Set<String> getPids(){
        return initialConfigState.keySet();
    }

    public static boolean contains(String pid){
        return initialConfigState.containsKey(pid);
    }

    public static void save(TamayaConfigPlugin plugin){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(bos, "UTF-8", false, 4);
        encoder.writeObject(initialConfigState);
        try {
            bos.flush();
            plugin.setConfigValue("backup", new String(bos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restore(TamayaConfigPlugin plugin){
        String serialized = (String)plugin.getConfigValue("history");
        if(serialized!=null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(serialized.getBytes());
            XMLDecoder encoder = new XMLDecoder(bis);
            InitialState.initialConfigState = (Map<String, Dictionary<String,?>>) encoder.readObject();
        }
    }
}
