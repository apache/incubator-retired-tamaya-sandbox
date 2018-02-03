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
package org.apache.tamaya.metamodel;

import org.apache.tamaya.base.configsource.ConfigSourceComparator;
import org.apache.tamaya.metamodel.internal.resolver.JavaResolver;

import javax.config.spi.ConfigSource;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;


/**
 * Wrapped property source that allows enabling a property source using an
 * {@code enabled} expression.
 */
public final class EnabledConfigSource
        implements ConfigSource, Enabled {

    private static final Logger LOG = Logger.getLogger(EnabledConfigSource.class.getName());
    private String enabledExpression;
    private ConfigSource wrapped;
    private boolean enabled;
    private static final JavaResolver resolver = new JavaResolver();

    public EnabledConfigSource(ConfigSource wrapped, Map<String,String> context, String expression) {
        this.enabledExpression = Objects.requireNonNull(expression);
        this.wrapped = Objects.requireNonNull(wrapped);
        this.enabled = calculateEnabled(context);
    }

    protected boolean calculateEnabled(Map<String, String> context) {
        try {
            return Boolean.TRUE.equals(resolver.evaluate(enabledExpression, context));
        } catch (Exception e) {
            LOG.severe("Invalid Boolean expression: '"
                    +enabledExpression+"': " + e + ", property source will be disabled: " +
                    wrapped.getName());
        }
        return false;
    }

    /**
     * Returns the enabled property.
     * @return the enabled value.
     */
    @Override
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * Enables/disables this property source.
     * @param enabled the enabled value.
     */
    @Override
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    @Override
    public int getOrdinal() {
        return ConfigSourceComparator.getOrdinal(this.wrapped);
    }


    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    @Override
    public String getValue(String key) {
        if(!isEnabled()){
            return null;
        }
        return this.wrapped.getValue(key);
    }

    @Override
    public Map<String, String> getProperties() {
        if(!isEnabled()){
            return Collections.emptyMap();
        }
        return this.wrapped.getProperties();
    }

    @Override
    public String toString() {
        return "DynamicPropertySource{" +
                "\n enabled=" + enabledExpression +
                "\n wrapped=" + wrapped +
                '}';
    }
}
