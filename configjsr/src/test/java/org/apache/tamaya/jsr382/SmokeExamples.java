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
package org.apache.tamaya.jsr382;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationSnapshot;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.spi.*;

import javax.config.Config;
import javax.config.ConfigAccessor;
import javax.config.ConfigProvider;
import javax.config.ConfigSnapshot;
import javax.config.spi.ConfigBuilder;
import javax.config.spi.ConfigProviderResolver;
import javax.config.spi.ConfigSource;
import javax.config.spi.Converter;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Simple smoke example to use the JDK 382 API.
 */
public class SmokeExamples {

    interface Server{

    }

    static Server defaultServer;

    public static void main(String[] args) {
        new SmokeExamples().filterAndPrint_tamaya();
    }


    public void jsr382(){

        Config config = ConfigProvider.getConfig();

        String textValue = config.getValue("foo.bar.property", String.class);
        int intValue = config.getValue("foo.bar.property", int.class);
        Server serverValue = config.getValue("foo.bar.property", Server.class);
    }

    public void apacheTamaya(){

        Configuration cfg = Configuration.current();

        String textValue = cfg.get("foo.bar.property");
        int intValue = cfg.get("foo.bar.property", int.class);
        Server serverValue = cfg.get("foo.bar.property", Server.class);
    }

    public void jsr382_Optional(){

        Config config = ConfigProvider.getConfig();

        Optional<String> textValue = config.getOptionalValue("foo.bar.property", String.class);
        Optional<Integer> intValue = config.getOptionalValue("foo.bar.property", Integer.class);
        Optional<Server> serverValue = config.getOptionalValue("foo.bar.property", Server.class);
    }

    public void apacheTamaya_Optional(){

        Configuration cfg = Configuration.current();

        Optional<String> textValue = cfg.getOptional("foo.bar.property");
        Optional<Integer> intValue = cfg.getOptional("foo.bar.property", Integer.class);
        Optional<Server> serverValue = cfg.getOptional("foo.bar.property", Server.class);
    }

    public void jsr382_Defaults(){

        Config config = ConfigProvider.getConfig();

        String textValue = config.getOptionalValue("foo.bar.property", String.class).orElse("anyDefault");
        Integer intValue = config.getOptionalValue("foo.bar.property", Integer.class).orElse(1234);
        Server serverValue = config.getOptionalValue("foo.bar.property", Server.class).orElse(defaultServer);
    }

    public void apacheTamaya_Defaults(){

        Configuration cfg = Configuration.current();

        String textValue = cfg.getOrDefault("foo.bar.property", "anyDefault");
        Integer intValue = cfg.getOrDefault("foo.bar.property", Integer.class, 1234);
        Server serverValue = cfg.getOrDefault("foo.bar.property", Server.class, defaultServer);
    }

    public void jsr382_multiKeyLookup(){

        Config config = ConfigProvider.getConfig();

        ConfigAccessor<String> accessor = config.access("foo.bar.property");
        accessor = accessor.addLookupSuffix("DEV").addLookupSuffix("server01");
        accessor = accessor.withDefault("anyDefault");
        String textValue = accessor.getValue();
    }

    public void apacheTamaya_multiKeyLookup(){

        Configuration config = Configuration.current();

        String textValue = config.getOrDefault(
                Arrays.asList(
                    "foo.bar.property.DEV.server1",
                    "foo.bar.property.server1",
                    "foo.bar.property.DEV",
                    "foo.bar.property"),
                "anyDefault");
    }

    public void jsr382_snapshot(){

        Config config = ConfigProvider.getConfig();

        ConfigAccessor<String> accessor = config.access("foo.bar.property");
        accessor = accessor.addLookupSuffix("DEV");
        accessor = accessor.withDefault("anyDefault");
        ConfigAccessor<Integer> accessor2 = config.access("foo.bar.property2").as(Integer.class);
        accessor = accessor.withDefault("1234");
        ConfigSnapshot snapshot = config.snapshotFor(accessor, accessor2);

        String property1 = accessor.getValue(snapshot);
        Integer property2 = accessor2.getValue(snapshot);
    }

    public void apacheTamaya_snapshot(){

        ConfigurationSnapshot config = Configuration.current().getSnapshot(
                "foo.bar.property", "foo.bar.property.DEV", "foo.bar.property2");

        String property1 = config.getOrDefault(
                Arrays.asList("foo.bar.property.DEV", "foo.bar.property"), "anyDefault");
        Integer property2 = config.getOrDefault("foo.bar.property2", Integer.class, 1234);
    }

