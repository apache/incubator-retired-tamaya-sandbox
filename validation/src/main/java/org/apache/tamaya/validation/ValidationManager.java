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
package org.apache.tamaya.validation;

import org.apache.tamaya.validation.spi.ConfigValidationMBean;
import org.apache.tamaya.validation.spi.ValidationModelProviderSpi;
import org.apache.tamaya.base.ServiceContextManager;

import javax.config.Config;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validator accessor to validate the current configuration.
 */
public final class ValidationManager {

    /** The logger used. */
    private static final Logger LOG = Logger.getLogger(ValidationManager.class.getName());

    private static final ValidationManager INSTANCE = new ValidationManager();

    private List<ValidationModel> models = new ArrayList<>();

    /**
     * Get the singleton instance.
     * @return the instance, never null.
     */
    public static ValidationManager getInstance(){
        return INSTANCE;
    }

    /**
     * Singleton constructor.
     */
    private ValidationManager() {
        for (ValidationModelProviderSpi model : ServiceContextManager.getServiceContext().getServices(ValidationModelProviderSpi.class)) {
            models.addAll(model.getConfigModels());
        }
        Collections.sort(models, new Comparator<ValidationModel>() {
            @Override
            public int compare(ValidationModel k1, ValidationModel k2) {
                return k2.getName().compareTo(k2.getName());
            }
        });
    }

    /**
     * Access the usage statistics for the recorded uses of configuration.
     * @return usage statistics
     */
    public String getConfigInfoText(){
        StringBuilder b = new StringBuilder();
        b.append("TYPE    OWNER      NAME                                              MANDATORY   DESCRIPTION\n");
        b.append("-----------------------------------------------------------------------------------------------------\n");
        for(ValidationModel model:models){
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

    private String formatWithFixedLength(String name, int targetLength) {
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

    /**
     * Get the validations defined.
     *
     * @return the sections defined, never null.
     */
    public Collection<ValidationModel> getModels() {
        return Collections.unmodifiableCollection(models);
    }


    /**
     * Find the validations by matching the validation's name against the given model type.
     * 
     * @param name the name to use, not null.
     * @param modelType classname of the target model type.  
     * @param <T> type of the model to filter for.
     * @return the sections defined, never null.
     */
    public <T extends ValidationModel> T getModel(String name, Class<T> modelType) {
        for (ValidationModelProviderSpi model : ServiceContextManager.getServiceContext().getServices(ValidationModelProviderSpi.class)) {
            for(ValidationModel configModel : model.getConfigModels()) {
                if(configModel.getName().equals(name) && configModel.getClass().equals(modelType)) {
                    return modelType.cast(configModel);
                }
            }
        }
        return null;
    }

    /**
     * Find the validations by checking the validation's name using the given regular expression.
     * @param namePattern the regular expression to use, not null.
     * @param targets the target types only to be returned (optional).
     * @return the sections defined, never null.
     */
    public Collection<ValidationModel> findModels(String namePattern, ValidationTarget... targets) {
        List<ValidationModel> result = new ArrayList<>();
        for (ValidationModelProviderSpi model : ServiceContextManager.getServiceContext().getServices(ValidationModelProviderSpi.class)) {
            for(ValidationModel configModel : model.getConfigModels()) {
                if(configModel.getName().matches(namePattern)) {
                    if(targets.length>0){
                        for(ValidationTarget tgt:targets){
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
    public Collection<Validation> validate(Config config) {
        return validate(config, false);
    }

    /**
     * Validates the given configuration.
     *
     * @param config the configuration to be validated against, not null.
     * @param showUndefined allows filtering for undefined configuration elements.
     * @return the validation results, never null.
     */
    public Collection<Validation> validate(Config config, boolean showUndefined) {
        List<Validation> result = new ArrayList<>();
        for (ValidationModel defConf : getModels()) {
            result.addAll(defConf.validate(config));
        }
        if(showUndefined){
            Iterable<String> map = config.getPropertyNames();
            Set<String> keys = new HashSet<>();
            map.forEach(keys::add);
            Set<String> areas = extractTransitiveAreas(keys);
            for (ValidationModel defConf : getModels()) {
                if(ValidationTarget.Section.equals(defConf.getType())){
                    for (Iterator<String> iter = areas.iterator();iter.hasNext();){
                        String area = iter.next();
                        if(area.matches(defConf.getName())){
                            iter.remove();
                        }
                    }
                }
                if(ValidationTarget.Parameter.equals(defConf.getType())){
                    keys.remove(defConf.getName());
                }
            }
            outer:for(String key:keys){
                for (ValidationModel defConf : getModels()) {
                    if(ValidationTarget.Section.equals(defConf.getType())){
                        if(defConf.getName().endsWith(".*") && key.matches(defConf.getName())){
                            // Ignore parameters that are part of transitive section.
                            continue outer;
                        }
                    }
                }
                result.add(Validation.checkUndefined("<auto>", key, ValidationTarget.Parameter));
            }
            for(String area:areas){
                result.add(Validation.checkUndefined("<auto>", area, ValidationTarget.Section));
            }
        }
        return result;
    }

    private java.util.Set<java.lang.String> extractTransitiveAreas(Set<String> keys) {
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


    /**
     * Registers the {@link ConfigValidationMBean} mbean for accessing config documentation into the local platform
     * mbean server.
     */
    public void registerMBean() {
        registerMBean(null);
    }

    /**
     * Registers the {@link ConfigValidationMBean} mbean for accessing config documentation into the local platform
     * mbean server.
     * 
     * @param context allows to specify an additional MBean context, maybe {@code null}. 
     */
    public void registerMBean(String context) {
        try{
            ConfigValidationMBean configMbean = ServiceContextManager.getServiceContext()
                    .getService(ConfigValidationMBean.class);
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

    @Override
    public String toString(){
        return "ValidationManager\n  "+getConfigInfoText();
    }

}
