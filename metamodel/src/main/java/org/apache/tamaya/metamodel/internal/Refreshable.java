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
package org.apache.tamaya.metamodel.internal;

import org.apache.tamaya.spi.ConfigurationContext;

/**
 * Common interface for refreshable items.
 */
public interface Refreshable {

    /**
     * Refreshes the given configuration context, by applying any changes
     * needed to reflect the change.
     * @param context the configuration context, not null.
     * @return the new configuration context. In case no changes are
     *         needed or the changes could be applied implicitly, the
     *         instance passed as input should be returned.
     */
    ConfigurationContext refresh(ConfigurationContext context);
}
