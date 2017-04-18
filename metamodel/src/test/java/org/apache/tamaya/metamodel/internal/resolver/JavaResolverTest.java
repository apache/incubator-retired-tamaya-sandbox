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

import static org.junit.Assert.*;


/**
 * Created by atsticks on 18.04.17.
 */
public class JavaResolverTest {

    private static final String TEST = "ResTest";
    private JavaResolver r = new JavaResolver();

    @Test
    public void getResolverId() throws Exception {
        assertEquals(r.getResolverId(), "java");
    }

    @Test
    public void evaluate() throws Exception {
        assertEquals(TEST, r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest#TEST"));
        assertEquals(TEST, r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest#getTest4"));
        assertEquals(TEST, r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest#getTest6"));
        assertEquals(TEST, r.evaluate("org.apache.tamaya.metamodel.internal.resolver.JavaResolverTest#getTest7"));
    }

    private String getTest5(){
        return TEST;
    }

    String getTest2(){
        return TEST;
    }

    public String getTest3(){
        return TEST;
    }

    static String getTest4(){
        return TEST;
    }

    private static String getTest6(){
        return TEST;
    }

    public static String getTest7(){
        return TEST;
    }

}