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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.tamaya.metamodel.internal.resolver.JavaResolver;
import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.PropertySourceComparator;


/**
 * Wrapped property source that allows enabling a property source using an
 * {@code enabled} expression.
 */
public final class EnabledPropertySource
        implements PropertySource, Enabled {

    private static final Logger LOG = Logger.getLogger(EnabledPropertySource.class.getName());
    private String enabledExpression;
    private PropertySource wrapped;
    private boolean enabled;
    private static final JavaResolver resolver = new JavaResolver();

    public EnabledPropertySource(PropertySource wrapped, Map<String,String> context, String expression) {
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
     * @return the enabled createValue.
     */
    @Override
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * Enables/disables this property source.
     * @param enabled the enabled createValue.
     */
    @Override
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    @Override
    public int getOrdinal() {
        return PropertySourceComparator.getOrdinal(this.wrapped);
    }

    @Override
    public String getName() {
        return this.wrapped.getName();
    }

    @Override
    public PropertyValue get(String key) {
        if(!isEnabled()){
            return null;
        }
        return this.wrapped.get(key);
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        if(!isEnabled()){
            return Collections.emptyMap();
        }
        return this.wrapped.getProperties();
    }

    @Override
    public boolean isScannable() {
        return this.wrapped.isScannable();
    }

    @Override
    public String toString() {
        return "EnabledPropertySource{" +
                "\n enabled=" + enabledExpression +
                "\n wrapped=" + wrapped +
                '}';
    }
}
