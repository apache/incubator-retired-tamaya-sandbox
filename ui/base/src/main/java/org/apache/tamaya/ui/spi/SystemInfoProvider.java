package org.apache.tamaya.ui.spi;/*
 * (C) Copyright 2015-2017 Trivadis AG. All rights reserved.
 */

import com.vaadin.ui.Accordion;

/**
 * Created by atsticks on 28.03.17.
 */
public interface SystemInfoProvider {

    void addSystemInfo(Accordion systemInfoPanel);

    void updateSystemInfo(Accordion systemInfoPanel);
}
