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

import org.apache.tamaya.doc.formats.HtmlDocFormat;
import org.apache.tamaya.doc.formats.TextDocFormat;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigDocumenterTest {

    @Test
    public void getDocumentationAndPrint_ConfigBean() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readClasses(AnnotatedDocConfigBean.class);
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertThat(documentation).isNotNull();
        System.out.println(new TextDocFormat().apply(documentation));
    }

    @Test
    public void getDocumentationAndPrint_AnnotationType() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readClasses(AnnotBasedStandaloneConfigDocumentation.class);
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertThat(documentation).isNotNull();
        System.out.println(new TextDocFormat().apply(documentation));
    }

    @Test
    public void getDocumentationAndPrint_Package() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readPackages("org.apache.tamaya.doc");
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertThat(documentation).isNotNull();
        System.out.println(new TextDocFormat().apply(documentation));
    }

    @Test
    public void getDocumentationAndPrint_Package_html() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readPackages("org.apache.tamaya.doc");
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertThat(documentation).isNotNull();
        System.out.println(new HtmlDocFormat().apply(documentation));
    }
}
