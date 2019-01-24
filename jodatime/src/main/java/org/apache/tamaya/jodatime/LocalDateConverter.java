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
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.util.Objects;

/**
 * Converter, converting from {@code String} to Joda-Time's
 * {@code LocalDate}.
 *
 * The converter supports the following formats for the provided
 * time information:
 *
 * <ul>
 *     <li>{@code yyyy ['-' MM ['-' dd]]}</li>
 *     <li>{@code yyyy ['-' DDD]}</li>
 *     <li>{@code xxxx '-W' ww ['-' e]}</li>
 * </ul>
 */
public class LocalDateConverter implements PropertyConverter<LocalDate> {
    static final String PARSER_FORMATS[] = {
            "yyyy ['-' MM ['-' dd]]",
            "yyyy ['-' DDD]",
            "xxxx '-W' ww ['-' e]",
            "yyyy ['-' dd ['-' MM]]",
    };


    // The DateTimeFormatter returned by ISODateTimeFormat are thread safe
    // according to the JavaDoc of JodaTime
    final static DateTimeParser FORMATS[] = {
            DateTimeFormat.forPattern(PARSER_FORMATS[0]).getParser(),
            DateTimeFormat.forPattern(PARSER_FORMATS[1]).getParser(),
         DateTimeFormat.forPattern(PARSER_FORMATS[2]).getParser(),
            DateTimeFormat.forPattern(PARSER_FORMATS[3]).getParser(),
    };

    protected static final DateTimeFormatter FORMATTER;

    static {
        FORMATTER = new DateTimeFormatterBuilder().append(null, FORMATS).toFormatter();
    }

    @Override
    public LocalDate convert(String value, ConversionContext context) {
        context.addSupportedFormats(LocalDateConverter.class, PARSER_FORMATS);

        String trimmed = Objects.requireNonNull(value).trim();
        LocalDate result = null;

        try {
            result = FORMATTER.parseLocalDate(trimmed);
        } catch (RuntimeException e) {
            // Ok, go on and try the next parser
        }

        return result;
    }
}
