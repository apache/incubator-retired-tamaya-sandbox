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

import org.apache.tamaya.doc.annot.ConfigAreaSpec;
import org.apache.tamaya.doc.annot.ConfigPropertySpec;

import java.util.Objects;

/**
 * Models a partial configuration validation result.
 */
public final class ValidationCheck {

    /**
     * Enum type describing the different validation results supported.
     */
    public enum Finding {
        /**
         * The validated item is valid
         */
        VALID,
        /**
         * The validated item is deprecated.
         */
        DEPRECATED,
        /**
         * The validated item is correct, but the createValue is worth a warning.
         */
        WARNING,
        /**
         * A required parameter or section is missing.
         */
        MISSING,
        /**
         * The validated item has an invalid createValue.
         */
        ERROR;

        /**
         * Method to quickly evaluate if the current state is an error state.
         *
         * @return true, if the state is not ERROR or MISSING.
         */
        boolean isError() {
            return this.ordinal() == MISSING.ordinal() || this.ordinal() == ERROR.ordinal();
        }
    }

    /**
     * The finding level.
     */
    private final Finding result;

    /**
     * The message.
     */
    private final String message;

    /**
     * The validation source.
     */
    private final Object source;


    /**
     * Creates a new ValidationResult.
     *
     * @param source the source item, not null.
     * @param message the message, not null.
     * @return a new validation check containing valid parts createObject the given model.
     */
    public static ValidationCheck createValid(Object source, String message) {
        return new ValidationCheck(source, Finding.VALID, null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param source the validated source, not null.
     * @param message the message, not null.
     * @return a new validation check containing missing parts createObject the given model.
     */
    public static ValidationCheck createMissing(Object source, String message) {
        return new ValidationCheck(source, Finding.MISSING, message);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param source the validated source, not null.
     * @param message error message, not null.
     * @return a new validation check containing erroneous parts createObject the given model with the given error message.
     */
    public static ValidationCheck createError(Object source, String message) {
        return new ValidationCheck(source, Finding.ERROR, message);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param source the validated source, not null.
     * @param message warning message to, not null.
     * @return a new validation check containing warning parts createObject the given model with the given warning message.
     */
    public static ValidationCheck createWarning(Object source, String message) {
        return new ValidationCheck(source, Finding.WARNING, message);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param source the validated source, not null.
     * @param message allows the message, not null.
     * @return a new validation check containing deprecated parts createObject the given model with an optional message.
     */
    public static ValidationCheck createDeprecated(Object source, String message) {
        return new ValidationCheck(source, Finding.DEPRECATED, message);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param source the validated source, not null.
     * @return a new validation result containing deprecated parts createObject the given model.
     */
    public static ValidationCheck createDeprecated(Object source) {
        return new ValidationCheck(source, Finding.DEPRECATED, "Deprecated: " + source);
    }


    /**
     * Constructor.
     *
     * @param source the validated specification, not null.
     * @param result     the configModel result, not null.
     * @param message    the detail message.
     */
    private ValidationCheck(Object source, Finding result, String message) {
        this.message = Objects.requireNonNull(message);
        this.source = Objects.requireNonNull(source);
        this.result = Objects.requireNonNull(result);
    }

    /**
     * Get the validation result.
     *
     * @return the result, never null.
     */
    public Finding getResult() {
        return result;
    }

    /**
     * Get the validation message.
     *
     * @return the detail message, or null.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the checks source.
     * @return the source, not null.
     */
    public Object getSource(){
        return source;
    }

    @Override
    public String toString() {
        String finalMessage = "";
        if (message != null) {
            finalMessage = " -> " + message;
        }
        if(source instanceof ConfigPropertySpec){
            ConfigPropertySpec pspec = (ConfigPropertySpec) source;
            return result + ": " + pspec.name() + " (property)"+finalMessage + '\n';
        }
        else if(source instanceof ConfigAreaSpec){
            ConfigAreaSpec gspec = (ConfigAreaSpec) source;
            return result + ": " + gspec.path() + " (group)"+finalMessage + '\n';
        }
        return result + ": " + source + ")"+finalMessage + '\n';
    }
}
