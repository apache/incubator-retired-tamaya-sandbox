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
package org.apache.tamaya.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import org.apache.tamaya.spi.ServiceContext;
import org.apache.tamaya.spi.ServiceContextManager;
import org.apache.tamaya.ui.spi.MessageProvider;
import org.apache.tamaya.ui.views.ConfigView;
import org.apache.tamaya.ui.views.ErrorView;
import org.apache.tamaya.ui.views.HomeView;
import org.apache.tamaya.ui.views.SystemView;


/**
 * UI main layout.
 */
public class ApplicationLayout extends HorizontalLayout {

    private NavBar navBar;
    private Panel content;
    private Navigator navigator;

    public ApplicationLayout(UI ui) {
        addStyleName(UIConstants.MAIN_LAYOUT);
        setSizeFull();
        initLayouts();
        setupNavigator(ui);
    }

    public Navigator getNavigator(){
        return navigator;
    }

    private void initLayouts() {
        navBar = new NavBar(this);
        // Use panel as main content container to allow it's content to scroll
        content = new Panel();
        content.setSizeFull();
        content.addStyleName(UIConstants.PANEL_BORDERLESS);

        addComponents(navBar, content);
        setExpandRatio(content, 1);
    }

    public void addView(String uri, Class<? extends View> viewClass, String viewName){
        navigator.addView(uri, viewClass);
        navBar.addViewButton(uri, viewName);
    }


    private void setupNavigator(UI ui) {
        navigator = new Navigator(ui, content);
        navigator.setErrorView(ErrorView.class);
        // Add view change listeners so we can do things like select the correct menu item and update the page title
        navigator.addViewChangeListener(navBar);
//        navigator.addViewChangeListener(new PageTitleUpdater());
        addView("/home", HomeView.class, getViewName(HomeView.class));
        addView("/system", SystemView.class, getViewName(SystemView.class));
        addView("/config", ConfigView.class, getViewName(ConfigView.class));
        navigator.navigateTo("/home");
    }

    private String getViewName(Class<? extends View> viewClass) {
        MessageProvider prov = ServiceContextManager.getServiceContext()
                .getService(MessageProvider.class);
        String msg = null;
        if(prov!=null){
            msg = prov.getMessage("views."+viewClass.getSimpleName()+".name");
        }
        return msg;
    }


}