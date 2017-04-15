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

import com.vaadin.ui.Accordion;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.ui.UIConstants;
import org.apache.tamaya.ui.spi.SystemInfoProvider;

import javax.annotation.Priority;

/**
 * Created by atsticks on 29.06.16.
 */
public abstract class AbstractTextInfoProvider implements SystemInfoProvider {

    private TextArea textArea;

    protected abstract String getCaption();

    protected abstract String getInfo();

    @Override
    public void addSystemInfo(Accordion systemInfoPanel) {
        VerticalLayout layout = new VerticalLayout();
        textArea = new TextArea("System Info");
        textArea.addStyleName(UIConstants.FIXED_FONT);
        textArea.setValue(getInfo());
        textArea.setRows(20);
        textArea.setHeight("400px");
        textArea.setWidth("100%");
        layout.addComponents(textArea);
        systemInfoPanel.addTab(layout, getCaption());
    }

    @Override
    public void updateSystemInfo(Accordion systemInfoPanel) {
        if(textArea!=null){
            textArea.setValue(getInfo());
        }else{
            textArea = new TextArea("System Info");
            textArea.setValue(getInfo());
            textArea.setSizeFull();
            systemInfoPanel.addTab(textArea, textArea.getCaption());
        }
    }

}
