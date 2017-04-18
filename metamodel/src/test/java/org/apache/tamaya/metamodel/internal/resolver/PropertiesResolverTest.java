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

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * Created by atsticks on 18.04.17.
 */
public class PropertiesResolverTest {

    private PropertiesResolver r = new PropertiesResolver();

    @Test
    public void getResolverId() throws Exception {
        assertEquals(r.getResolverId(), "properties");
    }

    @Test
    public void evaluate() throws Exception {
        for(Map.Entry<String,String> en: System.getenv().entrySet()){
            assertEquals(en.getValue(), r.evaluate("env:"+en.getKey()));
        }
        assertEquals("foo", r.evaluate("env:fsdifoisfo?default=foo"));
        for(Map.Entry en: System.getProperties().entrySet()){
            assertEquals(en.getValue(), r.evaluate("system:"+en.getKey()));
        }
        assertEquals("foo", r.evaluate("system:fsdifoisfo?default=foo"));
    }

}