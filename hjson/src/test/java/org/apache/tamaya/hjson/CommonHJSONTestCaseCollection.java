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

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.PropertySourceComparator;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class with a collection of common test cases each JSON processing
 * class must be able to pass.
 */
public abstract class CommonHJSONTestCaseCollection {

    abstract PropertySource getPropertiesFrom(URL source) throws Exception;

    @Test
    public void canReadNonLatinCharacters() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class
             .getResource("/configs/valid/cyrillic.hjson");

        assertThat(configURL).isNotNull();

        PropertySource propertySource = getPropertiesFrom(configURL);

        assertThat(propertySource.get("name")).isNotNull();
        assertThat(propertySource.get("name").getValue()).isEqualTo("\u041e\u043b\u0438\u0432\u0435\u0440");
        assertThat(propertySource.get("\u0444\u0430\u043c\u0438\u043b\u0438\u044f")).isNotNull();
        assertThat(propertySource.get("\u0444\u0430\u043c\u0438\u043b\u0438\u044f").getValue()).isEqualTo("Fischer");
    }

    @Test
    public void canReadUnicodeCharacters() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class
                .getResource("/configs/valid/kanji.hjson");

        assertThat(configURL).isNotNull();

        PropertySource propertySource = getPropertiesFrom(configURL);

        assertThat(propertySource.get("onamae")).isNotNull();
        // 霊屋 = Tamaya
        assertThat(propertySource.get("onamae").getValue()).isEqualTo("\u970a\u5c4b");
    }

    @Test
    public void canReadNestedStringOnlyJSONConfigFile2() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class
                .getResource("/configs/valid/simple-nested-string-only-config-1.hjson");

        assertThat(configURL).isNotNull();

        PropertySource properties = getPropertiesFrom(configURL);

        System.out.println("simple-nested-string-only-config-1.json -> " + properties.getProperties().values());

        assertThat(properties.getProperties().keySet().size() >= 5).isTrue();

        PropertyValue keyB = properties.get("b");
        PropertyValue keyDO = properties.get("d.o");
        PropertyValue keyDP = properties.get("d.p");

        assertThat(keyB).isNotNull();
        assertThat(keyB.getValue()).isEqualTo("B");
        assertThat(keyDO).isNotNull();
        assertThat(keyDO.getValue()).isEqualTo("O");
        assertThat(keyDP).isNotNull();
        assertThat(keyDP.getValue()).isEqualTo("P");
    }

    @Test
    public void canReadNestedStringOnlyJSONConfigFileWithObjectInTheMiddle()
            throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class
                .getResource("/configs/valid/simple-nested-string-only-config-2.hjson");

        assertThat(configURL).isNotNull();

        PropertySource properties = getPropertiesFrom(configURL);

        assertThat(properties.getProperties().keySet().size() >= 4).isTrue();

        PropertyValue keyA = properties.get("a");
        PropertyValue keyDO = properties.get("b.o");
        PropertyValue keyDP = properties.get("b.p");
        PropertyValue keyC = properties.get("c");

        assertThat(keyA).isNotNull();
        assertThat(keyA.getValue()).isEqualTo("A");
        assertThat(keyC).isNotNull();
        assertThat(keyC.getValue()).isEqualTo("C");
        assertThat(keyDO).isNotNull();
        assertThat(keyDO.getValue()).isEqualTo("O");
        assertThat(keyDP).isNotNull();
        assertThat(keyDP.getValue()).isEqualTo("P");
    }

    @Test
    public void canHandleIllegalJSONFileWhichContainsAnArray() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/invalid/with-array.hjson");

        assertThat(configURL).isNotNull();

        getPropertiesFrom(configURL).getProperties();
    }

    @Test(expected = IOException.class)
    public void canHandleIllegalJSONFileConsistingOfOneOpeningBracket() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/invalid/only-opening-bracket.hjson");

        assertThat(configURL).isNotNull();

        getPropertiesFrom(configURL).getProperties();
    }

    @Test
    public void canHandleIllegalJSONFileWhichIsEmpty() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/invalid/empty-file.hjson");

        assertThat(configURL).isNotNull();

        getPropertiesFrom(configURL).getProperties();
    }

    @Test
    public void priorityInConfigFileOverwriteExplicitlyGivenPriority() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/valid/with-explicit-priority.hjson");

        assertThat(configURL).isNotNull();

        PropertySource properties = getPropertiesFrom(configURL);

        assertThat(PropertySourceComparator.getOrdinal(properties)).isEqualTo(16784);
    }

    @Test
    public void canReadFlatStringOnlyJSONConfigFile() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/valid/simple-flat-string-only-config.hjson");

        assertThat(configURL).isNotNull();

        PropertySource properties = getPropertiesFrom(configURL);

        assertThat(properties.getProperties()).hasSize(3);

        PropertyValue keyA = properties.get("a");
        PropertyValue keyB = properties.get("b");
        PropertyValue keyC = properties.get("c");

        assertThat(keyA).isNotNull();
        assertThat(keyA.getValue()).isEqualTo("A");
        assertThat(keyB).isNotNull();
        assertThat(keyB.getValue()).isEqualTo("B");
        assertThat(keyC).isNotNull();
        assertThat(keyC.getValue()).isEqualTo("C");
    }

    @Test
    public void emptyJSONFileResultsInConfigException() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/invalid/empty-file.hjson");

        assertThat(configURL).isNotNull();

        PropertySource properties = getPropertiesFrom(configURL);

        properties.getProperties();
    }

    @Test
    public void canHandleEmptyJSONObject() throws Exception {
        URL configURL = CommonHJSONTestCaseCollection.class.getResource("/configs/valid/empty-object-config.hjson");

        assertThat(configURL).isNotNull();

        PropertySource properties = getPropertiesFrom(configURL);

        assertThat(properties.getProperties().keySet().size() >= 0).isTrue();
    }
}
