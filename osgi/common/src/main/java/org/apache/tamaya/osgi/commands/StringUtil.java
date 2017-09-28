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
package org.apache.tamaya.osgi.commands;

/**
 * Some String related helper methods.
 */
public final class StringUtil {

    private StringUtil(){}

    public static String format(String in, int length){
        if(in==null){
            in = "";
        }
        int count = length - in.length();
        if(count<0){
            return in.substring(0,length-3) + "...";
        }
        return in + printRepeat(" ", count);
    }

    public static String printRepeat(String s, int times) {
        StringBuilder b = new StringBuilder();
        for(int i=0;i<times;i++){
            b.append(s);
        }
        return b.toString();
    }
}
