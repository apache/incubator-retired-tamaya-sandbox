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

import org.apache.tamaya.base.convert.ConversionContext;
import org.apache.tamaya.spi.TypeLiteral;
import org.junit.Test;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Basic tests for Tamaya collection support. Relevant configs for this tests:
 * <pre>base.items=1,2,3,4,5,6,7,8,9,0
 * base.map=1::a, 2::b, 3::c, [4:: ]
 * </pre>
 */
public class CollectionsBaseTests {

    @Test
    public void testList_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<List<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("base.items", List.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items = (List<String>) config.getValue("base.items", List.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
    }

    @Test
    public void testArrayList_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<ArrayList<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("base.items", ArrayList.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof ArrayList);
        items = (ArrayList<String>) config.getValue("base.items", ArrayList.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof ArrayList);
    }

    @Test
    public void testLinkedList_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<LinkedList<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        List<String> items = config.getValue("base.items", List.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items instanceof LinkedList);
        assertEquals(10, items.size());
        items = (LinkedList<String>) config.getValue("base.items", LinkedList.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof LinkedList);
    }

    @Test
    public void testSet_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<Set<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("base.items", Set.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        items = (Set<String>) config.getValue("base.items", Set.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
    }

    @Test
    public void testSortedSet_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<SortedSet<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("base.items", Set.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertTrue(items instanceof SortedSet);
        assertEquals(10, items.size());
        items = (SortedSet<String>) config.getValue("base.items", SortedSet.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof SortedSet);
    }

    @Test
    public void testHashSet_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<HashSet<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("base.items", Set.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof HashSet);
        items = (HashSet<String>) config.getValue("base.items", HashSet.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof HashSet);
    }

    @Test
    public void testTreeSet_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.items", new TypeLiteral<TreeSet<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Set<String> items = config.getValue("base.items", Set.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof TreeSet);
        items = (TreeSet<String>) config.getValue("base.items", TreeSet.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof TreeSet);
    }

    @Test
    public void testMap_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.map", new TypeLiteral<Map<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("base.map", Map.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        items = (Map<String,String>) config.getValue("base.map", Map.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
    }

    @Test
    public void testHashMap_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.map", new TypeLiteral<HashMap<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("base.map", Map.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        assertTrue(items instanceof HashMap);
        items = (HashMap<String,String>) config.getValue("base.map", HashMap.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        assertTrue(items instanceof HashMap);
    }

    @Test
    public void testSortedMap_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.map", new TypeLiteral<SortedMap<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("base.map", Map.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        assertTrue(items instanceof SortedMap);
        items = (Map<String,String>) config.getValue("base.map", SortedMap.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        assertTrue(items instanceof SortedMap);
    }

    @Test
    public void testTreeMap_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.map", new TypeLiteral<TreeMap<String,String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Map<String,String> items = config.getValue("base.map", Map.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        assertTrue(items instanceof TreeMap);
        items =  config.getValue("base.map", TreeMap.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(4, items.size());
        assertEquals("a", items.get("1"));
        assertEquals("b", items.get("2"));
        assertEquals("c", items.get("3"));
        assertEquals(" ", items.get("4"));
        assertTrue(items instanceof TreeMap);
    }

    @Test
    public void testCollection_String(){
        Config config = ConfigProvider.getConfig();
        ConversionContext ctx = new ConversionContext.Builder(
                "base.map", new TypeLiteral<List<String>>(){}.getType()).build();
        ConversionContext.setContext(ctx);
        Collection<String> items = config.getValue("base.items", Collection.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
        assertTrue(items instanceof List);
        items = (Collection<String>) config.getValue("base.items", Collection.class);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(10, items.size());
    }
}
