/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.metamodel.internal.resolver;

import bsh.*;
import org.apache.tamaya.metamodel.MetaContext;
import org.apache.tamaya.metamodel.spi.SimpleResolver;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple resolver for {@link MetaContext} entries that
 * reads data from system and environment properties and uses
 * beanshell evaluation.
 *
 * Valid inputs are:
 * <ul>
 *     <li>{@code ${java:expression} }, whereas <i>expression</i> evaluates to the required type.</li>
 * </ul>
 */
public final class JavaResolver implements SimpleResolver{

    private static final Logger LOG = Logger.getLogger(JavaResolver.class.getName());


    @Override
    public String getResolverId() {
        return "java";
    }

    @Override
    public String evaluate(String expression) {
        try{
            return String.valueOf(evaluate(expression, null));
        }catch(Exception e){
            LOG.log(Level.WARNING, "Error evaluating expression: " + expression, e);
            return "ERROR{"+expression+"}:"+e;
        }
    }

    public Object evaluate(String expression, Map<String, String> context) throws UtilEvalError, EvalError {
            BshClassManager bshClassManager = new BshClassManager();
            NameSpace namespace = new NameSpace(bshClassManager, "config");
            namespace.loadDefaultImports();
            namespace.importStatic(JavaResolver.class);
            namespace.setVariable("contextprops", MetaContext.getInstance(), false);
            namespace.setVariable("envprops", System.getenv(), false);
            namespace.setVariable("sysprops", System.getProperties(), false);
            if(context!=null){
                for(Map.Entry<String,String> en:context.entrySet()){
                    namespace.setVariable(en.getKey(), en.getValue(), false);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream outStream = new PrintStream(out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            PrintStream errStream = new PrintStream(err);
            Interpreter interpreter = new Interpreter(null,
//                    new BufferedReader(
//                    new InputStreamReader(System.in)),
                    outStream, errStream,
                    false, namespace);
            return interpreter.eval(expression);
    }

    public static String context(String key){
        return MetaContext.getInstance().getProperty(key);
    }

    public static String env(String key){
        return System.getenv(key);
    }

    public static String sys(String key){
        return System.getProperty(key);
    }

    public static long TIME(){
        return System.currentTimeMillis();
    }

    public static Object[] eval(String command) throws IOException {
        Process proc = Runtime.getRuntime().exec(command);
        try(InputStream out = proc.getInputStream();
            InputStream err = proc.getErrorStream()){
            Object[] result = new Object[3];
            ByteArrayOutputStream sw = new ByteArrayOutputStream();
            byte[] buff = new byte[512];
            result[0] = proc.waitFor();
            int read = out.read(buff);
            while(read > 0){
                sw.write(buff, 0, read);
                out.read(buff);
            }
            result[1] = sw.toString();
            read = err.read(buff);
            while(read > 0){
                sw.write(buff, 0, read);
                err.read(buff);
            }
            result[2] = sw.toString();
            return result;
        } catch (InterruptedException e) {
            return new Object[]{"","Process interrupted.", -1};
        } catch (Exception e){
            return new Object[]{"","Process failed: " + e, -1};
        }
    }

}
