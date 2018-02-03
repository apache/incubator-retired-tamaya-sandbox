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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.config.Config;
import javax.config.ConfigProvider;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests the Tamaya Vertx configuration support.
 * Created by atsticks on 08.03.17.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigVerticleTest {

    @Rule
    public RunTestOnContext vertxContext = new RunTestOnContext();

    private TestVerticle testVerticle = new TestVerticle();

    @Before
    public void prepare(TestContext test){
        Async as = test.async(2);
        vertxContext.vertx().deployVerticle(testVerticle, r -> as.complete());
        vertxContext.vertx().deployVerticle(new ConfigVerticle(), r -> as.complete());
    }

    @Test
    public void testSingle(final TestContext testContext){
        final Async async = testContext.async();
        vertxContext.vertx().eventBus().send("CONFIG-VAL",
                "user.home", new Handler<AsyncResult<Message<String>>>() {
                    @Override
                    public void handle(AsyncResult<Message<String>> reply) {
                        testContext.assertEquals(
                                reply.result().body(),
                                System.getProperty("user.home"));
                        async.complete();
                    }
                });
    }

    @Test
    public void testMap(final TestContext testContext){
        final Async async = testContext.async();
        String selector = "user.";
        vertxContext.vertx().eventBus().send("CONFIG-MAP",
                selector, new Handler<AsyncResult<Message<String>>>() {
                    @Override
                    public void handle(AsyncResult<Message<String>> reply) {
                        testContext.assertNotNull(reply.result());
                        testContext.assertNotNull(reply.result().body());
                        Map<String,String> config = Json.decodeValue(reply.result().body(),
                                Map.class);
                        Config currentConfig = ConfigurationFunctions.filter((k, v) -> {
                            return k.matches("user.");
                        }).apply(ConfigProvider.getConfig());
                        Map<String,String> compareTo = ConfigurationFunctions.toMap(currentConfig);
                        testContext.assertEquals(config.size(), compareTo.size());
                        for(Map.Entry<String,String> en:compareTo.entrySet()){
                            testContext.assertEquals(
                                    config.get(en.getKey()), en.getValue());
                        }
                        async.complete();
                    }
                });
    }

    @Test
    public void testConfigCalls(TestContext testContext){
        testContext.assertNotNull(testVerticle.getConfiguration());
        testContext.assertEquals(
                testVerticle.getConfigValue("user.home"),
                System.getProperty("user.home"));
        testContext.assertEquals(
                testVerticle.getOptionalConfigValue("foo.bar").orElse( "blabla"),
                "blabla");
        testContext.assertEquals(
                testVerticle.getOptionalConfigValue("foo.bar", BigDecimal.class).orElse(new BigDecimal("1.12345")),
                new BigDecimal("1.12345"));
    }

    @Test
    public void testInjection(TestContext testContext){
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
    }
}
