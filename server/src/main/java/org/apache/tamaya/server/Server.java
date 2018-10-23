/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.server;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.ws.rs.core.Application;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Main Application for the Tamaya Configuration Server.
 */
public class Server {

    /**
     * Utility class.
     */
    private Server(){}


    public static void start() throws Exception {
        start("/", 8085);
    }

    public static void start(int port) throws Exception {
        start("/", port);
    }

    public static void start(String contextPath, int port) throws Exception {
        Objects.requireNonNull(contextPath);
        String appBase = ".";
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        // Define a web application context.
        Context context = tomcat.addWebapp(contextPath, new File(
                appBase).getAbsolutePath());
        // Add servlet that will register Jersey REST resources
        String servletName = "cxf-servlet";
        Wrapper wrapper = tomcat.addServlet(context, servletName,
                org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet.class.getName());
        wrapper.addInitParameter("javax.ws.rs.Application", ResourceLoader.class.getName());
        context.addServletMapping("/*", servletName);
        tomcat.start();
        tomcat.getServer().await();
    }


    public static void main(String... args) throws Exception {
        Configuration config = Configuration.current();
        String contextPath = config.getOrDefault("tamaya.server.contextPath", "/");
        int port = config.getOrDefault("tamaya.server.port", Integer.class, 8085);
        start(contextPath, port);
    }

    /**
     * JAX RS Application.
     */
    public final static class ResourceLoader extends Application{

        @Override
        public Set<Class<?>> getClasses() {
            final Set<Class<?>> classes = new HashSet<>();
            // register root resource
            classes.add(ConfigurationServices.class);
            return classes;
        }
    }

}