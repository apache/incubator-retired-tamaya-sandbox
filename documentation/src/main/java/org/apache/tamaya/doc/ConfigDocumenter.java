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
package org.apache.tamaya.doc;

import org.apache.tamaya.doc.annot.*;
import org.apache.tamaya.spi.ServiceContextManager;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read and store the current configuration documentation.
 */
public class ConfigDocumenter {

    private ConfigurationDocumentation docs = new ConfigurationDocumentation();


    public static ConfigDocumenter getInstance(){
        return ServiceContextManager.getServiceContext()
                .getService(ConfigDocumenter.class, ConfigDocumenter::new);
    }

    public static ConfigDocumenter getInstance(ClassLoader classLoader){
        return ServiceContextManager.getServiceContext(classLoader)
                .getService(ConfigDocumenter.class, ConfigDocumenter::new);
    }

    /**
     * Read documentation from the given classes.
     * @param clazzes the classes to read, not null.
     */
    public void readClasses(Class... clazzes){
        FilterBuilder filterBuilder = new FilterBuilder();
        List<URL> urls = new ArrayList<>(clazzes.length);
        for(Class clazz:clazzes){
//            filterBuilder.exclude(".*");
            filterBuilder.include(clazz.getName()+".*");
            urls.add(ClasspathHelper.forClass(clazz));
        }
        ConfigurationBuilder configBuilder = new ConfigurationBuilder()
                .setScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner(), new FieldAnnotationsScanner())
                .setUrls(urls)
                .filterInputsBy(filterBuilder);
        Reflections reflections = new Reflections(configBuilder);
        readSpecs(reflections);
    }

    /**
     * Read documentation from the classes managed by the given classloader.
     * @param classLoader the classloader, not null.
     */
    public void readClasses(ClassLoader classLoader){
        ConfigurationBuilder configBuilder = new ConfigurationBuilder()
                .setScanners(new TypeAnnotationsScanner(), new MethodAnnotationsScanner(), new FieldAnnotationsScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoader));
        Reflections reflections = new Reflections(configBuilder);
        readSpecs(reflections);
    }

    /**
     * Read documentation from the given packages.
     * @param packages the package names, not null.
     */
    public void readPackages(String... packages) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        FilterBuilder filterBuilder = new FilterBuilder();
        for (String p : packages) {
            filterBuilder.includePackage(p);
        }
        configBuilder.filterInputsBy(filterBuilder);
        configBuilder.setUrls(ClasspathHelper.forJavaClassPath());
        configBuilder.setScanners(new TypeAnnotationsScanner(),
                new MethodAnnotationsScanner(), new FieldAnnotationsScanner());
        Reflections reflections = new Reflections(configBuilder);
        readSpecs(reflections);
    }

    /**
     * Access the collected configuration documentation.
     * @return the documentation, not null.
     */
    public ConfigurationDocumentation getDocumentation(){
        return docs;
    }

    private void readSpecs(Reflections reflections){
        // types
        for(Class type:reflections.getTypesAnnotatedWith(ConfigSpec.class)){
            readConfigSpec(type);
        }
        for(Class type:reflections.getTypesAnnotatedWith(ConfigAreaSpec.class)){
            readAreaSpec(type);
        }
        for(Class type:reflections.getTypesAnnotatedWith(ConfigAreaSpecs.class)){
            readAreaSpecs(type);
        }
        for(Class type:reflections.getTypesAnnotatedWith(ConfigPropertySpec.class)){
            readPropertySpec(type);
        }
        for(Class type:reflections.getTypesAnnotatedWith(ConfigPropertySpecs.class)){
            readPropertySpecs(type);
        }
        // Fields
        for(Field f:reflections.getFieldsAnnotatedWith(ConfigAreaSpec.class)){
            readAreaSpec(f);
        }
        for(Field f:reflections.getFieldsAnnotatedWith(ConfigAreaSpecs.class)){
            readAreaSpecs(f);
        }
        for(Field f:reflections.getFieldsAnnotatedWith(ConfigPropertySpec.class)){
            readPropertySpec(f);
        }
        for(Field f:reflections.getFieldsAnnotatedWith(ConfigPropertySpecs.class)){
            readPropertySpecs(f);
        }
        // Methods
        for(Method m:reflections.getMethodsAnnotatedWith(ConfigAreaSpec.class)){
            readAreaSpec(m);
        }
        for(Method m:reflections.getMethodsAnnotatedWith(ConfigAreaSpecs.class)){
            readAreaSpecs(m);
        }
        for(Method m:reflections.getMethodsAnnotatedWith(ConfigPropertySpec.class)){
            readPropertySpec(m);
        }
        for(Method m:reflections.getMethodsAnnotatedWith(ConfigPropertySpecs.class)){
            readPropertySpecs(m);
        }
    }

    private void readConfigSpec(Class type){
        ConfigSpec configAnnot = (ConfigSpec)type.getAnnotation(ConfigSpec.class);
        docs.init(configAnnot, type);
        for (ConfigAreaSpec areaSpec:configAnnot.areas()){
            for(String basePath:areaSpec.basePaths()) {
                docs.addGroup(new DocumentedArea(areaSpec, type));
            }
        }
        for (ConfigPropertySpec propertySpec:configAnnot.properties()){
            docs.addProperty(new DocumentedProperty(propertySpec, type));
        }
    }

    private void readAreaSpecs(AnnotatedElement elem){
        ConfigAreaSpecs areaSpecs = elem.getAnnotation(ConfigAreaSpecs.class);
        for (ConfigAreaSpec areaSpec:areaSpecs.value()){
            for(String basePath:areaSpec.basePaths()) {
                docs.addGroup(new DocumentedArea(areaSpec, elem));
            }
        }
    }

    private void readPropertySpecs(AnnotatedElement elem){
        ConfigPropertySpecs propertySpecs = elem.getAnnotation(ConfigPropertySpecs.class);
        for (ConfigPropertySpec propertySpec:propertySpecs.value()){
            docs.addProperty(new DocumentedProperty(propertySpec, elem));
        }
    }

    private void readAreaSpec(AnnotatedElement elem){
        docs.addGroup(new DocumentedArea(elem.getAnnotation(ConfigAreaSpec.class), elem));
    }

    private void readPropertySpec(AnnotatedElement elem){
        docs.addProperty(new DocumentedProperty(elem.getAnnotation(ConfigPropertySpec.class), elem));
    }

}
