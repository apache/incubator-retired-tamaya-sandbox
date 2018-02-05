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

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.functions.PropertyMatcher;

import javax.config.Config;
import javax.config.ConfigProvider;
import javax.config.inject.ConfigProperty;

/**
 * Small configured verticle for testing Tamaya's vertx support.
 */
public class ConfigVerticle extends AbstractConfiguredVerticle{

    @ConfigProperty(name = "tamaya.vertx.config.map", defaultValue = "CONFIG-MAP")
    private String mapBusTarget;

    @ConfigProperty(name = "tamaya.vertx.config.value", defaultValue = "CONFIG-VAL")
    private String valBusTarget;

    private MessageConsumer<String> mapBusListener;
    private MessageConsumer<String> valBusListener;

    @Override
    public void start()throws Exception{
        mapBusListener = vertx.eventBus().consumer(mapBusTarget);
        mapBusListener.handler(message -> {
            Config config = ConfigProvider.getConfig();
            Config cfg = ConfigurationFunctions.filter(
                    (key, value) -> key.matches(message.body())
            ).apply(config);
            message.reply(Json.encodePrettily(ConfigurationFunctions.toMap(cfg)));
        });
        valBusListener = vertx.eventBus().consumer(valBusTarget);
        valBusListener.handler(message -> {
            Config config = ConfigProvider.getConfig();
            message.reply(config.getValue(message.body(), String.class));
        });
    }

    @Override
    public void stop()throws Exception{
        mapBusListener.unregister();
        valBusListener.unregister();
    }

    @Override
    public String toString() {
        return "ConfigVerticle{" +
                "mapBusTarget='" + mapBusTarget + '\'' +
                ", valBusTarget='" + valBusTarget + '\'' +
                '}';
    }
}
