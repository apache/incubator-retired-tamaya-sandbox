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

import org.apache.tamaya.Configuration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.format.ISODateTimeFormat.dateTime;

public class FullStackIT {

    @Test
    public void retrieveJodaTimeValuesFromConfiguration() {

        Configuration configuration = Configuration.current();

        String dateTimeString = configuration.get("dateTimeValue");
        DateTime dateTimeValue = configuration.get("dateTimeValue", DateTime.class);

        assertThat(dateTimeString).isNotNull();
        assertThat(dateTimeString).isEqualTo("2010-08-08T14:00:15.5+10:00");
        assertThat(dateTimeValue).isNotNull();
        assertThat(dateTimeValue.getMillis()).isEqualTo(dateTime().parseDateTime(dateTimeString).getMillis());
    }

    @Test
    public void retrieveDateTimeZoneValueFromConfiguration() {
        Configuration configuration = Configuration.current();

        String zoneAAsString = configuration.get("dateTimeZoneValueA");
        DateTimeZone zoneA = configuration.get("dateTimeZoneValueA", DateTimeZone.class);

        assertThat(zoneAAsString).isEqualTo("UTC");
        assertThat(zoneA).isEqualTo(DateTimeZone.forID("UTC"));

        String zoneBAsString = configuration.get("dateTimeZoneValueB");
        DateTimeZone zoneB = configuration.get("dateTimeZoneValueB", DateTimeZone.class);

        assertThat(zoneBAsString).isEqualTo("+01:00");
        assertThat(zoneB).isEqualTo(DateTimeZone.forOffsetHours(1));
    }

    @Test
    public void retrievePeriodValueFromConfiguration() {
        Configuration configuration = Configuration.current();

        MutablePeriod referenceValue = new MutablePeriod();

        ISOPeriodFormat.standard().getParser().parseInto(referenceValue, "P1Y1M1W1DT1H1M1S", 0,
                                                         Locale.ENGLISH);

        String periodAsString = configuration.get("periodValueA");
        Period period = configuration.get("periodValueA", Period.class);

        assertThat(periodAsString).isEqualTo("P1Y1M1W1DT1H1M1S");
        assertThat(period).isEqualTo(referenceValue.toPeriod());
    }
}
