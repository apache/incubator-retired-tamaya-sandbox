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
package org.apache.tamaya.uom;

import org.apache.tamaya.spi.ConversionContext;
import org.apache.tamaya.spi.PropertyConverter;
import javax.measure.Unit;
import javax.measure.format.UnitFormat;
import javax.measure.spi.ServiceProvider;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Converter from {@code String} to a JSR 363 {@code Unit}.
 *
 * @see Unit
 * @author wkeil
 */
// TODO not sure, if this could clash with JSR 363's own UnitConverter, but unless that's used here, it might be OK
public class UnitConverter implements PropertyConverter<Unit> {
    private static final String PATTERN_REGEX = "(\\+|-)?\\d+";
    private static final Pattern IS_INTEGER_VALUE = Pattern.compile(PATTERN_REGEX);

    @Override
    public Unit convert(String value, ConversionContext context) {
        String trimmed = requireNonNull(value).trim();
        context.addSupportedFormats(UnitConverter.class, "All Units supported by JSR 363");
        UnitFormat format = ServiceProvider.current().getUnitFormatService().getUnitFormat();

        Unit result = null;

        try {
            result = format.parse(trimmed);

        } catch (RuntimeException e) {
            result = null; // Give the next converter a change. Read the JavaDoc
                            // of convert
        }

        return result;
    }

}
