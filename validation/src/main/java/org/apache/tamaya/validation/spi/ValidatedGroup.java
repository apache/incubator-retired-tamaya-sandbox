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
package org.apache.tamaya.validation.spi;

import org.apache.tamaya.validation.ConfigValidation;
import org.apache.tamaya.validation.ConfigArea;
import org.apache.tamaya.validation.ConfigValidationResult;

import javax.config.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default configuration Model for a configuration area.
 */
public class ValidatedGroup implements ConfigValidation {

    private final String owner;
    private final String name;
    private boolean required;
    private List<ConfigValidation> childModels = new ArrayList<>();

    public ValidatedGroup(String owner, String name, ConfigValidation... configModels){
        this(owner, name, Arrays.asList(configModels));
    }

    public ValidatedGroup(String owner, String name, Collection<ConfigValidation> configModels){
        this.owner = Objects.requireNonNull(owner);
        this.name = Objects.requireNonNull(name);
        this.childModels.addAll(configModels);
        this.childModels = Collections.unmodifiableList(childModels);
        for(ConfigValidation val: configModels) {
            if(val.isRequired()){
                this.required = true;
                break;
            }
        }
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public ConfigArea getArea() {
        return ConfigArea.Group;
    }

    @Override
    public String getDescription() {
        if(childModels.isEmpty()){
            return null;
        }
        StringBuilder b = new StringBuilder();
        for(ConfigValidation val: childModels){
            b.append("  >> ").append(val);
        }
        return b.toString();
    }

    public Collection<ConfigValidation> getValidations(){
        return childModels;
    }

    @Override
    public Collection<ConfigValidationResult> validate(Config config) {
        List<ConfigValidationResult> result = new ArrayList<>(1);
        for(ConfigValidation child: childModels){
            result.addAll(child.validate(config));
        }
        return result;
    }

    @Override
    public String toString(){
        return String.valueOf(getArea()) + ", size: " + childModels.size() + ": " + getDescription();
    }

}
