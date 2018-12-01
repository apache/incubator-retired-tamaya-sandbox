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

import org.apache.tamaya.doc.annot.ConfigAreaSpec;
import org.apache.tamaya.doc.annot.ConfigPropertySpec;
import org.apache.tamaya.doc.annot.ConfigSpec;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.spi.PropertyValue;

import java.net.URL;

@ConfigSpec(
        name="Test",
        version="0.1.0",
        description = "Tomcat Configuration based on Tamaya"
)
@ConfigAreaSpec(
        path = "kubernetes",
        description = "Kubernetes Settings",
        areaType = PropertyValue.ValueType.MAP,
        max = 1
)
@ConfigAreaSpec(
        path = "kubernetes.security",
        description = "Kubernetes Security Settings",
        areaType = PropertyValue.ValueType.MAP,
        max = 1
)
@ConfigAreaSpec(
        path = "kubernetes.cluster",
        description = "Kubernetes Cluster Options",
        areaType = PropertyValue.ValueType.MAP,
        max = 1
)
@ConfigAreaSpec(
        path = "<root>",
        description = "Main Options",
        areaType = PropertyValue.ValueType.MAP,
        properties = {
                @ConfigPropertySpec(name="log", description ="Log the server startup in detail, default: false.",
                        valueType = Boolean.class),
                @ConfigPropertySpec(name="refresh", description = "Refresh interval in millis, default: 1000ms",
                        valueType = Long.class),
        }
)
public interface AnnotBasedStandaloneConfigDocumentation {

    @ConfigAreaSpec(
            description = "Tomcat Server Endpoints",
            min = 1,
            areaType = PropertyValue.ValueType.ARRAY)
    @ConfigDefaultSections("servers")
    class Server{
        @ConfigPropertySpec(description = "The server name.")
        @Config(required = true)
        private String name;
        @ConfigPropertySpec(description = "The server url.")
        @Config(required = true)
        private URL url;
    }

}
