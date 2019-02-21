/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.k8s;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.models.V1APIService;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.proto.V1;
import io.kubernetes.client.util.Config;

import java.util.logging.Logger;


/**
 * Accessor for reading to or writing from an etcd endpoint.
 */
class K8SAccessor {

    private static final Logger LOG = Logger.getLogger(K8SAccessor.class.getName());

    public void test(){
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        client.
        V1ConfigMap configMap = new V1ConfigMap().    }

}
