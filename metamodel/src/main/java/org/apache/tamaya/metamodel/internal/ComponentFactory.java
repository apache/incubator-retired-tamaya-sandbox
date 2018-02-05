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

package org.apache.tamaya.metamodel.internal;

import org.apache.tamaya.base.ServiceContextManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Small helper class for loading of configured instances.
 */
public final class ComponentFactory<T> {

    private Class<T> type;
    private Map<String, T> serviceContext = new HashMap<>();
    private Set<String> loaded = new HashSet<>();

    /**
     * Constructor.
     * @param type the service type, not null.
     */
    public ComponentFactory(Class<T> type){
        this.type = Objects.requireNonNull(type);
        for(T service: ServiceContextManager.getServiceContext().getServices(type)){
            serviceContext.put(service.getClass().getName(), service);
        }
    }

    /**
     * Creates an instance of the given type based on a type configuration.
     * Type hereby is
     * <ul>
     *     <li>A fully qualified class name</li>
     *     <li>A simple class name of a filter class registered with the current
     *     ServiceContext.</li>
     * </ul>
     * @param identifier the configured type
     * @return the component found, or null.
     */
    public T getComponent(String identifier)
            throws IllegalAccessException, InstantiationException {
        T comp = this.serviceContext.get(identifier);
        if(comp==null){
            for(Map.Entry<String, T> en:serviceContext.entrySet()){
                if(en.getKey().endsWith("."+identifier)){
                    comp = en.getValue();
                }
            }
        }
        // Multiple instances: create a new instance using the parameterless constructor for all subsequent
        // resolutions.
        if(loaded.contains(comp.getClass().getName())){
            return (T)comp.getClass().newInstance();
        }
        // Ensure that the next access will return a new instance.
        loaded.add(comp.getClass().getName());
        return comp;
    }

    public Collection<T> loadInstances(NodeList nodeList) {
        List<T> items = new ArrayList<>();
        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeName().equals("filter")){
                String type = node.getNodeValue();
                try {
                    T item = getComponent(type);
                    ComponentConfigurator.configure(item, node);
                    items.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return items;
    }

    @Override
    public String toString() {
        return "ComponentFactory{" +
                "type=" + type +
                ", serviceContext=" + serviceContext +
                '}';
    }

}
