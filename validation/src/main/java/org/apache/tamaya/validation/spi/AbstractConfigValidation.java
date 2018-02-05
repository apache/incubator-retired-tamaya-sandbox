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

import java.util.Objects;

/**
 * Default configuration Model for a configuration area.
 */
public abstract class AbstractConfigValidation implements ConfigValidation, Comparable<ConfigValidation> {
    private final String owner;
    private final String name;
    private final String description;
    private boolean required = false;


    protected AbstractConfigValidation(String owner, String name, boolean required, String description) {
        this.name = Objects.requireNonNull(name);
        this.owner = Objects.requireNonNull(owner);
        this.description = description;
        this.required = required;
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
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public int compareTo(ConfigValidation configModel) {
        int compare = getArea().compareTo(configModel.getArea());
        if (compare != 0) {
            return compare;
        }
        return getName().compareTo(configModel.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractConfigValidation that = (AbstractConfigValidation) o;
        return getArea().equals(that.getArea()) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return getArea().hashCode() + name.hashCode();
    }
}
