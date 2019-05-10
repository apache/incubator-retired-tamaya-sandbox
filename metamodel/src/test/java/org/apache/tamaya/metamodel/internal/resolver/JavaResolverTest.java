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

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by atsticks on 18.04.17.
 */
public class JavaResolverTest {

    public static final String TEST = "ResTest";

    private JavaResolver r = new JavaResolver();

    @Test
    public void getResolverId() throws Exception {
        assertThat(r.getResolverId()).isEqualTo("java");
    }

    @Test
    public void evaluateDirect() throws Exception {
        assertThat("createValue").isEqualTo(r.evaluate("\"createValue\""));
        assertThat("1.1").isEqualTo(r.evaluate("\"1.1\""));
        assertThat(1).isEqualTo(r.evaluate("1"));
    }

    @Test
    public void evaluateProperties() throws Exception {
        assertThat(System.getProperty("java.version")).isEqualTo(r.evaluate("sys(\"java.version\")"));
        String key = System.getenv().keySet().iterator().next();
        assertThat(System.getenv(key)).isEqualTo(r.evaluate("env(\""+key+"\")"));
        MetaContext.getInstance().setStringProperty("foo", "bar");
        assertThat("bar").isEqualTo(r.evaluate("context(\"foo\")"));
    }

    @Test
    public void evaluateExpression() throws Exception {
        assertThat(true).isEqualTo(r.evaluate("env(\"STAGE\") == null"));
        assertThat(true).isEqualTo(r.evaluate("sys(\"STAGE\") == null"));
        System.setProperty("STAGE", "DEV2");
        assertThat(false).isEqualTo(r.evaluate("sys(\"STAGE\") == null"));
        System.setProperty("STAGE", "DEV2");
        assertThat("DEV2").isEqualTo(r.evaluate("sys(\"STAGE\") == null?env(\"STAGE\"):sys(\"STAGE\")"));
        assertThat("DEV2").isEqualTo(r.evaluate("if(sys(\"STAGE\") == null)return env(\"STAGE\"); else return sys(\"STAGE\");"));
        System.getProperties().remove("STAGE");
        assertThat("foo").isEqualTo(r.evaluate("if(sys(\"STAGE\") == null)return \"foo\"; else return sys(\"STAGE\");"));
    }

    @Test
    public void evaluateSimple() throws Exception {
        assertThat(TEST).isEqualTo(r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest.TEST"));
        assertThat(TEST).isEqualTo(r.evaluate("new org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest().getTest1()"));
        assertThat(TEST).isEqualTo(r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest.getTest2()"));
    }

    public String getTest1(){
        return TEST;
    }

    public static String getTest2(){
        return TEST;
    }

}
