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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.functions.ConfigurationFunctions;
import org.apache.tamaya.inject.ConfigurationInjector;
import org.apache.tamaya.inject.api.Config;

import java.util.Map;
import java.util.TreeMap;

/**
 * This is a simple verticle registering Tamaya event bus messaging for accessing configuration:
 * <ul>
 *     <li>Don't pass anything, current a {@link JsonObject} with the full Tamaya configuration.</li>
 *     <li>Pass a {@code String} key, current a String return createValue, if present or a failure.</li>
 *     <li>Pass a {@link JsonArray} of keys, current a {@link JsonObject} return createValue, with the key/values found.</li>
 * </ul>
 */
public class TamayaConfigurationProducer extends AbstractConfiguredVerticle{

    public static final String DEFAULT_CONFIGRE_ADDRESS = "CONFIG.CONFIGURE";
    public static final String DEFAULT_CONFIG_GET_MULTI_ADDRESS = "CONFIG.GET.MAP";
    public static final String DEFAULT_CONFIG_GET_SINGLE_ADDRESS = "CONFIG.GET.SINGLE";

    @Config(key = "tamaya.vertx.busaddress.inject", defaultValue = DEFAULT_CONFIGRE_ADDRESS)
    private String injectionBusTarget;

    @Config(key = "tamaya.vertx.busaddress.multi", defaultValue = DEFAULT_CONFIG_GET_MULTI_ADDRESS)
    private String mapBusTarget;

    @Config(key = "tamaya.vertx.busaddress.single", defaultValue = DEFAULT_CONFIG_GET_SINGLE_ADDRESS)
    private String singleBusTarget;


    /**
     * Registers a handler for accessing single configuration keys (input: String, reply type: String). If no
     * config createValue is present the consumer will reply with a NOT_FOUND failure.
     * @param address the event bus address to register.
     * @param eventBus the event bus.
     * @return the consumer registered.
     */
    public static MessageConsumer<String> registerSingleConfigEntryProvider(String address, EventBus eventBus){
        MessageConsumer<String> consumer = eventBus.consumer(address);
        consumer.handler(h -> {
            String key = (String) h.body();
            if (key == null) {
                h.fail(HttpResponseStatus.BAD_REQUEST.code(), "Missing config key.");
            } else {
                String value = Configuration.current().getOrDefault(key, null);
                if (value != null) {
                    h.reply(value);
                } else {
                    h.fail(HttpResponseStatus.NOT_FOUND.code(), "Config key not found: " + key);
                }
            }
        });
        return consumer;
    }

    /**
     * Registers a handler for accessing multiple configuration keys (input: String[] (Json),
     * reply type: {@code Map<String,String>} (Json).
     * @param address the event bus address to register.
     * @param eventBus the event bus.
     * @return the consumer registered.
     */
    public static MessageConsumer<String> registerMultiConfigEntryProvider(String address, EventBus eventBus){
        MessageConsumer<String> consumer = eventBus.consumer(address);
        consumer.handler(h -> {
            String val = h.body();
            Configuration config = Configuration.current();
            Map<String,String> entries = new TreeMap<>();
            if(val!=null){
                String[] sections = Json.decodeValue(val, String[].class);
                for (String section : sections) {
                    if(section!=null) {
                        entries.putAll(config.map(ConfigurationFunctions.section(section)).getProperties());
                    }
                }
            }else{
                entries.putAll(config.getProperties());
            }
            h.reply(Json.encode(entries));
        });
        return consumer;
    }

    /**
     * Registers a handler for configuring any objects sent via the message bus using Tamaya's injection API.
     * @param address the event bus address to register.
     * @param eventBus the event bus.
     * @return the consumer registered.
     */
    public static MessageConsumer<Object> registerConfigurationInjector(String address, EventBus eventBus){
        MessageConsumer<Object> consumer = eventBus.consumer(address);
        consumer.handler(h -> {
            Object o = h.body();
            if(o==null){
                h.fail(HttpResponseStatus.BAD_REQUEST.code(), "Required createObject to configure is missing.");
            }else {
                ConfigurationInjector.getInstance().configure(o);
                h.reply("OK");
            }
        });
        return consumer;
    }



    @Override
    public void start(Future<Void> startFuture) throws Exception {
        registerMultiConfigEntryProvider(mapBusTarget, vertx.eventBus());
        registerSingleConfigEntryProvider(singleBusTarget, vertx.eventBus());
        registerConfigurationInjector(injectionBusTarget, vertx.eventBus());
        super.start(startFuture);
    }

    public String getInjectionBusTarget() {
        return injectionBusTarget;
    }

    public String getMapBusTarget() {
        return mapBusTarget;
    }

    public String getSingleBusTarget() {
        return singleBusTarget;
    }
}
