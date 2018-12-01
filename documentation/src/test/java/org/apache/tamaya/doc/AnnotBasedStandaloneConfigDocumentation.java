package org.apache.tamaya.doc;

import org.apache.tamaya.doc.annot.ConfigAreaSpec;
import org.apache.tamaya.doc.annot.ConfigPropertySpec;
import org.apache.tamaya.doc.annot.ConfigSpec;
import org.apache.tamaya.inject.api.Config;
import org.apache.tamaya.inject.api.ConfigDefaultSections;
import org.apache.tamaya.spi.PropertyValue;

import java.net.URL;

@ConfigSpec(
        name="Test",
        version="0.1.0",
        description = "Tomcat Configuration based on Tamaya"
)
@ConfigAreaSpec(
        path = "kubernetes",
        description = "Kubernetes Settings",
        areaType = PropertyValue.ValueType.MAP,
        max = 1
)
@ConfigAreaSpec(
        path = "kubernetes.security",
        description = "Kubernetes Security Settings",
        areaType = PropertyValue.ValueType.MAP,
        max = 1
)
@ConfigAreaSpec(
        path = "kubernetes.cluster",
        description = "Kubernetes Cluster Options",
        areaType = PropertyValue.ValueType.MAP,
        max = 1
)
@ConfigAreaSpec(
        path = "<root>",
        description = "Main Options",
        areaType = PropertyValue.ValueType.MAP,
        properties = {
                @ConfigPropertySpec(name="log", description ="Log the server startup in detail, default: false.",
                        valueType = Boolean.class),
                @ConfigPropertySpec(name="refresh", description = "Refresh interval in millis, default: 1000ms",
                        valueType = Long.class),
        }
)
public interface AnnotBasedStandaloneConfigDocumentation {

    @ConfigAreaSpec(
            description = "Tomcat Server Endpoints",
            min = 1,
            areaType = PropertyValue.ValueType.ARRAY)
    @ConfigDefaultSections("servers")
    class Server{
        @ConfigPropertySpec(description = "The server name.")
        @Config(required = true)
        private String name;
        @ConfigPropertySpec(description = "The server url.")
        @Config(required = true)
        private URL url;
    }

}
