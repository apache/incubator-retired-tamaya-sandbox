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

import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConversionContext;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodConverterTest {
    /*
     * I am aware of the 'Parameterized tests' feature of JUnit but
     * decided not to use it. Oliver B. Fischer, 3th April 2015
     */

    private static PeriodConverter converter = new PeriodConverter();

    private static final PeriodFormatter FORMATTER = ISOPeriodFormat.standard();

    @Test
    public void canConvertPropertiesInAllSupportedFormats() {
        Object[][] inputResultPairs = {
             // ISO format
             {"P0007Y", FORMATTER.parsePeriod("P7Y0M0W0DT0H0M0S")},
             {"P7Y", FORMATTER.parsePeriod("P7Y0M0W0DT0H0M0S")},
             {"P7891Y", FORMATTER.parsePeriod("P7891Y0M0W0DT0H0M0S")},
             {"P7891Y", FORMATTER.parsePeriod("P7891Y0M0W0DT0H0M0S")},

             {"P1Y1M", FORMATTER.parsePeriod("P1Y1M0W0DT0H0M0S")},
             {"P1Y9M", FORMATTER.parsePeriod("P1Y9M0W0DT0H0M0S")},

             {"P1Y1D", FORMATTER.parsePeriod("P1Y0M0W1DT0H0M0S")},
             {"P1YT1S", FORMATTER.parsePeriod("P1Y0M0W0DT0H0M1S")},

             // Alternative format
             {"P0002-03-00T00:00:05", FORMATTER.parsePeriod("P2Y3M0W0DT0H0M5S")}
        };

        ConversionContext context = new ConversionContext.Builder(TypeLiteral.of(Period.class)).build();
        for (Object[] pair : inputResultPairs) {
            Period period = converter.convert((String) pair[0], context);

            assertThat(period).isNotNull().isEqualTo((Period) pair[1]);
        }
    }

    @Test
    public void invalidInputValuesResultInReturningNull() {
        String[] inputValues = {
            "P0002-03T00:00:05",
            "P0002T00:00:05",
            "P0002T00:05"
        };

        ConversionContext context = new ConversionContext.Builder(TypeLiteral.of(Period.class)).build();
        for (String input : inputValues) {
            Period period = converter.convert(input, context);

            assertThat(period).isNull();
        }
    }

    @Test
    public void allSupportedFormatsAreAddedToTheConversionContext() {
        String name = PeriodConverter.class.getSimpleName();

        ConversionContext context = new ConversionContext.Builder(TypeLiteral.of(Period.class)).build();
        converter.convert("P7Y0M0W0DT0H0M0S", context);

        assertThat(context.getSupportedFormats()).hasSize(2)
            .contains("PyYmMwWdDThHmMsS (" + name + ")", "Pyyyy-mm-ddThh:mm:ss (" + name + ")");
    }
}
