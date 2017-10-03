package org.apache.tamaya.osgi.injection;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by atsti on 03.10.2017.
 */
final class OSGIConfigurationInjector{

    private ConfigurationAdmin cm;
    private Configuration tamayaOSGIConfiguration;
    private String pid;
    private String location;


    public OSGIConfigurationInjector(ConfigurationAdmin cm, String pid){
        this(cm, pid, null);
    }

    public OSGIConfigurationInjector(ConfigurationAdmin cm, String pid, String location){
        this.cm = Objects.requireNonNull(cm);
        tamayaOSGIConfiguration = ConfigurationProvider.createConfiguration(
                ConfigurationProvider.getConfigurationContextBuilder()
                .addDefaultPropertyConverters()
                .addDefaultPropertyFilters()
                .addPropertySources(new OSGIConfigAdminPropertySource(cm, pid, location))
                .build());
    }

    public <T> T configure(T instance){
        return ConfigurationInjection.getConfigurationInjector()
                .configure(instance, tamayaOSGIConfiguration);
    }

    public <T> Supplier<T> getConfiguredSupplier(java.util.function.Supplier<T> supplier){
        return ConfigurationInjection.getConfigurationInjector()
                .getConfiguredSupplier(supplier, tamayaOSGIConfiguration);
    }

    /**
     * Creates a template implementing the annotated methods based on current configuration data.
     *
     * @param <T> the type of the template.
     * @param templateType the type of the template to be created.
     * @return the configured template.
     */
    public <T> T createTemplate(Class<T> templateType){
        return ConfigurationInjection.getConfigurationInjector()
                .createTemplate(templateType, tamayaOSGIConfiguration);
    }
}
