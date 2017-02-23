package org.apache.tamaya.osgi;

import org.apache.tamaya.inject.api.Config;

/**
 * Created by atsticks on 27.12.16.
 */
public class HelloImpl implements Hello{

    @Config("java.version")
    private String javaVersion;

    @Override
    public String sayHello() {
        return "Hello: " + javaVersion;
    }
}
