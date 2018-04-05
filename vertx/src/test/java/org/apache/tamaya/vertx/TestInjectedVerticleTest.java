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
package org.apache.tamaya.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tamaya.ConfigurationProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Tests the Tamaya Vertx configuration support.
 * Created by atsticks on 08.03.17.
 */
@RunWith(VertxUnitRunner.class)
public class TestInjectedVerticleTest {

    @Rule
    public RunTestOnContext vertxContext = new RunTestOnContext();

    private TestInjectedVerticle testVerticle = new TestInjectedVerticle();

    private TamayaConfigurationProducer producerVerticle = new TamayaConfigurationProducer();

    @Before
    public void prepare( TestContext testContext)throws Exception{
        Async async = testContext.async();
        vertxContext.vertx().deployVerticle(producerVerticle, h-> async.complete());
    }

    @Test
    public void testSingle(final TestContext testContext){
        final Async async = testContext.async();
        final Async async2 = testContext.async();
        vertxContext.vertx().eventBus().send(TamayaConfigurationProducer.DEFAULT_CONFIG_GET_SINGLE_ADDRESS,
                "user.home", reply -> {
                        testContext.assertNotNull(reply.result());
                        testContext.assertNotNull(reply.result().body());
                        testContext.assertEquals(
                                reply.result().body(),
                                System.getProperty("user.home"));
                        async.complete();
                });
        String[] arr = new String[]{"user."};
        vertxContext.vertx().eventBus().send(TamayaConfigurationProducer.DEFAULT_CONFIG_GET_MULTI_ADDRESS,
                Json.encode(arr), reply -> {
                        testContext.assertTrue(reply.succeeded());
                        testContext.assertNotNull(reply.result());
                        testContext.assertNotNull(reply.result().body());
                        Map<String,String> config = Json.decodeValue((String)reply.result().body(),
                                Map.class);
                        testContext.assertEquals(
                                config.get("user.home"),
                                System.getProperty("user.home"));
                        testContext.assertEquals(
                                config.get("user.name"),
                                System.getProperty("user.name"));
                        async2.complete();
                });
    }

    @Test
    public void testMupltiple(final TestContext testContext){
        final Async async = testContext.async();
        String[] arr = new String[]{"user.home", "user.name","java.version"};
        vertxContext.vertx().eventBus().send(TamayaConfigurationProducer.DEFAULT_CONFIG_GET_MULTI_ADDRESS,
                Json.encode(arr), reply -> {
                        testContext.assertNotNull(reply.result());
                        testContext.assertNotNull(reply.result().body());
                        Map<String,String> config = Json.decodeValue((String)reply.result().body(), Map.class);
                        Map<String,String> compareTo = ConfigurationProvider.getConfiguration().getProperties();
                        testContext.assertEquals(
                                    config.get("user.name"), System.getProperty("user.name"));
                        testContext.assertEquals(
                                config.get("user.home"), System.getProperty("user.home"));
                        testContext.assertEquals(
                                config.get("java.version"), System.getProperty("java.version"));
                        async.complete();
                });
    }

    @Test
    public void testInjection(TestContext testContext){
        Async async = testContext.async();
        vertxContext.vertx().deployVerticle(testVerticle, h-> {
            testContext.assertNotNull(testVerticle.userHome);
            testContext.assertNotNull(testVerticle.userName);
            testContext.assertNotNull(testVerticle.anyNumber);
            testContext.assertEquals(
                    testVerticle.userHome,
                    System.getProperty("user.home"));
            testContext.assertEquals(
                    testVerticle.userName,
                    System.getProperty("user.name"));
            testContext.assertEquals(
                    testVerticle.anyNumber,
                    new BigDecimal("1.123456789"));
            async.complete();
        });

    }
}
