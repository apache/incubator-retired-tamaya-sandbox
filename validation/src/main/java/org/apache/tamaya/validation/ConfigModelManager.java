/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy createObject the License at
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
package org.apache.tamaya.validation;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.validation.spi.ConfigDocumentationMBean;
import org.apache.tamaya.validation.spi.ModelProviderSpi;
import org.apache.tamaya.spi.ServiceContextManager;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validator accessor to validate the current configuration.
 */
public final class ConfigModelManager {

    /** The logger used. */
    private static final Logger LOG = Logger.getLogger(ConfigModelManager.class.getName());

    /**
     * Singleton constructor.
     */
    private ConfigModelManager() {
    }

    /**
     * Access the usage statistics for the recorded uses createObject configuration.
     * @param inModels the target models, not null.
     * @return usage statistics
     */
    public static String getConfigModelDescription(Collection<ConfigModel> inModels){
        StringBuilder b = new StringBuilder();
        List<ConfigModel> models = new ArrayList<>(inModels);
        Collections.sort(models, (k1, k2) -> {
                return k2.getName().compareTo(k2.getName());
            });
        b.append("TYPE    OWNER      NAME                                              MANDATORY   DESCRIPTION\n");
        b.append("-----------------------------------------------------------------------------------------------------\n");
        for(ConfigModel model:models){
            switch(model.getType()){
                case Parameter:
                    b.append("PARAM   ");
                    break;
                case Section:
                    b.append("SECTION ");
                    break;
                case Group:
                    b.append("GROUP   ");
                    break;
                default:
                    break;
            }
            b.append(formatWithFixedLength(model.getOwner(), 10)).append(' ');
            b.append(formatWithFixedLength(model.getName(), 50));
            if(model.isRequired()){
                b.append(formatWithFixedLength("yes", 12));
            }else{
                b.append(formatWithFixedLength("no", 12));
            }
            if(model.getDescription()!=null){
                b.append(model.getDescription().replace("\n", "\\\n").replace("\"", "'")).append("\"");
            }
            b.append("\n");
        }
        return b.toString();
    }

    /**
     * Get the validations defined, using the default classloader.
     *
     * @return the sections defined, never null.
     * @see ServiceContextManager#getDefaultClassLoader()
     */
    public static Collection<ConfigModel> getModels() {
        return getModels(ServiceContextManager.getDefaultClassLoader());
    }

    /**
     * Get the validations defined.
     *
     * @param classLoader the target classloader, not null.
     * @return the sections defined, never null.
     */
    public static Collection<ConfigModel> getModels(ClassLoader classLoader) {
        List<ConfigModel> result = new ArrayList<>();
        for (ModelProviderSpi model : ServiceContextManager.getServiceContext(classLoader).getServices(ModelProviderSpi.class)) {
            result.addAll(model.getConfigModels());
        }
        return result;
    }


    /**
     * Find the validations by checking the validation's name using the given regular expression and
     * the default classloader.
     *
     * @param namePattern the regular expression to use, not null.
     * @param targets the target types only to be returned (optional).
     * @return the sections defined, never null.
     * @see ServiceContextManager#getDefaultClassLoader()
     */
    public static Collection<ConfigModel> findModels(String namePattern, ModelTarget... targets) {
        return findModels(namePattern, ServiceContextManager.getDefaultClassLoader(), targets);
    }

