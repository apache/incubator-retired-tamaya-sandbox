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
package org.apache.tamaya.ui.views;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.apache.tamaya.spi.PropertyFilter;
import org.apache.tamaya.spi.PropertySource;

import javax.annotation.Priority;
import java.util.List;
import java.util.Map;

/**
 * Created by atsticks on 29.06.16.
 */
@Priority(0)
public class TamayaGeneralSystemInfoProvider extends AbstractTextInfoProvider {

    @Override
    protected String getCaption(){
        return "System Info";
    }

    @Override
    protected String getInfo() {
        Configuration config = Configuration.current();
        return new StringBuilder()
                .append("Configuration:                   ").append(config.getClass().getName()).append('\n')
                .append("ConfigurationContext:            ").append(config.getContext().getClass().getName()).append('\n')
                .append("PropertyValueCombinationPolicy:  ")
                .append(config.getContext().getPropertyValueCombinationPolicy().getClass().getName()).append('\n')
                .append("Property sources:                ").append(config.getContext().getPropertySources().size()).append('\n')
                .append(getPropertySourceList(config.getContext())).append('\n')
                .append("Property filters:                ").append(config.getContext().getPropertyFilters().size()).append('\n')
                .append(getPropertyFilterList(config.getContext())).append('\n')
                .append("Property converters:             ").append(config.getContext().getPropertyConverters().size()).append('\n')
                .append(getPropertyConverterList(config.getContext())).append('\n')
                .toString();

    }

    private String getPropertySourceList(ConfigurationContext context) {
        StringBuilder b = new StringBuilder();
        b.append("  ").append(format("NAME", 40)).append(format("ORDINAL", 8))
                .append(format("CLASS", 30)).append('\n');
        b.append("  ---------------------------------------------------------------------------------" +
                "-------------------------------------------------------------").append('\n');
        for(PropertySource ps:context.getPropertySources()){
            b.append("  ").append(format(ps.getName(),40)).append(format(String.valueOf(ps.getOrdinal()), 8))
                    .append(format(ps.getClass().getSimpleName(), 30)).append('\n');
        }
        return b.toString();
    }

    private String getPropertyFilterList(ConfigurationContext context) {
        StringBuilder b = new StringBuilder();
        b.append("  ").append(format("CLASS", 80)).append('\n');
        b.append("  ---------------------------------------------------------------------------------" +
                "-------------------------------------------------------------").append('\n');
        for(PropertyFilter ps:context.getPropertyFilters()){
            b.append("  ").append(format(ps.getClass().getName(), 80)).append('\n');
        }
        return b.toString();
    }

    private String getPropertyConverterList(ConfigurationContext context) {
        StringBuilder b = new StringBuilder();
        b.append("  ").append(format("TYPE", 30))
                .append(format("CONVERTERS", 90)).append('\n');
        b.append("  ---------------------------------------------------------------------------------" +
                "-------------------------------------------------------------").append('\n');
        for(Map.Entry<TypeLiteral<?>,List<PropertyConverter<?>>> ps:context.getPropertyConverters().entrySet()){
            b.append("  ").append(format(ps.getKey().getRawType().getSimpleName(),30))
                    .append(format(getClassList(ps.getValue()), 90)).append('\n');
        }
        return b.toString();
    }

    private String getClassList(List<PropertyConverter<?>> items) {
        StringBuilder b = new StringBuilder();
        for (Object o : items) {
            b.append(o.getClass().getName()).append(", ");
        }
        if (b.length() > 0) {
            b.setLength(b.length() - 2);
        }
        return b.toString();
    }

    private String format(String val, int len){
        if(val.length()>len){
            return val.substring(0, len-3)+".. ";
        }
        StringBuilder b = new StringBuilder(val);
        for(int i=0;i<len-val.length();i++){
            b.append(' ');
        }
        return b.toString();
    }


}
