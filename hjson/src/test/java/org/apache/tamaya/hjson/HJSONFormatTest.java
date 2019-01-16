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
import org.apache.tamaya.format.MappedConfigurationDataPropertySource;
import org.apache.tamaya.spi.PropertySource;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class HJSONFormatTest extends CommonHJSONTestCaseCollection {
    private final HJSONFormat format = new HJSONFormat();

    @Test(expected = NullPointerException.class)
    public void acceptsNeedsNonNullParameter() throws Exception {
        format.accepts(null);
    }

    @Test
    public void aNonJSONFileBasedURLIsNotAccepted() throws Exception {
        URL url = new URL("file:///etc/service/conf.conf");

        assertThat(format.accepts(url)).isFalse();
    }

    @Test
    public void aJSONFileBasedURLIsAccepted() throws Exception {
        URL url = new URL("file:///etc/service/conf.hjson");

        assertThat(format.accepts(url)).isTrue();
    }

    @Test
    public void aHTTPBasedURLIsNotAccepted() throws Exception {
        URL url = new URL("http://nowhere.somewhere/conf.hjson");
        assertThat(format.accepts(url)).isTrue();
    }

    @Test
    public void aFTPBasedURLIsNotAccepted() throws Exception {
        URL url = new URL("ftp://nowhere.somewhere/a/b/c/d/conf.hjson");

        assertThat(format.accepts(url)).isTrue();
    }

    @Override
    PropertySource getPropertiesFrom(URL source) throws Exception {
        try (InputStream is = source.openStream()) {
            ConfigurationData data = format.readConfiguration(source.toString(), is);
            return new MappedConfigurationDataPropertySource(data);
        }
    }
}
