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
package org.apache.tamaya.hjson;

import org.apache.tamaya.format.ConfigurationData;
import org.apache.tamaya.format.ConfigurationFormat;
import org.hjson.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Implementation of the {@link ConfigurationFormat}
 * able to read configuration properties with comments represented in HJSON
 *
 * @see <a href="http://www.hjson.org">JSON format specification</a>
 */
public class HJSONFormat implements ConfigurationFormat {

    @Override
    public String getName() {
        return "hjson";
    }

    @Override
    public boolean accepts(URL url) {
        return Objects.requireNonNull(url).getPath().endsWith(".hjson");
    }

    @Override
    public ConfigurationData readConfiguration(String resource, InputStream inputStream)
    throws IOException{
        try{
            JsonValue root = JsonValue.readHjson(new InputStreamReader(inputStream,  Charset.forName("UTF-8")));
            HJSONDataBuilder dataBuilder = new HJSONDataBuilder(resource, root);
            return new ConfigurationData(resource, this, dataBuilder.build());
        } catch(Exception e) {
            throw new IOException("Failed to read data from " + resource, e);
        }
    }
}
