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
package org.apache.tamaya.collections;

import javax.config.Config;
import javax.config.ConfigProvider;

import org.apache.tamaya.base.convert.ConversionContext;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Basic tests for Tamaya collection support. Relevant configs for this tests:
 * <pre>base.items=1,2,3,4,5,6,7,8,9,0
 * base.map=1::a, 2::b, 3::c, [4:: ]
 * </pre>
 */
public class CollectionsTypedReadOnlyTests {

    @Test(expected=UnsupportedOperationException.class)
    public void testArrayListList_1(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.arraylist", new TypeLiteral<List<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("typed.arraylist", List.class);
        assertTrue(items instanceof ArrayList);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testArrayListList_2(){
        Config config = ConfigProvider.getConfig();ConversionContext ctx = new ConversionContext.Builder(
                "typed.arraylist", new TypeLiteral<ArrayList<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("typed.arraylist", List.class);
        assertTrue(items instanceof ArrayList);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testLinkedListList_1(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.linkedlist", new TypeLiteral<LinkedList<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("typed.linkedlist", List.class);
        assertTrue(items instanceof LinkedList);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testLinkedListList_2(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.linkedlist", new TypeLiteral<LinkedList<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("typed.linkedlist", LinkedList.class);
        assertTrue(items instanceof LinkedList);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }


    @Test(expected=UnsupportedOperationException.class)
    public void testHashSet_1(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.hashset", new TypeLiteral<Set<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("typed.hashset", Set.class);
        assertTrue(items instanceof HashSet);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }
    @Test(expected=UnsupportedOperationException.class)
    public void testHashSet_2(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.hashset", new TypeLiteral<HashSet<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("typed.hashset", Set.class);
        assertTrue(items instanceof HashSet);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testTreeSet_1(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.treeset", new TypeLiteral<TreeSet<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("typed.treeset", Set.class);
        assertTrue(items instanceof TreeSet);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }
    @Test(expected=UnsupportedOperationException.class)
    public void testTreeSet_2(){
        Config config = ConfigProvider.getConfig();
        Set<String> items = items = config.getValue("typed.treeset", TreeSet.class);
        assertTrue(items instanceof TreeSet);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items.add("test");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testHashMap_1(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.hashmap", new TypeLiteral<Map<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("typed.hashmap", Map.class);
        assertNotNull(items);
        assertTrue(items instanceof HashMap);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        items.put("g","hjhhj");
    }
    @Test(expected=UnsupportedOperationException.class)
    public void testHashMap_2(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.hashmap", new TypeLiteral<HashMap<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("typed.hashmap", Map.class);
        assertNotNull(items);
        assertTrue(items instanceof HashMap);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        items.put("g","hjhhj");
    }


    @Test(expected=UnsupportedOperationException.class)
    public void testTreeMap_1(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.treemap", new TypeLiteral<HashMap<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("typed.treemap", Map.class);
        assertNotNull(items);
        assertTrue(items instanceof TreeMap);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        items.put("g","hjhhj");
    }
    @Test(expected=UnsupportedOperationException.class)
    public void testTreeMap_2(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "typed.treemap", new TypeLiteral<HashMap<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("typed.treemap", TreeMap.class);
        assertNotNull(items);
        assertTrue(items instanceof TreeMap);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        items.put("g","hjhhj");
    }

}
