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
package org.apache.tamaya.osgi;

import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;

/**
 * Created by atsticks on 18.09.17.
 */
public class DefaultOSGIConfigMapper implements OSGIConfigMapper {

    @Override
    public org.apache.tamaya.Configuration getConfiguration(String pid) {
        if (pid != null) {
            return ConfigurationProvider.getConfiguration()
                    .with(ConfigurationFunctions.section("[" + pid + ']', true));
        }
        return null;
    }

    @Override
    public String toString() {
        return "Default OSGIConfigRootMapper([symbolicName:version/properties] -> [bundle:symbolicName]";
    }
}
