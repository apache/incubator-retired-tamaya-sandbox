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
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.ui.UIConstants;
import org.apache.tamaya.ui.internal.VerticalSpacedLayout;
import org.apache.tamaya.ui.spi.MessageProvider;
import org.apache.tamaya.ui.spi.SystemInfoProvider;

import javax.annotation.Priority;

/**
 * View showing the current loaded system components.
 */
@Priority(10000)
public class SystemView extends VerticalSpacedLayout implements View {

    private Accordion configTree = new Accordion();


    public SystemView() {
        Label caption = new Label("Tamaya Runtime");
        Label description = new Label(
                "This view shows the system components currently active. This information may be useful when checking if an" +
                        "configuration extension is loaded and for inspection of the configuration and property sources" +
                        "invovlved.",
                ContentMode.TEXT);
        configTree.setHeight("100%");
        configTree.setWidth("100%");
        fillAccordion();

        addComponents(caption, description, configTree);
        caption.addStyleName(UIConstants.LABEL_HUGE);
    }

    private void fillAccordion() {
        configTree.removeAllComponents();
        for(SystemInfoProvider infoProvider:ServiceContextManager.getServiceContext()
                .getServices(SystemInfoProvider.class)){
            infoProvider.addSystemInfo(configTree);
        }
    }

    private void updateAccordion() {
        for(SystemInfoProvider infoProvider:ServiceContextManager.getServiceContext()
                .getServices(SystemInfoProvider.class)){
            infoProvider.updateSystemInfo(configTree);
        }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        fillAccordion();
    }
}