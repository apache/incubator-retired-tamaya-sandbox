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
package org.apache.tamaya.osgi.commands;

import org.apache.tamaya.osgi.ConfigHistory;
import org.apache.tamaya.osgi.Policy;
import org.osgi.framework.Bundle;

import java.util.Dictionary;
import java.util.List;
import java.util.Set;

/**
 * Service exposing the Tamaya OSGI configuration logic.
 */
public interface TamayaConfigService{
    /** The system/config property to set Tamaya's {@link Policy}. */
    String TAMAYA_POLICY_PROP = "tamaya-policy";
    /** The MANIFEST property to set Tamaya's {@link Policy}. */
    String TAMAYA_POLICY_MANIFEST = "Tamaya-Policy";
    /** The system/config property to define a customized Tamaya's configuration root, replacing the {@code [PID]} default
     * prefix used. */
    String TAMAYA_CUSTOM_ROOT_PROP = "tamaya-config-root";
    /** The MANIFEST property to define a customized Tamaya's configuration root, replacing the {@code [PID]} default
     * prefix used. */
    String TAMAYA_CUSTOM_ROOT_MANIFEST = "Tamaya-Config-Root";
    /** The system/config property to enable Tamaya. */
    String TAMAYA_ENABLED_PROP = "tamaya-enabled";
    /** The MANIFEST property to enable Tamaya. */
    String TAMAYA_ENABLED_MANIFEST = "Tamaya-Enabled";
    /** The system/config property to enable Tamaya automatic updates (requires Tamaya's Updater plugin to be loaded as well). */
    String TAMAYA_AUTO_UPDATE_ENABLED_PROP = "tamaya-update-enabled";
    /** The MANIFEST property to enable Tamaya automatic updates (requires Tamaya's Updater plugin to be loaded as well). */
    String TAMAYA_AUTO_UPDATE_ENABLED_MANIFEST = "Tamaya-Update-Enabled";

    /**
     * Enables/disables automatic updates (requires Tamaya's Updater plugin to be loaded as well).
     * @param enabled set to true to enable updates.
     */
    void setAutoUpdateEnabled(boolean enabled);

    /**
     * Enables/disables Tamaya config by default.
     * @param enabled set to true to enable Tamaya for all bundles by default.
     */
    void setTamayaEnabledByDefault(boolean enabled);

    /**
     * Get the flag, if Tamaya is enabled by default for all bundles.
     * @return true if Tamaya is enabled.
     */
    boolean isTamayaEnabledByDefault();

    /**
     * Get the default policy Tamaya is using for adapting OSGI configuration.
     * @return the default policy, never null.
     */
    Policy getDefaultPolicy();

    /**
     * Set the default policy Tamaya is using for adapting OSGI configuration.
     * @param policy the policy, not null.
     */
    void setDefaultPolicy(Policy policy);

    /**
     * Updates the given OSGI configuration with Tamaya configuration.
     * @param pid the target PID, not null.
     * @return the new configuration.
     */
    Dictionary<String,Object> updateConfig(String pid);

    /**
     * Updates the given OSGI configuration with Tamaya configuration.
     * @param pid the target PID, not null.
     * @param dryRun if true, the changes will not be applied to the OSGI configuration.
     * @return the configuration that would be applied, has been applied.
     */
    Dictionary<String,Object> updateConfig(String pid, boolean dryRun);

    /**
     * Updates the given OSGI configuration with Tamaya configuration.
     * @param pid the target PID, not null.
     * @param policy the updating policy to be used, by default.
     * @param forcePolicy if set to true, the given policy will be used, even if an alternate policy is configured
     *                    for the given PID.
     * @param dryRun if true, the changes will not be applied to the OSGI configuration.
     * @return the configuration that would be applied, has been applied.
     */
    Dictionary<String,Object> updateConfig(String pid, Policy policy, boolean forcePolicy, boolean dryRun);

    /**
     * Checks if a bundle is enabled for Tamaya configuration.
     * @param bundle the bundle, not null.
     * @return true, if the bundle is enabled.
     */
    boolean isBundleEnabled(Bundle bundle);

    /**
     * Get the flag if automatic updates for config changes are enabled.
     * @return true, if automatic updates for config changes are enabled.
     */
    boolean isAutoUpdateEnabled();

    /**
     * Get the backup written for a PID.
     * @param pid the pid, not null.
     * @return the backup, or null, if no backup is present.
     */
    Dictionary<String,?> getBackup(String pid);

    /**
     * Get all current known PIDs for which backups are registered.
     * @return all known PIDs for which backups are registered.
     */
    Set<String> getBackupPids();

    /**
     * Restores a backup, replacing the current OSGI configuration with the backup and
     * disabling Tamaya for this PID.
     * @param pid the PID, not null.
     * @return true, if a backup has been restored successfully.
     */
    boolean restoreBackup(String pid);

    /**
     * Stores the current OSGI configuration as a backup (only if no backup is existing).
     * @param pid the target PID, not null.
     * @return true, if a backup has been stored successfully.
     */
    boolean createBackup(String pid);

    /**
     * Deletes a backup, if existing.
     * @param pid the target PID, not null.
     * @return true, if a backup has been restored successfully.
     */
    boolean deleteBackup(String pid);

    /**
     * Sets the maximum getHistory size.
     * @param maxHistory the max getHistory size. {@code 0} disables the getHistory function.
     */
    void setMaxHistorySize(int maxHistory);

    /**
     * Get the max getHistory size.
     * @return the max getHistory size. {@code 0} means the getHistory function is disabled.
     */
    int getMaxHistorySize();

    /**
     * Access the current (full) change getHistory.
     * @return the current getHistory, never null.
     */
    List<ConfigHistory> getHistory();

    /**
     * Clears the getHistory.
     */
    void clearHistory();

    /**
     * Clears the getHistory for a PID.
     * @param pid the target PID, not null.
     */
    void clearHistory(String pid);

    /**
     * Get the getHistory for a PID.
     * @param pid the target PID, not null.
     * @return the PID's getHistory, never null.
     */
    List<ConfigHistory> getHistory(String pid);

    /**
     * Access the current OSGI configuration for a PID.
     * @param pid the target PID, not null.
     * @param section a subsection to be filter (using startsWith).
     * @return the (optionally filtered) OSGI configuration.
     */
    Dictionary<String,Object> getOSGIConfiguration(String pid, String section);

    /**
     * Checks if a backup exists.
     * @param pid the target PID, not null.
     * @return true, if a backup exists.
     */
    boolean containsBackup(String pid);
}