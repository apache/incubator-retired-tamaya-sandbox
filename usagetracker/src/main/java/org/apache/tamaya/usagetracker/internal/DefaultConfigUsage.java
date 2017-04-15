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

import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.usagetracker.UsageStat;
import org.apache.tamaya.usagetracker.spi.ConfigUsageSpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the module's SPI.
 */
public class DefaultConfigUsage implements ConfigUsageSpi {

    private Set<String> ignoredPackages = new HashSet<>();

    private Map<String, UsageStat> stats = new ConcurrentHashMap<>();

    /** By default usage tracking is not enabled. */
    private boolean usageTrackingEnabled = initEnabled();

    /**
     * Method that checks the 'tamaya.usage-report' system property for
     * enabling tamaya usage reporting initially.
     * @return {@code true} iff property is set to {@code true}
     */
    private boolean initEnabled() {
        String val = System.getProperty("tamaya.usage-report");
        if(Boolean.parseBoolean(val)){
            return true;
        }
        return false;
    }

    public DefaultConfigUsage(){
        ignoredPackages.add("com.intellij");
        ignoredPackages.add("java");
        ignoredPackages.add("org.junit");
        ignoredPackages.add("junit");
        ignoredPackages.add("javax");
        ignoredPackages.add("sun");
        ignoredPackages.add("oracle");
        ignoredPackages.add("com.sun");
        ignoredPackages.add("com.oracle");
        ignoredPackages.add("org.apache.tamaya");
    }

    @Override
    public void enableUsageTracking(boolean enabled){
        this.usageTrackingEnabled = enabled;
    }

    @Override
    public boolean isTrackingEnabled(){
        return usageTrackingEnabled;
    }

    @Override
    public Set<String> getIgnoredPackages() {
        return Collections.unmodifiableSet(ignoredPackages);
    }

    @Override
    public void addIgnoredPackages(String... packageName) {

    }

    @Override
    public UsageStat getSinglePropertyStats(String key) {
        return this.stats.get(key);
    }

    @Override
    public UsageStat getAllPropertiesStats() {
        return this.stats.get("<<all>>");
    }

    /**
     * Get the recorded usage references of configuration.
     * @return the recorded usge references, never null.
     */
    @Override
    public Collection<UsageStat> getUsageStats() {
        return stats.values();
    }

    @Override
    public void recordAllPropertiesAccess(ConfigurationContext context){
        recordSingleKeyAccess(PropertyValue.of("<<all>>","<not stored>","-"), context);
    }

    @Override
    public void recordSingleKeyAccess(PropertyValue value, ConfigurationContext context){
        // Ignore meta-entries
        if(!isTrackingEnabled()){
            return;
        }
        UsageStat usage = this.stats.get(value.getKey());
        if(usage==null){
            usage = new UsageStat(value.getKey());
            this.stats.put(value.getKey(), usage);
        }
        usage.trackUsage(value);
    }


    /**
     * Access the usage statistics for the recorded uses of configuration.
     */
    @Override
    public String getInfo(){
        StringBuilder b = new StringBuilder();
        b.append("Apache Tamaya Configuration Usage Metrics\n");
        b.append("=========================================\n");
        b.append("DATE: ").append(new Date()).append("\n\n");
        List<UsageStat> usages = new ArrayList<>(getUsageStats());
        Collections.sort(usages, new Comparator<UsageStat>() {
            @Override
            public int compare(UsageStat k1, UsageStat k2) {
                return k2.getUsageCount() - k1.getUsageCount();
            }
        });
        for(UsageStat usage:usages){
            String usageCount = String.valueOf(usage.getUsageCount());
            b.append(usageCount);
            b.append("       ".substring(0, 7-usageCount.length()));
            b.append(usage.getKey()).append(":\n");
            for(UsageStat.AccessStats details: usage.getAccessDetails()) {
                String accessCount = String.valueOf(details.getAccessCount());
                    b.append("  - ").append(accessCount);
                    b.append("      ".substring(0, 6-usageCount.length()));
                    b.append(details.getAccessPoint());
                    int endIndex = 50-details.getAccessPoint().length();
                    if(endIndex<0){
                        endIndex = 0;
                    }
                    b.append("                                                  ".substring(0, endIndex));
                    b.append(",");b.append(" first=").append(new Date(details.getFirstAccessTS()))
                            .append(",");b.append(" last=").append(new Date(details.getLastAccessTS()))
                            .append('\n');
            }
        }
        return b.toString();
    }

    @Override
    public void clearStats() {
        this.stats.clear();
    }

}
