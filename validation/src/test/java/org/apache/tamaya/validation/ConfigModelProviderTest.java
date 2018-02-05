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
package org.apache.tamaya.validation;

import org.apache.tamaya.validation.spi.ValidatedSection;
import org.apache.tamaya.validation.spi.ValidatedParameter;
import org.apache.tamaya.validation.spi.ValidatedGroup;
import org.apache.tamaya.validation.spi.ConfigValidationProviderSpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Created by Anatole on 09.08.2015.
 */
public class ConfigModelProviderTest implements ConfigValidationProviderSpi {

    private List<ConfigValidation> configModels = new ArrayList<>(1);

    public ConfigModelProviderTest(){
        configModels.add(new TestConfigModel());
        configModels = Collections.unmodifiableList(configModels);
    }

    public Collection<ConfigValidation> getConfigValidations() {
        return configModels;
    }

    private static final class TestConfigModel extends ValidatedGroup {

        public TestConfigModel(){
            super("TestConfigModel", "TestConfig", new ValidatedSection.Builder("TestConfigModel",
                    "a.test.existing").setRequired(true).build(),
                    ValidatedParameter.of("TestConfigModel", "a.test.existing.aParam", true),
                    ValidatedParameter.of("TestConfigModel", "a.test.existing.optionalParam"),
                    ValidatedParameter.of("TestConfigModel", "a.test.existing.aABCParam", false, "[ABC].*"),
                    new ValidatedSection.Builder("TestConfigModel", "a.test.notexisting").setRequired(true).build(),
                    ValidatedParameter.of("TestConfigModel", "a.test.notexisting.aParam", true),
                    ValidatedParameter.of("TestConfigModel", "a.test.notexisting.optionalParam"),
                    ValidatedParameter.of("TestConfigModel", "a.test.existing.aABCParam2", false, "[ABC].*"));
        }
        @Override
        public String getName() {
            return "TestConfigConfigModel";
        }

    }

}
