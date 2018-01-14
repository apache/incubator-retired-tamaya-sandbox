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

import org.apache.tamaya.validation.spi.AbstractConfigModel;

import javax.config.Config;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Models a partial configuration configModel result.
 */
public final class Validation {
    /**
     * the config section.
     */
    private final ValidationModel configModel;
    /**
     * The configModel result.
     */
    private final ValidationType result;
    /**
     * The configModel message.
     */
    private final String message;

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @return a new validation result containing valid parts of the given model.
     */
    public static Validation checkValid(ValidationModel configModel) {
        return new Validation(configModel, ValidationType.VALID, null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @return a new validation result containing missing parts of the given model.
     */
    public static Validation checkMissing(ValidationModel configModel) {
        return new Validation(configModel, ValidationType.MISSING, null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @param message Additional message to be shown (optional).
     * @return a new validation result containing missing parts of the given model with a message.
     */
    public static Validation checkMissing(ValidationModel configModel, String message) {
        return new Validation(configModel, ValidationType.MISSING, message);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @param error error message to add.
     * @return a new validation result containing erroneous parts of the given model with the given error message.
     */
    public static Validation checkError(ValidationModel configModel, String error) {
        return new Validation(configModel, ValidationType.ERROR, error);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @param warning warning message to add.
     * @return a new validation result containing warning parts of the given model with the given warning message.
     */
    public static Validation checkWarning(ValidationModel configModel, String warning) {
        return new Validation(configModel, ValidationType.WARNING, warning);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @param alternativeUsage allows setting a message to indicate non-deprecated replacement, maybe null.
     * @return a new validation result containing deprecated parts of the given model with an optional message.
     */
    public static Validation checkDeprecation(ValidationModel configModel, String alternativeUsage) {
        return new Validation(configModel, ValidationType.DEPRECATED, alternativeUsage != null ? "Use instead: " + alternativeUsage : null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param configModel the configModel item, not null.
     * @return a new validation result containing deprecated parts of the given model.
     */
    public static Validation checkDeprecation(ValidationModel configModel) {
        return new Validation(configModel, ValidationType.DEPRECATED, null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param owner owner
     * @param key the name/model key
     * @param type model type 
     * @return a corresponding configModel item
     */
    public static Validation checkUndefined(final String owner, final String key, final ValidationTarget type) {
        return new Validation(new AbstractConfigModel(owner, key, false, "Undefined key: " + key) {

            @Override
            public ValidationTarget getType() {
                return type;
            }

            @Override
            public Collection<Validation> validate(Config config) {
                return Collections.emptyList();
            }
        }, ValidationType.UNDEFINED, null);
    }


    /**
     * Constructor.
     *
     * @param configModel the configModel item, not null.
     * @param result     the configModel result, not null.
     * @param message    the detail message.
     * @return new validation result.
     */
    public static Validation of(ValidationModel configModel, ValidationType result, String message) {
        return new Validation(configModel, result, message);
    }


    /**
     * Constructor.
     *
     * @param configModel the configModel item, not null.
     * @param result     the configModel result, not null.
     * @param message    the detail message.
     */
    private Validation(ValidationModel configModel, ValidationType result, String message) {
        this.message = message;
        this.configModel = Objects.requireNonNull(configModel);
        this.result = Objects.requireNonNull(result);
    }

    /**
     * Get the configModel section.
     *
     * @return the section, never null.
     */
    public ValidationModel getConfigModel() {
        return configModel;
    }

    /**
     * Get the configModel result.
     *
     * @return the result, never null.
     */
    public ValidationType getResult() {
        return result;
    }

    /**
     * Get the detail message.
     *
     * @return the detail message, or null.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (message != null) {
            return result + ": " + configModel.getName() + " (" + configModel.getType() + ") -> " + message + '\n';
        }
        return result + ": " + configModel.getName() + " (" + configModel.getType() + ")";
    }
}
