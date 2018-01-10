/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.usagetracker.internal;

import org.apache.tamaya.base.filter.FilterContext;
import org.apache.tamaya.spi.Filter;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.usagetracker.spi.ConfigUsageSpi;

import javax.annotation.Priority;

/**
 * Configuration filter to be applied at the end of the filter chain. This filter
 * actually does not change the current filter value, but use the filter process
 * to track configuration usage.
 */
@Priority(Integer.MAX_VALUE)
public class UsageTrackerFilter implements Filter{

    @Override
    public String filterProperty(String key, String value) {
        ConfigUsageSpi tracker = ServiceContextManager.getServiceContext().getService(ConfigUsageSpi.class);
        FilterContext context = FilterContext.getContext();
        tracker.recordSingleKeyAccess(key, value, context==null?null:context.getConfig());
        return value;
    }
}
