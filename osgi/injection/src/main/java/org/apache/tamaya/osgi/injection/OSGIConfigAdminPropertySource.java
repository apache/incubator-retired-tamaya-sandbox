package org.apache.tamaya.osgi.injection;

import org.apache.tamaya.spi.PropertySource;
import org.apache.tamaya.spi.PropertyValue;
import org.apache.tamaya.spisupport.BasePropertySource;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a Tamaya PropertySource, which internally wraps the OSGI ConfigAdmin service, preconfigured
 * for a PID and (optionally) location.
 */
public class OSGIConfigAdminPropertySource extends BasePropertySource{

    private static final Logger LOG = Logger.getLogger(OSGIConfigAdminPropertySource.class.getName());
    private ConfigurationAdmin configurationAdmin;
    private String pid;
    private String location;

    public OSGIConfigAdminPropertySource(ConfigurationAdmin configurationAdmin, String pid){
        this.configurationAdmin = Objects.requireNonNull(configurationAdmin);
        this.pid = Objects.requireNonNull(pid);
    }

    public OSGIConfigAdminPropertySource(ConfigurationAdmin configurationAdmin, String pid, String location){
        this.configurationAdmin = Objects.requireNonNull(configurationAdmin);
        this.pid = Objects.requireNonNull(pid);
        this.location = location;
    }

    /**
     * Get the configured OSGI service PID.
     * @return the pid, nnever null.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Get the configured OSGI config location, may be null.
     * @return the location, or null.
     */
    public String getLocation() {
        return location;
    }

    @Override
    public PropertyValue get(String key) {
        try {
            Configuration osgiConfig = configurationAdmin.getConfiguration(pid, location);
            Dictionary<String,Object> props = osgiConfig.getProperties();
            if(props!=null){
                Object value = props.get(key);
                if(value!=null) {
                    return PropertyValue.of(key, String.valueOf(value), "OSGI ConfigAdmin: " + pid);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.FINEST,  e, () -> "No config for PID: " + pid);
        }
        return null;
    }

    @Override
    public Map<String, PropertyValue> getProperties() {
        try {
            Configuration osgiConfig = configurationAdmin.getConfiguration(pid);
            Dictionary<String,Object> props = osgiConfig.getProperties();
            if(props!=null){
                Map<String, PropertyValue> result = new HashMap<>();
                Enumeration<String> keys = props.keys();
                while(keys.hasMoreElements()){
                    String key = keys.nextElement();
                    Object value = props.get(key);
                    result.put(key, PropertyValue.of(key, String.valueOf(value), "OSGI ConfigAdmin: " + pid));
                }
                return result;
            }
        } catch (IOException e) {
            LOG.log(Level.FINEST,  e, () -> "No config for PID: " + pid);
        }
        return Collections.emptyMap();
    }
}