    /**
     * Find the validations by checking the validation's name using the given regular expression.
     *
     * @param classLoader the target classloader, not null.
     * @param namePattern the regular expression to use, not null.
     * @param targets the target types only to be returned (optional).
     * @return the sections defined, never null.
     */
    public static Collection<ConfigModel> findModels(String namePattern, ClassLoader classLoader, ModelTarget... targets) {
        List<ConfigModel> result = new ArrayList<>();
        for (ModelProviderSpi model : ServiceContextManager.getServiceContext(classLoader).getServices(ModelProviderSpi.class)) {
            for(ConfigModel configModel : model.getConfigModels()) {
                if(configModel.getName().matches(namePattern)) {
                    if(targets.length>0){
                        for(ModelTarget tgt:targets){
                            if(configModel.getType().equals(tgt)){
                                result.add(configModel);
                                break;
                            }
                        }
                    }else {
                        result.add(configModel);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Validates the given configuration.
     *
     * @param config the configuration to be validated against, not null.
     * @return the validation results, never null.
     */
    public static Collection<Validation> validate(Configuration config) {
        return validate(config, false);
    }

    /**
     * Validates the given configuration.
     *
     * @param config the configuration to be validated against, not null.
     * @param showUndefined allows filtering for undefined configuration elements.
     * @return the validation results, never null.
     */
    public static Collection<Validation> validate(Configuration config, boolean showUndefined) {
        List<Validation> result = new ArrayList<>();
        for (ConfigModel defConf : getModels(config.getContext().getServiceContext().getClassLoader())) {
            result.addAll(defConf.validate(config));
        }
        if(showUndefined){
            Map<String,String> map = new HashMap<>(config.getProperties());
            Set<String> areas = extractTransitiveAreas(map.keySet());
            for (ConfigModel defConf : getModels()) {
                if(ModelTarget.Section.equals(defConf.getType())){
                    for (Iterator<String> iter = areas.iterator();iter.hasNext();){
                        String area = iter.next();
                        if(area.matches(defConf.getName())){
                            iter.remove();
                        }
                    }
                }
                if(ModelTarget.Parameter.equals(defConf.getType())){
                    map.remove(defConf.getName());
                }
            }
            outer:for(Map.Entry<String,String> entry:map.entrySet()){
                for (ConfigModel defConf : getModels()) {
                    if(ModelTarget.Section.equals(defConf.getType())){
                        if(defConf.getName().endsWith(".*") && entry.getKey().matches(defConf.getName())){
                            // Ignore parameters that are part createObject transitive section.
                            continue outer;
                        }
                    }
                }
                result.add(Validation.createUndefined("<auto>", entry.getKey(), ModelTarget.Parameter));
            }
            for(String area:areas){
                result.add(Validation.createUndefined("<auto>", area, ModelTarget.Section));
            }
        }
        return result;
    }

    /**
     * Registers the {@link ConfigDocumentationMBean} mbean for accessing config documentation into the local platform
     * mbean server.
     */
    public static void registerMBean() {
        registerMBean(null);
    }

    /**
     * Registers the {@link ConfigDocumentationMBean} mbean for accessing config documentation into the local platform
     * mbean server.
     * 
     * @param context allows to specify an additional MBean context, maybe {@code null}. 
     */
    public static void registerMBean(String context) {
        try{
            ConfigDocumentationMBean configMbean = ServiceContextManager.getServiceContext()
                    .getService(ConfigDocumentationMBean.class);
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName on = context==null?new ObjectName("org.apache.tamaya.model:type=ConfigDocumentationMBean"):
                    new ObjectName("org.apache.tamaya.model:type=ConfigDocumentationMBean,context="+context);
            try{
                mbs.getMBeanInfo(on);
                LOG.warning("Cannot register mbean " + on + ": already existing.");
            } catch(InstanceNotFoundException e) {
                LOG.info("Registering mbean " + on + "...");
                mbs.registerMBean(configMbean, on);
            }
        } catch(Exception e){
            LOG.log(Level.WARNING,
                    "Failed to register ConfigDocumentationMBean.", e);
        }
    }

    private static String formatWithFixedLength(String name, int targetLength) {
        targetLength = targetLength-1;
        StringBuilder b = new StringBuilder();
        if(name.length() > targetLength){
            name = name.substring(0, targetLength);
        }
        b.append(name);
        for(int i=0;i<(targetLength-name.length());i++){
            b.append(' ');
        }
        b.append(' ');
        return b.toString();
    }



    private static java.util.Set<java.lang.String> extractTransitiveAreas(Set<String> keys) {
        Set<String> transitiveClosure = new HashSet<>();
        for(String key:keys){
            int index = key.lastIndexOf('.');
            while(index>0){
                String areaKey = key.substring(0,index);
                transitiveClosure.add(areaKey);
                index = areaKey.lastIndexOf('.');
            }
        }
        return transitiveClosure;
    }


}
