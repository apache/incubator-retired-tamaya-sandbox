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

import org.apache.tamaya.ConfigurationSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Result of a validation performed on a configuration.
 */
public final class ValidationResult {
    private ConfigurationSnapshot snapshot;
    private List<ValidationCheck> result;

    /**
     * Creates a new validation result.
     * @param snapshot the snapshot config, not null.
     * @param result the result, not null.
     */
    public ValidationResult(ConfigurationSnapshot snapshot, List<ValidationCheck> result) {
        this.snapshot = Objects.requireNonNull(snapshot);
        this.result = Objects.requireNonNull(result);
    }

    /**
     * Access the validated snapshot.
     * @return the snapshot, not null.
     */
    public ConfigurationSnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Access the validation findings.
     * @return the findings, not null.
     */
    public List<ValidationCheck> getResult() {
        return result;
    }

    /**
     * Checks if the validation does not include errors.
     * @return true if no errors were identified.
     */
    public boolean isSuccessfull(){
        for(ValidationCheck check:this.result){
            if(check.getResult().isError()){
                return false;
            }
        }
        return true;
    }

    /**
     * Get the the findings filtered by Finding types given.
     * @param findingTypes the findingTypes.
     * @return the filterered list.
     */
    public List<ValidationCheck> getResultByFindings(ValidationCheck.Finding... findingTypes){
        List<ValidationCheck.Finding> findings = Arrays.asList(findingTypes);
        if(findings.isEmpty()){
            return result;
        }else{
            return result.stream()
                    .filter(f -> findings.contains(f.getResult()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "snapshot=" + snapshot +
                ", result=" + result +
                '}';
    }
}
