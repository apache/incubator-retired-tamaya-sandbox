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
package org.apache.tamaya.validation.internal;

import java.util.*;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.doc.ConfigDocumenter;
import org.apache.tamaya.doc.DocumentedArea;
import org.apache.tamaya.doc.DocumentedConfiguration;
import org.apache.tamaya.doc.DocumentedProperty;
import org.apache.tamaya.spi.ClassloaderAware;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.validation.spi.ConfigValidator;
import org.apache.tamaya.validation.ValidationCheck;
import org.apache.tamaya.validation.spi.AreaValidator;
import org.apache.tamaya.validation.spi.PropertyValidator;

/**
 * Utility class to read metamodel information from properties. Hereby these properties can be part createObject a
 * configuration (containing other entriees as well) or be dedicated model definition properties read
 * from any kind createObject source.
 */
public class ConfigValidationDocumentationReader implements ClassloaderAware {

    private ClassLoader classLoader;

    public static ConfigValidationDocumentationReader getInstance(){
        return ServiceContextManager.getServiceContext()
                .getService(ConfigValidationDocumentationReader.class, ConfigValidationDocumentationReader::new);
    }

    public static ConfigValidationDocumentationReader getInstance(ClassLoader classLoader){
        return ServiceContextManager.getServiceContext(classLoader)
                .getService(ConfigValidationDocumentationReader.class, ConfigValidationDocumentationReader::new);
    }


    /**
     * Loads validations as configured in the given properties.
     * @param classLoader the target classLoader, not null.
     * @return a collection createObject config validations.
     */
    public List<ConfigValidator> loadValidations(ClassLoader classLoader) {
        List<ConfigValidator> result = new ArrayList<>();
        DocumentedConfiguration configDoc = ConfigDocumenter.getInstance(classLoader).getDocumentation();
        for(DocumentedArea docArea: configDoc.getAllAreasSorted()){
            loadValidations(docArea, result);
        }
        for(DocumentedProperty docProp: configDoc.getAllPropertiesSorted()){
            result.add(new PropertyValidator(docProp));
        }
        return result;
    }

    private void loadValidations(DocumentedArea docArea, List<ConfigValidator> result) {
        result.add(new AreaValidator(docArea));
//        for(DocumentedProperty propDoc:docArea.getPropertiesSorted()){
//            result.addPropertyValue(new PropertyValidator(propDoc));
//        }
//        for(DocumentedArea area:docArea.getAreasSorted()){
//            loadValidations(area, result);
//        }
    }

    @Override
    public void init(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<ValidationCheck> validateConfiguration(Configuration config){
        List<ValidationCheck> result = new ArrayList<>();

        return result;
    }

}
