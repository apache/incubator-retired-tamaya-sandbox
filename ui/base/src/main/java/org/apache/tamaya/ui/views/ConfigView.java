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
package org.apache.tamaya.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.functions.PropertyMatcher;
import org.apache.tamaya.ui.UIConstants;
import org.apache.tamaya.ui.internal.VerticalSpacedLayout;

import javax.annotation.Priority;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * View for evaluating the current convifugration configArea.
 */
@Priority(10)
public class ConfigView extends VerticalSpacedLayout implements View {

    private TextField keyFilter = new TextField("Key filter");
    private TextField valueFilter = new TextField("Value filter");
    private TextArea configArea = new TextArea("Current Configuration");

    public ConfigView() {
        Label caption = new Label("Raw Configuration");
        Label description = new Label(
                "This view shows the overall <b>raw</b> configuration configArea. Dependening on your access rights you" +
                        "may see partial or masked data. Similarly configuration can be <i>read-only</i> or <i>mutable</i>.",
                ContentMode.HTML);
        TabSheet tabPane = new TabSheet();
        tabPane.setHeight("100%");
        tabPane.setWidth("100%");
        tabPane.addTab(createConfigTab(), "Configuration");
        tabPane.addTab(createEnvTab(), "Environment Properties");
        tabPane.addTab(createSysPropsTab(), "System Properties");
        tabPane.addTab(createRuntimeTab(), "Runtime Properties");
        addComponents(caption, description, tabPane);
        caption.addStyleName(UIConstants.LABEL_HUGE);
        description.addStyleName(UIConstants.LABEL_LARGE);
    }

    private Component createRuntimeTab() {
        VerticalLayout tabLayout = new VerticalLayout();
        TextArea runtimeProps = new TextArea();
        runtimeProps.setRows(25);
        StringBuilder b = new StringBuilder();
        b.setLength(0);
        b.append("Available Processors : ").append(Runtime.getRuntime().availableProcessors()).append('\n');
        b.append("Free Memory          : ").append(Runtime.getRuntime().freeMemory()).append('\n');
        b.append("Max Memory           : ").append(Runtime.getRuntime().maxMemory()).append('\n');
        b.append("Total Memory         : ").append(Runtime.getRuntime().totalMemory()).append('\n');
        b.append("Default Locale       : ").append(Locale.getDefault()).append('\n');
        runtimeProps.setValue(b.toString());
        runtimeProps.setReadOnly(true);
        runtimeProps.setHeight("100%");
        runtimeProps.setWidth("100%");
        tabLayout.addComponents(runtimeProps);
        tabLayout.setHeight("100%");
        tabLayout.setWidth("100%");
        return tabLayout;
    }

    private Component createSysPropsTab() {
        VerticalLayout tabLayout = new VerticalLayout();
        TextArea sysProps = new TextArea();
        sysProps.setRows(25);
        StringBuilder b = new StringBuilder();
        for(Map.Entry<Object,Object> en:new TreeMap<>(System.getProperties()).entrySet()){
            b.append(en.getKey()).append("=").append(en.getValue()).append('\n');
        }
        sysProps.setValue(b.toString());
        sysProps.setReadOnly(true);
        sysProps.setHeight("100%");
        sysProps.setWidth("100%");
        tabLayout.addComponents(sysProps);
        tabLayout.setHeight("100%");
        tabLayout.setWidth("100%");
        return tabLayout;
    }

    private Component createEnvTab() {
        VerticalLayout tabLayout = new VerticalLayout();
        TextArea envProps = new TextArea();
        StringBuilder b = new StringBuilder();
        envProps.setRows(25);
        for(Map.Entry<String,String> en:new TreeMap<>(System.getenv()).entrySet()){
            b.append(en.getKey()).append("=").append(en.getValue()).append('\n');
        }
        envProps.setValue(b.toString());
        envProps.setReadOnly(true);
        envProps.setHeight("100%");
        envProps.setWidth("100%");
        tabLayout.addComponents(envProps);
        tabLayout.setHeight("100%");
        tabLayout.setWidth("100%");
        return tabLayout;
    }

    private Component createConfigTab() {
        VerticalLayout tabLayout = new VerticalLayout();
        Component filters = createFilters();
        configArea.setWordWrap(false);
        configArea.setReadOnly(true);
        configArea.setHeight("100%");
        configArea.setWidth("100%");
        configArea.setRows(20);
        fillTree();
        tabLayout.addComponents(filters, configArea);
        tabLayout.setHeight("100%");
        tabLayout.setWidth("100%");
        return tabLayout;
    }

    private Component createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        Button filterButton = new Button("Filter", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                fillTree();
            }
        });
        filters.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        filters.addComponents(keyFilter, valueFilter, filterButton);
        filters.setSpacing(true);
        return filters;
    }

    private void fillTree() {
        final String keyFilterExp = this.keyFilter.getValue();
        final String valueFilterExp = this.valueFilter.getValue();
        Configuration config = ConfigurationProvider.getConfiguration()
                .with(ConfigurationFunctions.filter(new PropertyMatcher() {
            @Override
            public boolean test(String key, String value) {
                if(!keyFilterExp.isEmpty() && !key.matches(keyFilterExp)){
                    return false;
                }
                if(!valueFilterExp.isEmpty() && !value.matches(valueFilterExp)){
                    return false;
                }
                return true;
            }
        }));
        configArea.setValue(config.query(ConfigurationFunctions.textInfo()));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        fillTree();
    }
}