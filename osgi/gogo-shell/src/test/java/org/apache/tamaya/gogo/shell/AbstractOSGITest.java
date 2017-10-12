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
package org.apache.tamaya.gogo.shell;

import org.apache.tamaya.osgi.TamayaConfigPlugin;
import org.apache.tamaya.osgi.commands.TamayaConfigService;
import org.junit.Before;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by atsticks on 27.09.17.
 */
public abstract class AbstractOSGITest {

    private Map<String,Hashtable<String, Object>> properties = new ConcurrentHashMap<>();

    @Mock
    protected BundleContext bundleContext;

    @Mock
    protected ConfigurationAdmin cm;

    @Mock
    private ServiceReference<ConfigurationAdmin> cmRef;
    @Mock
    private ServiceReference<TamayaConfigService> tamayaRef;

    protected TamayaConfigService tamayaConfigPlugin;

    protected Dictionary<String,Object> getProperties(String pid){
        return this.properties.get(pid);
    }

    @Before
    public void setup()throws Exception{
        doAnswer(invocation -> {
            return initConfigurationMock((String)invocation.getArguments()[0]);
        }).when(cm).getConfiguration(any());
        doAnswer(invocation -> {
            return initConfigurationMock((String)invocation.getArguments()[0]);
        }).when(cm).getConfiguration(any(), any());
        doReturn(new Bundle[0]).when(bundleContext).getBundles();
        doReturn(cmRef).when(bundleContext).getServiceReference(ConfigurationAdmin.class);
        doReturn(cm).when(bundleContext).getService(cmRef);
        doReturn(tamayaRef).when(bundleContext).getServiceReference(TamayaConfigService.class);
        tamayaConfigPlugin = new TamayaConfigPlugin(bundleContext);
        doReturn(tamayaConfigPlugin).when(bundleContext).getService(tamayaRef);
    }

    protected Configuration initConfigurationMock(final String pid)throws Exception{
        Configuration config = mock(Configuration.class);
        doAnswer(invocation -> {
            Hashtable<String,Object> props = properties.get(pid);
            props.clear();
            props.putAll((Map<? extends String, ?>) invocation.getArguments()[0]);
            return null;
        }).when(config).update(any(Dictionary.class));
        doAnswer(invocation -> {
            Hashtable<String,Object> props = properties.get(pid);
            if(props==null){
                props = new Hashtable<>();
                properties.put(pid, props);
                for(Map.Entry en:System.getProperties().entrySet()){
                    props.put(en.getKey().toString(), en.getValue());
                }
            }
            return new Hashtable<>(props);
        }).when(config).getProperties();
        return config;
    }
}
