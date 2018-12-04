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
package org.apache.tamaya.validation.spi;

import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.validation.ValidationCheck;

import java.util.List;
import java.util.Objects;

/**
 * Default configuration Model for a configuration parameter.
 */
public class RegexPropertyValidator extends AbstractValidator {

    /** Regular expression for validating the value. */
    private final String regEx;

    private boolean warningOnly;

    private boolean validateAll;

    /**
     * Creates a new validator.
     * @param regEx the regular expression to check.
     * @param validateAll true, if all property values provided should be validated.
     * @param warningOnly if activated only warning will be generated.
     * @param key the property key
     * @param description a description of the check.
     */
    public RegexPropertyValidator(String key, String regEx, boolean validateAll, boolean warningOnly, String description) {
        super(key, false, description);
        this.regEx = Objects.requireNonNull(regEx);
        this.validateAll = validateAll;
        this.warningOnly = warningOnly;
    }

    /**
     * Creates a new validator.
     * @param regEx the regular expression to check.
     * @param key the property key
     * @param description a description of the check.
     */
    public RegexPropertyValidator(String key, String regEx, String description) {
        super(key, false, description);
        this.regEx = Objects.requireNonNull(regEx);
    }


    @Override
    protected void validateValues(List<PropertyValue> configValues, List<ValidationCheck> result) {
        if (configValues.isEmpty() && isRequired()) {
            result.add(ValidationCheck.createMissing(this, "Missing key: " + getKey()));
        }
        String baseText = getDescription();
        if(baseText==null){
            baseText = "Validation failure for property '" + getKey() + "': ";
        }else{
            baseText = baseText + " Validation failure: ";
        }
        if (regEx != null) {
            for(PropertyValue value:configValues) {
                if (!value.getValue().matches(regEx)) {
                    if(warningOnly){
                        result.add(ValidationCheck.createWarning(this, baseText + " Value not matching expression: " + regEx + ", was " +
                                configValues.get(0).getValue()));
                    }else {
                        result.add(ValidationCheck.createError(this, baseText + " Value not matching expression: " + regEx + ", was " +
                                configValues.get(0).getValue()));
                    }
                }
                if(!validateAll){
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName()).append("{\n");
        b.append("  key       : ").append(getKey()).append('\n');
        b.append("  required  : ").append(isRequired()).append('\n');
        b.append("  expression: ").append(regEx).append("\n}\n");
        return b.toString();
    }

}
