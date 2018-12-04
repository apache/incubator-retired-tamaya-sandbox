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
package org.apache.tamaya.validation;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationSnapshot;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.validation.spi.ConfigValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Validator manager to validate a configuration.
 */
public final class ConfigValidation {

    /** The logger used. */
    private static final Logger LOG = Logger.getLogger(ConfigValidation.class.getName());
    /** The validators used. */
    private List<org.apache.tamaya.validation.spi.ConfigValidator> validators = new ArrayList<>();

    /**
     * Private constructor.
     */
    private ConfigValidation(){}

    /**
     * Access a singleton using the default classloader.
     */
    public static ConfigValidation getInstance() {
        return ServiceContextManager.getServiceContext(
                ServiceContextManager.getDefaultClassLoader()
        ).getService(ConfigValidation.class, ConfigValidation::new);
    }

    /**
     * Access a singleton using the given target classloader.
     * @param classLoader the classloader, not null.
     */
    public static ConfigValidation getInstance(ClassLoader classLoader){
        return ServiceContextManager.getServiceContext(classLoader).getService(ConfigValidation.class, ConfigValidation::new);
    }

    /**
     * Add a validator.
     * @param validator the new validator, not null.
     */
    public void addValidator(ConfigValidator validator){
        if(!validators.contains(validator)) {
            this.validators.add(validator);
        }
    }

    /**
     * Removes a vlidator.
     * @param validator the validator, not null.
     */
    public void removeValidator(ConfigValidator validator){
        this.validators.remove(validator);
    }

    /**
     * Get the validations defined, using the default classloader.
     *
     * @return the sections defined, never null.
     * @see ServiceContextManager#getDefaultClassLoader()
     */
    public Collection<org.apache.tamaya.validation.spi.ConfigValidator> getValidators() {
        return getValidators(ServiceContextManager.getDefaultClassLoader());
    }

    /**
     * Get the validations defined.
     *
     * @param classLoader the target classloader, not null.
     * @return the sections defined, never null.
     */
    public Collection<org.apache.tamaya.validation.spi.ConfigValidator> getValidators(ClassLoader classLoader) {
        return validators;
    }

    /**
     * Validates the given configuration.
     *
     * @param config the configuration to be validated against, not null.
     * @return the validation results, never null.
     */
    public ValidationResult validate(Configuration config) {
        ConfigurationSnapshot snapshot = config.getSnapshot();
        List<ValidationCheck> result = new ArrayList<>();
        for (org.apache.tamaya.validation.spi.ConfigValidator validator : this.validators) {
            result.addAll(validator.validate(config));
        }
        return new ValidationResult(snapshot, result);
    }

    @Override
    public String toString() {
        return "ConfigValidation{" +
                "validators=" + validators +
                '}';
    }
}
