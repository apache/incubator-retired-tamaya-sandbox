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
package org.apache.tamaya.jsr382;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.spisupport.DefaultConfigurationSnapshot;

import javax.config.ConfigAccessor;
import javax.config.ConfigSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a {@link ConfigSnapshot} based on Tamaya resources.
 */
class TamayaConfigSnapshot  implements ConfigSnapshot {

    private DefaultConfigurationSnapshot snapshot;
    private List<ConfigAccessor<?>> accessors = new ArrayList<>();

    public TamayaConfigSnapshot(Configuration config, ConfigAccessor<?>... configAccessors){
        Set<String> keys = new HashSet<>();
        for(ConfigAccessor<?> accessor:configAccessors){
            keys.addAll(((TamayaConfigAccessor)accessor).getCandidateKeys());
        }
        this.snapshot = new DefaultConfigurationSnapshot(config, keys);
    }

    public Configuration getConfiguration(){
        return snapshot;
    }

    @Override
    public String toString() {
        return "TamayaConfigSnapshot{" +
                "snapshot=" + snapshot +
                '}';
    }
}
