///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.apache.tamaya.etcd;
//
//import org.apache.tamaya.spi.PropertyValue;
//
//import java.util.*;
//import java.util.logging.Logger;
//
///**
// * Propertysource that is reading configuration from a configured etcd endpoint. Setting
// * {@code etcd.prefix} as system property maps the etcd based configuration
// * to this prefix namespace. Etcd servers are configured as {@code etcd.server.urls} system or environment property.
// * Etcd can be disabled by setting {@code tamaya.etcdprops.disable} either as environment or system property.
// */
//public class EtcdPropertySource extends AbstractEtcdPropertySource{
//
//    private static final Logger LOG = Logger.getLogger(EtcdPropertySource.class.getName());
//
//    public EtcdPropertySource(List<String> server){
//        this();
//        setServer(server);
//    }
//
//    public EtcdPropertySource(String... server){
//        this();
//        setServer(Arrays.asList(server));
//    }
//
//    public EtcdPropertySource(){
//        setDefaultOrdinal(1000);
//        setDirectory(EtcdBackendConfig.getEtcdDirectory());
//        setServer(EtcdBackendConfig.getServers());
//    }
//
//}
