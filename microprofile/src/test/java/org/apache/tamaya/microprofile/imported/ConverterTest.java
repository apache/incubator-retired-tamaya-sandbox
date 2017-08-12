/*
 * Copyright (c) 2016-2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.tamaya.microprofile.imported;

import org.apache.tamaya.microprofile.imported.converters.Duck;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 * @author <a href="mailto:emijiang@uk.ibm.com">Emily Jiang</a>
 * @author <a href="mailto:john.d.ament@gmail.com">John D. Ament</a>
 */
public class ConverterTest {

    private Config config = ConfigProvider.getConfig();

    @Test
    public void testCustomConverter() {
        Duck namedDuck = config.getValue("tck.config.test.javaconfig.converter.duckname", Duck.class);
        Assert.assertNotNull(namedDuck);
        Assert.assertEquals(namedDuck.getName(), "Hannelore");
    }

    @Test
    public void testInteger() {
        Integer value = config.getValue("tck.config.test.javaconfig.converter.integervalue", Integer.class);
        Assert.assertEquals(value, Integer.valueOf(1234));
    }

    @Test
    public void testInt() {
        int value = config.getValue("tck.config.test.javaconfig.converter.integervalue", int.class);
        Assert.assertEquals(value, 1234);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInteger_Broken() {
        Integer value = config.getValue("tck.config.test.javaconfig.converter.integervalue.broken", Integer.class);
    }

    @Test
    public void testLong() {
        Long value = config.getValue("tck.config.test.javaconfig.converter.longvalue", Long.class);
        Assert.assertEquals(value, Long.valueOf(1234567890));
    }

    @Test
    public void testlong() {
        long primitiveValue = config.getValue("tck.config.test.javaconfig.converter.longvalue", long.class);
        Assert.assertEquals(primitiveValue, 1234567890L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLong_Broken() {
        config.getValue("tck.config.test.javaconfig.converter.longvalue.broken", Long.class);
    }

    @Test
    public void testFloat() {
        Float value = config.getValue("tck.config.test.javaconfig.converter.floatvalue", Float.class);
        Assert.assertEquals(value, 12.34f, 0.0f);
    }

    @Test
    public void testfloat() {
        float value = config.getValue("tck.config.test.javaconfig.converter.floatvalue", float.class);
        Assert.assertEquals(value, 12.34f, 0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFloat_Broken() {
        config.getValue("tck.config.test.javaconfig.converter.floatvalue.broken", Float.class);
    }

    @Test
    public void testDouble() {
        Double value = config.getValue("tck.config.test.javaconfig.converter.doublevalue", Double.class);
        Assert.assertEquals(value, 12.34d, 0.0d);
    }

    @Test
    public void testdouble() {
        double value = config.getValue("tck.config.test.javaconfig.converter.doublevalue", double.class);
        Assert.assertEquals(value,12.34d, 0d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDouble_Broken() {
        Double value = config.getValue("tck.config.test.javaconfig.converter.doublevalue.broken", Double.class);
    }

    @Test
    public void testDuration() {
        Duration value = config.getValue("tck.config.test.javaconfig.converter.durationvalue", Duration.class);
        Assert.assertEquals(value, Duration.parse("PT15M"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuration_Broken() {
        Duration value = config.getValue("tck.config.test.javaconfig.converter.durationvalue.broken", Duration.class);
    }

    @Test
    public void testLocalTime() {
        LocalTime value = config.getValue("tck.config.test.javaconfig.converter.localtimevalue", LocalTime.class);
        Assert.assertEquals(value, LocalTime.parse("10:37"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLocalTime_Broken() {
        LocalTime value = config.getValue("tck.config.test.javaconfig.converter.localtimevalue.broken", LocalTime.class);
    }

    @Test
    public void testLocalDate() {
        LocalDate value = config.getValue("tck.config.test.javaconfig.converter.localdatevalue", LocalDate.class);
        Assert.assertEquals(value, LocalDate.parse("2017-12-24"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLocalDate_Broken() {
        LocalDate value = config.getValue("tck.config.test.javaconfig.converter.localdatevalue.broken", LocalDate.class);
    }

    @Test
    public void testLocalDateTime() {
        LocalDateTime value = config.getValue("tck.config.test.javaconfig.converter.localdatetimevalue", LocalDateTime.class);
        Assert.assertEquals(value, LocalDateTime.parse("2017-12-24T10:25:30"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLocalDateTime_Broken() {
        LocalDateTime value = config.getValue("tck.config.test.javaconfig.converter.localdatetimevalue.broken", LocalDateTime.class);
    }

    @Test
    public void testOffsetDateTime() {
        OffsetDateTime value = config.getValue("tck.config.test.javaconfig.converter.offsetdatetimevalue", OffsetDateTime.class);
        Assert.assertEquals(value, OffsetDateTime.parse("2007-12-03T10:15:30+01:00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOffsetDateTime_Broken() {
        OffsetDateTime value = config.getValue("tck.config.test.javaconfig.converter.offsetdatetimevalue.broken", OffsetDateTime.class);
    }
    
    @Test
    public void testOffsetTime() {
        OffsetTime value = config.getValue("tck.config.test.javaconfig.converter.offsettimevalue", OffsetTime.class);
        OffsetTime parsed = OffsetTime.parse("13:45:30.123456789+02:00");
        Assert.assertEquals(value, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOffsetTime_Broken() {
        OffsetTime value = config.getValue("tck.config.test.javaconfig.converter.offsettimevalue.broken", OffsetTime.class);
    }
    
    @Test
    public void testInstant() {
        Instant value = config.getValue("tck.config.test.javaconfig.converter.instantvalue", Instant.class);
        Assert.assertEquals(value, Instant.parse("2015-06-02T21:34:33.616Z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstant_Broken() {
        Instant value = config.getValue("tck.config.test.javaconfig.converter.instantvalue.broken", Instant.class);
    }

    @Test
    public void testBoolean() {
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.true", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.true", boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.true_uppercase", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.true_mixedcase", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.false", Boolean.class));

        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.one", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.zero", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.seventeen", Boolean.class));

        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.yes", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.yes_uppercase", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.yes_mixedcase", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.no", Boolean.class));

        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.y", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.y_uppercase", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.n", Boolean.class));

        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.on", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.on_uppercase", Boolean.class));
        Assert.assertTrue(config.getValue("tck.config.test.javaconfig.configvalue.boolean.on_mixedcase", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.off", Boolean.class));
        Assert.assertFalse(config.getValue("tck.config.test.javaconfig.configvalue.boolean.off", boolean.class));
    }


    @Test
    public void testURLConverter() throws MalformedURLException {
        URL url = config.getValue("tck.config.test.javaconfig.converter.urlvalue", URL.class);
        Assert.assertEquals(url, new URL("http://microprofile.io"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testURLConverterBroken() throws Exception {
        URL ignored = config.getValue("tck.config.test.javaconfig.converter.urlvalue.broken", URL.class);
    }
}
