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

import org.apache.tamaya.metamodel.MetaContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created by atsticks on 18.04.17.
 */
public class JavaResolverTest {

    public static final String TEST = "ResTest";

    private JavaResolver r = new JavaResolver();

    @Test
    public void getResolverId() throws Exception {
        assertEquals(r.getResolverId(), "java");
    }

    @Test
    public void evaluateDirect() throws Exception {
        assertEquals("createValue", r.evaluate("\"createValue\""));
        assertEquals("1.1", r.evaluate("1.1"));
        assertEquals("1", r.evaluate("1"));
    }

    @Test
    public void evaluateProperties() throws Exception {
        assertEquals(System.getProperty("java.version"), r.evaluate("sys(\"java.version\")"));
        String key = System.getenv().keySet().iterator().next();
        assertEquals(System.getenv(key), r.evaluate("env(\""+key+"\")"));
        MetaContext.getInstance().setProperty("foo", "bar");
        assertEquals("bar", r.evaluate("context(\"foo\")"));
    }

    @Test
    public void evaluateExpression() throws Exception {
        assertEquals("true", r.evaluate("env(\"STAGE\") == null"));
        assertEquals("true", r.evaluate("sys(\"STAGE\") == null"));
        System.setProperty("STAGE", "DEV2");
        assertEquals("false", r.evaluate("sys(\"STAGE\") == null"));
        System.setProperty("STAGE", "DEV2");
        assertEquals("DEV2", r.evaluate("sys(\"STAGE\") == null?env(\"STAGE\"):sys(\"STAGE\")"));
        assertEquals("DEV2", r.evaluate("if(sys(\"STAGE\") == null)return env(\"STAGE\"); else return sys(\"STAGE\");"));
        System.getProperties().remove("STAGE");
        assertEquals("foo", r.evaluate("if(sys(\"STAGE\") == null)return \"foo\"; else return sys(\"STAGE\");"));
    }

    @Test
    public void evaluateSimple() throws Exception {
        assertEquals(TEST, r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest.TEST"));
        assertEquals(TEST, r.evaluate("new org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest().getTest1()"));
        assertEquals(TEST, r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest.getTest2()"));
    }

    public String getTest1(){
        return TEST;
    }

    public static String getTest2(){
        return TEST;
    }

}