    public void jsr382_access(){

        Config config = ConfigProvider.getConfig();
        Config config2 = ConfigProvider.getConfig(ClassLoader.getSystemClassLoader());

        ConfigProviderResolver.instance().registerConfig(config, Thread.currentThread().getContextClassLoader());
        ConfigProviderResolver.instance().registerConfig(config2, ClassLoader.getSystemClassLoader());
        ConfigProviderResolver.instance().releaseConfig(config2);
    }

    public void apacheTamaya_access(){

        Configuration config = Configuration.current();
        Configuration config2 = Configuration.current(ClassLoader.getSystemClassLoader());

        Configuration.setCurrent(config);
        Configuration.setCurrent(config2, ClassLoader.getSystemClassLoader());
        Configuration released = Configuration.releaseConfiguration(ClassLoader.getSystemClassLoader());
    }

    public void jsr382_builder(){

        ConfigBuilder builder = ConfigProviderResolver.instance().getBuilder();
        builder.addDefaultSources();
        builder.forClassLoader(ClassLoader.getSystemClassLoader());
        builder.addDiscoveredSources();
        builder.addDiscoveredConverters();
        builder.withConverter(Void.class, 0, s -> null);
        ConfigSource myConfigSource = new MyConfigSource();
        builder.withSources(myConfigSource);
        Config config = builder.build();
    }

    public void apacheTamaya_builder(){

        ConfigurationBuilder builder = Configuration.createConfigurationBuilder();
        builder.setClassLoader(ClassLoader.getSystemClassLoader());
        builder.addDefaultPropertySources();
        builder.addDefaultPropertySources();
        builder.addDefaultPropertyConverters();
        builder.addPropertyConverters(TypeLiteral.of(Void.class), (s, ctx) -> null);
        PropertySource myConfigSource = new MyPropertySource();
        builder.addPropertySources(myConfigSource);
        builder.highestPriority(myConfigSource);
        Configuration config = builder.build();
    }

    public void resolution_jsr(){

        String myKey = "Test ${java.version},${java.foo},${PATH}";

        Config config = ConfigProvider.getConfig();
        String resolvedValue = config.access("myKey").evaluateVariables(true).getValue();
    }

    public void resolution_tamaya(){

        String myKey = "Test ${java.version},${java.foo},${env:PATH}";

        Configuration config = Configuration.current();
        String resolvedValue = config.get("myKey");
    }

    public void filterAndPrint_tamaya(){

        Configuration config = Configuration.current();
        // Filter only java.* entries
        Configuration javaConfig = config.map(ConfigurationFunctions.section("java."));
        // Print them as text
        String text = javaConfig.adapt(ConfigurationFunctions.textInfo());
        System.out.println(text);
        text = javaConfig.adapt(ConfigurationFunctions.xmlInfo());
        System.out.println(text);
    }

    private class MyConfigSource implements ConfigSource {
        @Override
        public Map<String, String> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        public String getValue(String propertyName) {
            return "Config:"+propertyName;
        }

        @Override
        public String getName() {
            return "demo";
        }
    }

    private class MyConverter implements Converter<ServerSocket> {

        @Override
        public ServerSocket convert(String value) {
            try {
                int port = 89;
                int backlog = 0;
                int[] values = null; // parseValues(value);
                return new ServerSocket(values[0],values[1]);
            }catch(Exception e){
                return null;
            }
        }
    }

    private class MyPropertySource implements PropertySource {
        @Override
        public Map<String, PropertyValue> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        public PropertyValue get(String key) {
            return PropertyValue.createValue(key, "Config:"+key);
        }

        @Override
        public String getName() {
            return "demo";
        }
    }

    private class MyTamayaConverter implements PropertyConverter<ServerSocket> {

        @Override
        public ServerSocket convert(String value, ConversionContext context) {
            try {
                String key = context.getKey();
                // the conversion context enables much more powerful conversion...
                int port = context.getConfiguration().getOrDefault(key+".port", int.class, 89);
                int backlog = context.getConfiguration().getOrDefault(key+".backlog", int.class, 20);
                return new ServerSocket(port, backlog);
            }catch(Exception e){
                return null;
            }
        }
    }
}
