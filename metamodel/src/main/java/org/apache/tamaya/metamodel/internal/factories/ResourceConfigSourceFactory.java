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

package org.apache.tamaya.metamodel.internal.factories;

import org.osgi.service.component.annotations.Component;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Factory for configuring resource based property sources.
 */
@Component
public class ResourceConfigSourceFactory extends URLConfigSourceFactory {

    private static final Logger LOG = Logger.getLogger(ResourceConfigSourceFactory.class.getName());

    @Override
    public String getName() {
        return "resource";
    }


    protected String example() {
        return "<resource location=\"META-INF/config.xml\"\n" +
                "     formats=\"xml-properties\")>\n";
    }

    protected URL createResource(String location) {
        try {
            return getClass().getClassLoader().getResource(location);
        } catch (Exception e) {
            LOG.warning("Invalid resource '" + location + "'.");
            return null;
        }
    }

}
