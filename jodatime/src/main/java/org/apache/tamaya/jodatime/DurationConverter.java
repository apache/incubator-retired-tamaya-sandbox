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
package org.apache.tamaya.jodatime;

import org.apache.tamaya.base.convert.ConversionContext;
import org.joda.time.Duration;
import org.joda.time.Period;

import javax.config.spi.Converter;
import java.util.Objects;

/**
 * <p>A {@link Converter} for converting a string representation of a
 * given duration into a {@link Duration} instance.</p>
 *
 * <p>This converter supports the following string representations of a
 * duration:</p>
 *
 *   <ol>
 *     <li>ISO8601 format ({@code PTa.bS}). For example, "PT72.345S" represents 1 minute,
 *     12 seconds and 345 milliseconds.</li>
 *     <li>All the period formats as defined in {@link PeriodConverter}.</li>
 *   </ol>
 */
public class DurationConverter implements Converter<Duration> {

    private PeriodConverter periodConverter = new PeriodConverter();

    @Override
    public Duration convert(String value) {
        String trimmed = Objects.requireNonNull(value).trim();
        ConversionContext context = ConversionContext.getContext();
        if(context!=null) {
            addSupportedFormats(context);
        }
        try {
            return Duration.parse(value);
        }catch(Exception e){
            Period period = null;
            if(value.startsWith("P")){
                period = periodConverter.convert("P0Y0M0W"+value.substring(1));
            }
            if(period == null){
                period = periodConverter.convert("P0000-00-"+value);
            }
            if(period != null){
                return period.toStandardDuration();
            }
        }
        return null;
    }

    private void addSupportedFormats(ConversionContext context) {
        context.addSupportedFormats(getClass(), "PTa.bS");
        context.addSupportedFormats(getClass(), "PdDThHmMsS");
        context.addSupportedFormats(getClass(), "ddThh:mm:ss");
    }

}
