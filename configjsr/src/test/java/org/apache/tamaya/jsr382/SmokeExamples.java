///*
// * Licensed to the Apache Software Foundation (ASF) under one
// *  or more contributor license agreements.  See the NOTICE file
// *  distributed with this work for additional information
// *  regarding copyright ownership.  The ASF licenses this file
// *  to you under the Apache License, Version 2.0 (the
// *  "License"); you may not use this file except in compliance
// *  with the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// */
//package org.apache.tamaya.jsr382;
//
//import org.apache.tamaya.Configuration;
//import org.apache.tamaya.ConfigurationSnapshot;
//
//import javax.config.Config;
//import javax.config.ConfigAccessor;
//import javax.config.ConfigProvider;
//import javax.config.ConfigSnapshot;
//import java.util.Arrays;
//import java.util.Optional;
//
///**
// * Simple smoke example to use the JDK 382 API.
// */
//public class SmokeExamples {
//
//
//    static Server defaultServer;
//
//    public static void main(String[] args) {
//    }
//
//
//    public void jsr382(){
//
//        Config config = ConfigProvider.getConfig();
//
//        String textValue = config.getValue("foo.bar.property", String.class);
//        int intValue = config.getValue("foo.bar.property", int.class);
//        Server serverValue = config.getValue("foo.bar.property", Server.class);
//    }
//
//    public void apacheTamaya(){
//
//        Configuration cfg = Configuration.current();
//
//        String textValue = cfg.get("foo.bar.property");
//        int intValue = cfg.get("foo.bar.property", int.class);
//        Server serverValue = cfg.get("foo.bar.property", Server.class);
//    }
//
//    public void jsr382_Optional(){
//
//        Config config = ConfigProvider.getConfig();
//
//        Optional<String> textValue = config.getOptionalValue("foo.bar.property", String.class);
//        Optional<Integer> intValue = config.getOptionalValue("foo.bar.property", Integer.class);
//        Optional<Server> serverValue = config.getOptionalValue("foo.bar.property", Server.class);
//    }
//
//    public void apacheTamaya_Optional(){
//
//        Configuration cfg = Configuration.current();
//
//        Optional<String> textValue = cfg.getOptional("foo.bar.property");
//        Optional<Integer> intValue = cfg.getOptional("foo.bar.property", Integer.class);
//        Optional<Server> serverValue = cfg.getOptional("foo.bar.property", Server.class);
//    }
//
//    public void jsr382_Defaults(){
//
//        Config config = ConfigProvider.getConfig();
//
//        String textValue = config.getOptionalValue("foo.bar.property", String.class).orElse("anyDefault");
//        Integer intValue = config.getOptionalValue("foo.bar.property", Integer.class).orElse(1234);
//        Server serverValue = config.getOptionalValue("foo.bar.property", Server.class).orElse(defaultServer);
//    }
//
//    public void apacheTamaya_Defaults(){
//
//        Configuration cfg = Configuration.current();
//
//        String textValue = cfg.getOrDefault("foo.bar.property", "anyDefault");
//        Integer intValue = cfg.getOrDefault("foo.bar.property", Integer.class, 1234);
//        Server serverValue = cfg.getOrDefault("foo.bar.property", Server.class, defaultServer);
//    }
//
//    public void jsr382_multiKeyLookup(){
//
//        Config config = ConfigProvider.getConfig();
//
//        ConfigAccessor<String> accessor = config.access("foo.bar.property");
//        accessor = accessor.addLookupSuffix("DEV").addLookupSuffix("server01");
//        accessor = accessor.withDefault("anyDefault");
//        String textValue = accessor.getValue();
//    }
//
//    public void apacheTamaya_multiKeyLookup(){
//
//        Configuration config = Configuration.current();
//
//        String textValue = config.getOrDefault(
//                Arrays.asList(
//                    "foo.bar.property.DEV.server1",
//                    "foo.bar.property.server1",
//                    "foo.bar.property.DEV",
//                    "foo.bar.property"),
//                "anyDefault");
//    }
//
//    public void jsr382_snapshot(){
//
//        Config config = ConfigProvider.getConfig();
//
//        ConfigAccessor<String> accessor = config.access("foo.bar.property");
//        accessor = accessor.addLookupSuffix("DEV");
//        accessor = accessor.withDefault("anyDefault");
//        ConfigAccessor<Integer> accessor2 = config.access("foo.bar.property2").as(Integer.class);
//        accessor = accessor.withDefault("1234");
//        ConfigSnapshot snapshot = config.snapshotFor(accessor, accessor2);
//
//        String property1 = accessor.getValue(snapshot);
//        Integer property2 = accessor2.getValue(snapshot);
//    }
//
//    public void apacheTamaya_snapshot(){
//
//        ConfigurationSnapshot config = Configuration.current().getSnapshot(
//                "foo.bar.property", "foo.bar.property.DEV", "foo.bar.property2");
//
//        String property1 = config.getOrDefault(
//                Arrays.asList("foo.bar.property.DEV", "foo.bar.property"),
//                "anyDefault");
//        Integer property2 = config.getOrDefault(
//                "foo.bar.property2", Integer.class, 1234);
//    }
//
//}
