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
package org.apache.tamaya.metamodel.internal.resolver;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.spi.MetaConfigurationReader;
import org.apache.tamaya.spi.ConfigurationBuilder;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.ConfigurationContextBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by atsticks on 01.05.17.
 */
public class LoggingReader implements MetaConfigurationReader{

    private static final JavaResolver resolver = new JavaResolver();

    @Override
    public void read(final Document document, ConfigurationBuilder configBuilder) {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String, Object> meta = new HashMap<>();
                meta.put("context", ConfigurationProvider.getConfiguration().getContext());
                meta.put("config", ConfigurationProvider.getConfiguration());
                NodeList nodeList = document.getDocumentElement().getElementsByTagName("context");
                for(int i=0;i<nodeList.getLength();i++){
                    Node node = nodeList.item(i);
                    if(node.getNodeName().equals("log")){
                        String expression = node.getTextContent();
                        BufferedReader reader = new BufferedReader(new StringReader(expression));
                        String line = null;
                        try {
                            line = reader.readLine();
                            while(line!=null){
                                Object res = resolver.evaluate(line);
                                if(res!=null) {
                                    System.out.println(res);
                                }
                                line = reader.readLine();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 10000L);
    }

}
