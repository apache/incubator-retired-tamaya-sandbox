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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import org.apache.tamaya.inject.api.Config;

import java.math.BigDecimal;

/**
 * Small configured verticle for testing Tamaya's vertx support.
 */
public class TestInjectedVerticle extends AbstractVerticle{

    @Config(key="user.name")
    public String userName;

    @Config(key="user.home")
    public String userHome;

    @Config(key= "any.number.BD", defaultValue = "1.123456789")
    public BigDecimal anyNumber;

    @Override
    public void start(Future<Void> startFuture)throws Exception{
        vertx.eventBus().registerCodec(new MessageCodec() {
            @Override
            public void encodeToWire(Buffer buffer, Object o) {
                buffer.appendBytes(Json.encodePrettily(o).getBytes());
            }

            @Override
            public Object decodeFromWire(int i, Buffer buffer) {
                return null;
            }

            @Override
            public Object transform(Object o) {
                return o;
            }

            @Override
            public String name() {
                return "local";
            }

            @Override
            public byte systemCodecID() {
                return -1;
            }
        });
        vertx.eventBus().send(TamayaConfigurationProducer.DEFAULT_CONFIGRE_ADDRESS, this,
                new DeliveryOptions().setCodecName("local"),
                r -> startFuture.complete());
    }

    @Override
    public String toString() {
        return "TestInjectedVerticle{" +
                "userName='" + userName + '\'' +
                ", userHome='" + userHome + '\'' +
                ", anyNumber='" + anyNumber + '\'' +
                '}';
    }
}
