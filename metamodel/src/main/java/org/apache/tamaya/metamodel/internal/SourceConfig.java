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


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolver to resolve/map DSL related source expressions into PropertySources
 * loadable by a ConfigurationContext. Hereby the ordering of loaded property sources must be
 * honored if possible by implicitly adapting/Overriding the default ordinal for the sources
 * added.
 */
public class SourceConfig {

    private boolean enabled;
    private String type;
    private String name;
    private Integer ordinal;
    private long refreshInterval;
    private Map<String,String> sourceConfiguration = new HashMap<>();

    private SourceConfig(Builder builder) {
        enabled = builder.enabled;
        type = builder.type;
        name = builder.name;
        refreshInterval = builder.refreshInterval;
        ordinal = builder.ordinal;
        sourceConfiguration = builder.sourceConfiguration;
    }

    /**
     * New builder builder.
     *
     * @param type the type
     * @return the builder
     */
    public static Builder newBuilder(String type) {
        return new Builder(type);
    }

    /**
     * New builder builder using this instance's settings.
     *
     * @return the builder
     */
    public Builder toBuilder() {
        Builder builder = new Builder(this.type);
        builder.enabled = this.enabled;
        builder.type = this.type;
        builder.ordinal = this.ordinal;
        builder.name = this.name;
        builder.sourceConfiguration = this.sourceConfiguration;
        return builder;
    }

    /**
     * Is enabled boolean.
     *
     * @return the boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets source configuration.
     *
     * @return the source configuration
     */
    public Map<String, String> getSourceConfiguration() {
        return sourceConfiguration;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the current refresh interval, default is 0 meaning the property
     * source is never refreshed.
     *
     * @return the refresh interval
     */
    public long getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PropertySourceConfig{" +
                "enabled=" + enabled +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", ordinal=" + ordinal +
                ", sourceConfiguration=" + sourceConfiguration +
                '}';
    }


    public <T> T create(Class<T> type)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        return (T)Class.forName(this.getType()).newInstance();
    }


    /**
     * {@code PropertySourceConfig} builder static inner class.
     */
    public static final class Builder {
        private boolean enabled;
        private String type;
        private String name;
        private Integer ordinal;
        private long refreshInterval;
        private Map<String, String> sourceConfiguration;

        private Builder(String type) {
            this.type = Objects.requireNonNull(type);
            if(type.trim().isEmpty()){
                throw new IllegalArgumentException("Type is empty.");
            }
        }

        /**
         * Sets the {@code refreshInterval} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code refreshInterval} to set
         * @return a reference to this Builder
         */
        public Builder withRefreshInterval(long val) {
            refreshInterval = val;
            return this;
        }

        /**
         * Sets the {@code ordinal} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code ordinal} to set
         * @return a reference to this Builder
         */
        public Builder withOrdinal(Integer val) {
            ordinal = val;
            return this;
        }

        /**
         * Sets the {@code enabled} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code enabled} to set
         * @return a reference to this Builder
         */
        public Builder withEnabled(boolean val) {
            enabled = val;
            return this;
        }

        /**
         * Sets the {@code type} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code type} to set
         * @return a reference to this Builder
         */
        public Builder withType(String val) {
            type = val;
            return this;
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder withName(String val) {
            name = val;
            return this;
        }

        /**
         * Sets the {@code sourceConfiguration} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param val the {@code sourceConfiguration} to set
         * @return a reference to this Builder
         */
        public Builder withSourceConfiguration(Map<String, String> val) {
            sourceConfiguration = val;
            return this;
        }

        /**
         * Returns a {@code PropertySourceConfig} built from the parameters previously set.
         *
         * @return a {@code PropertySourceConfig} built with parameters of this {@code PropertySourceConfig.Builder}
         */
        public SourceConfig build() {
            return new SourceConfig(this);
        }
    }
}
