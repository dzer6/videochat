package com.dzer6.ga3

import java.io.IOException
import java.util.Properties

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class PropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer {

    private static final Logger log = LoggerFactory.getLogger(PropertyPlaceholderConfigurer.class)
  
    def properties = [:]

    @Override
    protected void loadProperties(final Properties props) throws IOException {
        super.loadProperties(props)
        for (final Object key : props.keySet()) {
            properties.put((String) key, props.getProperty((String) key))
        }
    }

    /**
     * Return a property loaded by the place holder.
     *
     * @param name the property name.
     * @return the property value.
     */
    def propertyMissing(String name) {
        def value = properties.get(name)
        log.debug("get property name = $name, value = $value")
        return value
    }
}