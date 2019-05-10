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
import org.apache.tamaya.Configuration;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Tests the Tamaya Vertx configuration support. Created by atsticks on
 * 08.03.17.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigVerticleTest {

    @Rule
    public RunTestOnContext vertxContext = new RunTestOnContext();

    private TamayaConfigurationProducer producerVerticle = new TamayaConfigurationProducer();

    @Before
    public void prepare(final TestContext testContext) {
        final Async deployAsync = testContext.async();
        vertxContext.vertx().deployVerticle(producerVerticle, res -> {
            if (!res.succeeded()) {
                testContext.fail();
            }
            deployAsync.complete();
        });
    }

    @Test
    public void testSingle(final TestContext testContext) {
        final Async async = testContext.async();
        vertxContext.vertx().eventBus().send(TamayaConfigurationProducer.DEFAULT_CONFIG_GET_SINGLE_ADDRESS,
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
    public void testMap(final TestContext testContext) {
        final Async async = testContext.async();
        String selector = "[]{\"user.*\"}";
        vertxContext.vertx().eventBus().send(TamayaConfigurationProducer.DEFAULT_CONFIG_GET_MULTI_ADDRESS,
                selector, reply -> {
                    testContext.assertNotNull(reply.result());
                    testContext.assertNotNull(reply.result().body());
                    Map<String, String> config = Json.decodeValue((String) reply.result().body(),
                            Map.class);
                    Map<String, String> compareTo = Configuration.current()
                            .map(ConfigurationFunctions.filter((k, v) -> k.matches("user."))).getProperties();
                    testContext.assertEquals(config.size(), compareTo.size());
                    for (Map.Entry<String, String> en : compareTo.entrySet()) {
                        testContext.assertEquals(
                                config.get(en.getKey()), en.getValue());
                    }
                    async.complete();
                });
    }

    @Test
    public void testConfigCalls(TestContext testContext) {
        testContext.assertNotNull(producerVerticle.getConfiguration());
        testContext.assertEquals(
                producerVerticle.getConfigValue("user.home"),
                System.getProperty("user.home"));
        testContext.assertEquals(
                producerVerticle.getOptionalConfigValue("foo.bar").orElse("blabla"),
                "blabla");
        testContext.assertEquals(
                producerVerticle.getOptionalConfigValue("foo.bar", BigDecimal.class).orElse(new BigDecimal("1.12345")),
                new BigDecimal("1.12345"));
    }

}
