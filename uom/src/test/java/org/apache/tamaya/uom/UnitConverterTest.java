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

import javax.measure.Unit;

import org.apache.tamaya.TypeLiteral;
import org.apache.tamaya.spi.ConversionContext;
import org.junit.Test;

import tec.units.ri.unit.Units;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitConverterTest {
    private UnitConverter converter = new UnitConverter();

    @Test
    public void canConvertUnitInformation() {

        ConversionContext context = new ConversionContext.Builder(TypeLiteral.of(Unit.class)).build();
        Unit<?> unit = converter.convert("m", context);

        assertThat(unit).isNotNull();
        assertThat(unit).isEqualTo(Units.METRE);
    }

}
