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
package org.apache.tamaya.jodatime;

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;
import org.joda.time.LocalTime;
import org.joda.time.format.*;

import java.util.Objects;

/**
 * Converter, converting from {@code String} to Joda-Time's
 * {@code LocalTime}.
 *
 * The converter supports the following formats for the provided
 * time information:
 *
 * <ul>
 *     <li>{@code ['T']` time-element}</li>
 *     <li>{@code time-element = HH [minute-element] | [fraction]}</li>
 *     <li>{@code minute-element = ':' mm [second-element] | [fraction]}</li>
 *     <li>{@code second-element = ':' ss [fraction] }</li>
 *     <li>{@code fraction       = ('.' | ',') digit+ }</li>
 * </ul>
 */
public class LocalTimeConverter implements PropertyConverter<LocalTime> {
    static final String PARSER_FORMATS[] = {
            "['T']` time-element\n" +
                    "time-element = HH [minute-element] | [fraction]\n" +
                    "minute-element = ':' mm [second-element] | [fraction]\n" +
                    "second-element = ':' ss [fraction]" +
                    "fraction       = ('.' | ',') digit+`",
    };

    @Override
    public LocalTime convert(String value) {
        ConversionContext.doOptional(context -> {
                    context.addSupportedFormats(LocalTimeConverter.class, PARSER_FORMATS);
                });

        String trimmed = Objects.requireNonNull(value).trim();
        try {
            return ISODateTimeFormat.localTimeParser().parseLocalTime(trimmed);
        } catch (RuntimeException e) {
            // Ok, go on and try the next parser
        }
        return null;
    }
}
