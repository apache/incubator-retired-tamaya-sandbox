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
package org.apache.tamaya.hjson;

import org.apache.tamaya.resource.AbstractPathPropertySourceProvider;
import org.apache.tamaya.spi.PropertySource;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Path based default provider for hjson formatted config files.
 */
public class PathBasedHJSONPropertySourceProvider extends AbstractPathPropertySourceProvider{

    private static final Logger LOG = Logger.getLogger(PathBasedHJSONPropertySourceProvider.class.getName());
    private HJSONFormat jsonFormat = new HJSONFormat();

    public PathBasedHJSONPropertySourceProvider(String... paths) {
        super(paths);
    }

    @Override
    protected Collection<PropertySource> getPropertySources(URL url) {
        if(jsonFormat.accepts(url)){
            try {
                return Collections.singletonList(
                        jsonFormat.readConfiguration(url.toString(), url.openStream()).toPropertySource());
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to read yaml file: " +url, e);
            }
        }
        return Collections.emptyList();
    }

}


