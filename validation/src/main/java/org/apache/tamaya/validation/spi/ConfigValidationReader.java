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
package org.apache.tamaya.validation.spi;

import java.util.*;

import org.apache.tamaya.validation.ConfigValidation;

/**
 * Utility class to read metamodel information from properties. Hereby these properties can be part of a
 * configuration (containing other entriees as well) or be dedicated model definition properties read
 * from any kind of source.
 */
public final class ConfigValidationReader {

    /** The default model entries selector. */
    private static final String DEFAULT_META_INFO_SELECTOR = ".model";

    /**
     * Utility class only.
     */
    private ConfigValidationReader(){}


    /**
     * Loads validations as configured in the given properties.
     * @param owner owner, not null.
     * @param props the properties to be read
     * @return a collection of config validations.
     */
    public static Collection<ConfigValidation> loadValidations(String owner, Map<String,String> props) {
        List<ConfigValidation> result = new ArrayList<>();
        Set<String> itemKeys = new HashSet<>();
        for (String key : props.keySet()) {
            if (key.startsWith("_") &&
                    key.endsWith(DEFAULT_META_INFO_SELECTOR + ".target")) {
                itemKeys.add(key.substring(0, key.length() - ".model.target".length()));
            }
        }
        for (String baseKey : itemKeys) {
            String target = props.get(baseKey + ".model.target");
            String type = props.get(baseKey + ".model.type");
            String value = props.get(baseKey + ".model.transitive");
            boolean transitive = false;
            if(value!=null) {
                transitive = Boolean.parseBoolean(value);
            }
            String description = props.get(baseKey + ".model.description");
            String regEx = props.get(baseKey + ".model.expression");
            String validations = props.get(baseKey + ".model.validations");
            String requiredVal = props.get(baseKey + ".model.required");
            String targetKey = baseKey.substring(1);
            try {
                if ("Parameter".equalsIgnoreCase(target)) {
                    result.add(validateParameter(owner, targetKey,
                            description, type, requiredVal, regEx, validations));
                } else if ("Section".equalsIgnoreCase(target)) {
                    if (transitive) {
                        result.add(validateSection(owner, targetKey + ".*",
                                description, requiredVal, validations));
                    } else {
                        result.add(validateSection(owner, targetKey,
                                description, requiredVal, validations));
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Creates a parameter validation.
     * @param paramName the param name, not null.
     * @param description the optional description
     * @param type the param type, default is String.
     * @param reqVal the required value, default is 'false'.
     * @param regEx an optional regular expression to be checked for this param
     * @param validations the optional custom validations to be performed.
     * @return the new validation for this parameter.
     */
    private static ConfigValidation validateParameter(String owner, String paramName, String description, String type, String reqVal,
                                                      String regEx, String validations) {
        boolean required = "true".equalsIgnoreCase(reqVal);
        ValidatedParameter.Builder builder = ValidatedParameter.builder(owner, paramName).setRequired(required)
                .setDescription(description).setExpression(regEx);
        if(type!=null) {
            builder.setType(type);
        }
//        if(validations!=null) {
//            builder.setValidations(validations);
//        }
       return builder.build();
    }

    /**
     * Creates a section validation.
     * @param sectionName the section's name, not null.
     * @param description the optional description
     * @param reqVal the required value, default is 'false'.
     * @param validations the optional custom validations to be performed.
     * @return the new validation for this section.
     */
    private static ConfigValidation validateSection(String owner, String sectionName, String description, String reqVal,
                                                    String validations) {
        boolean required = "true".equalsIgnoreCase(reqVal);
        ValidatedSection.Builder builder = ValidatedSection.builder(owner, sectionName).setRequired(required)
                .setDescription(description);
        //        if(validations!=null) {
//            builder.setValidations(validations);
//        }
        return builder.build();
    }
}
