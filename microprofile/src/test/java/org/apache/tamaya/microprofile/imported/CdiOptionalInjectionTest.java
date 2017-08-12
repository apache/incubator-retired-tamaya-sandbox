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

import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.tamaya.microprofile.cdi.MicroprofileCDIExtension;
import org.apache.tamaya.microprofile.cdi.MicroprofileConfigurationProducer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Verify injection of {@code Optional<T>} fields.
 *
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
@RunWith(ApplicationComposer.class)
public class CdiOptionalInjectionTest{

    @Module
    @Classes(cdi = true, value = {
            OptionalValuesBean.class,
            MicroprofileCDIExtension.class,
            MicroprofileConfigurationProducer.class
    })
    public EjbJar jar() {
        return new EjbJar("config");
    }

    private @Inject OptionalValuesBean optionalValuesBean;

    @Test
    public void testOptionalInjection() {
        Assert.assertTrue(optionalValuesBean.getIntProperty().isPresent());
        Assert.assertEquals(optionalValuesBean.getIntProperty().get(), Integer.valueOf(1234));

        Assert.assertFalse(optionalValuesBean.getNotexistingProperty().isPresent());

        Assert.assertTrue(optionalValuesBean.getStringValue().isPresent());
        Assert.assertEquals(optionalValuesBean.getStringValue().get(), "hello");
    }
}
