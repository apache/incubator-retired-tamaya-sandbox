///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.apache.tamaya.osgi;
//
//import org.osgi.service.cm.ConfigurationAdmin;
//import org.osgi.service.component.annotations.*;
//
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.logging.Logger;
//
///**
// * Created by atsticks on 06.09.17.
// */
//@Component(name = ConfigPrinter.COMPONENT_NAME,
//        configurationPolicy = ConfigurationPolicy.OPTIONAL,
//        configurationPid = ConfigPrinter.COMPONENT_NAME,
//        service = ConfigPrinterService.class)
//public class ConfigPrinter implements ConfigPrinterService {
//
//    public static final String COMPONENT_NAME = "ConfigPrinter";
//    public static final String COMPONENT_LABEL = "Managed ConfigPrinter Service";
//
//    private static final Logger LOG = Logger.getLogger(ConfigPrinter.class.getName());
//
//    private static ExecutorService executor = Executors.newCachedThreadPool();
//    private Worker worker = new Worker();
//
//    private ConfigurationAdmin cm;
//
//    @Reference
//    void setConfigurationAdmin(ConfigurationAdmin cm) {
//        this.cm = cm;
//    }
//
//    /**
//     * Called when all of the SCR Components required dependencies have been
//     * satisfied.
//     */
//    @Activate
//    @Modified
//    public void updateConfig(final Map<String,String> properties) {
//        LOG.info("Activating the " + COMPONENT_LABEL);
//        if(properties!=null) {
//            worker.setConfig(properties.toString());
//        }else{
//            worker.setConfig("no config.");
//        }
//    }
//
//    /**
//     * Called when any of the SCR Components required dependencies become
//     * unsatisfied.
//     */
//    @Deactivate
//    public void deactivate() {
//        LOG.info("Deactivating the " + COMPONENT_LABEL);
//    }
//
//    @Override
//    public void startPrinter() {
//        executor.execute(worker);
//    }
//
//    @Override
//    public void stopPrinter() {
//        if (!executor.isTerminated()) {
//            executor.shutdownNow();
//        }
//    }
//
//    /**
//     * Thread worker that continuously prints a message.
//     */
//    private static class Worker implements Runnable {
//
//        private String config;
//
//        public void run() {
//            boolean running = true;
//            int messageCount = 0;
//            while (running) {
//                try {
//                    LOG.info("Config " + (++messageCount) + ": " + config);
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    running = false;
//                    LOG.info("Thread shutting down");
//                }
//            }
//        }
//
//        public void setConfig(String config) {
//            this.config = config;
//        }
//
//    }
//}