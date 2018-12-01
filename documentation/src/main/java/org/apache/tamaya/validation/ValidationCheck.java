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

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Models a partial configuration configModel result.
 */
public final class ValidationCheck {
    /**
     * The configModel result.
     */
    private final Finding result;
    /**
     * The configModel message.
     */
    private final String message;

    private final Annotation spec;

    /**
     * Get the checks annotation.
     * @return the annotation.
     */
    public Class getSpecType(){
        return spec.getClass();
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the specification item, not null.
     * @return a new validation result containing valid parts createObject the given model.
     */
    public static ValidationCheck createValid(Annotation spec) {
        return new ValidationCheck(spec, Finding.VALID, null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the validated specification, not null.
     * @return a new validation result containing missing parts createObject the given model.
     */
    public static ValidationCheck createMissing(Annotation spec) {
        return new ValidationCheck(spec, Finding.MISSING, null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the validated specification, not null.
     * @param message Additional message to be shown (optional).
     * @return a new validation result containing missing parts createObject the given model with a message.
     */
    public static ValidationCheck createMissing(Annotation spec, String message) {
        return new ValidationCheck(spec, Finding.MISSING, message);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the validated specification, not null.
     * @param error error message to addNode.
     * @return a new validation result containing erroneous parts createObject the given model with the given error message.
     */
    public static ValidationCheck createError(Annotation spec, String error) {
        return new ValidationCheck(spec, Finding.ERROR, error);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the validated specification, not null.
     * @param warning warning message to addNode.
     * @return a new validation result containing warning parts createObject the given model with the given warning message.
     */
    public static ValidationCheck createWarning(Annotation spec, String warning) {
        return new ValidationCheck(spec, Finding.WARNING, warning);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the validated specification, not null.
     * @param alternativeUsage allows setting a message to indicate non-deprecated replacement, maybe null.
     * @return a new validation result containing deprecated parts createObject the given model with an optional message.
     */
    public static ValidationCheck createDeprecated(Annotation spec, String alternativeUsage) {
        return new ValidationCheck(spec, Finding.DEPRECATED, alternativeUsage != null ? "Use instead: " + alternativeUsage : null);
    }

    /**
     * Creates a new ValidationResult.
     *
     * @param spec the validated specification, not null.
     * @return a new validation result containing deprecated parts createObject the given model.
     */
    public static ValidationCheck createDeprecated(Annotation spec) {
        return new ValidationCheck(spec, Finding.DEPRECATED, null);
    }


    /**
     * Constructor.
     *
     * @param spec the validated specification, not null.
     * @param result     the configModel result, not null.
     * @param message    the detail message.
     * @return new validation result.
     */
    public static ValidationCheck create(Annotation spec, Finding result, String message) {
        return new ValidationCheck(spec, result, message);
    }


    /**
     * Constructor.
     *
     * @param spec the validated specification, not null.
     * @param result     the configModel result, not null.
     * @param message    the detail message.
     */
    private ValidationCheck(Annotation spec, Finding result, String message) {
        this.message = message;
        this.spec = Objects.requireNonNull(spec);
        this.result = Objects.requireNonNull(result);
    }

    /**
     * Get the configModel section.
     *
     * @return the section, never null.
     */
    public Annotation getSpec() {
        return spec;
    }

    /**
     * Get the configModel result.
     *
     * @return the result, never null.
     */
    public Finding getResult() {
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
        String finalMessage = "";
        if (message != null) {
            finalMessage = " -> " + message;
        }
        if(spec instanceof ConfigPropertySpec){
            ConfigPropertySpec pspec = (ConfigPropertySpec)spec;
            return result + ": " + pspec.name() + " (property)"+finalMessage + '\n';
        }
        else if(spec instanceof ConfigAreaSpec){
            ConfigAreaSpec gspec = (ConfigAreaSpec)spec;
            return result + ": " + gspec.path() + " (group)"+finalMessage + '\n';
        }
        return result + ": " + spec + ")"+finalMessage + '\n';
    }
}
