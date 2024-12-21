package com.pmi.tpd.spring.context;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.config.PropertiesFactoryBean;

import com.google.common.collect.Sets;

/**
 * An extension of the Spring {@code PropertiesFactoryBean} which uses system properties to override matching entries in
 * the factory-created {@code Properties} instance.
 * <p>
 * This class emulates the behaviour of Spring's {@code PropertyPlaceholderConfigurer} when {@code systemPropertiesMode}
 * is set to {@code SYSTEM_PROPERTIES_MODE_OVERRIDE}. The difference between the two is that the configurer can only be
 * used to perform the override in conjunction with {@code &#064;Value} annotated or XML-wired properties where this can
 * be used when injecting a {@code Properties} instance and querying it for properties.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class SystemOverridePropertiesFactoryBean extends PropertiesFactoryBean {

    public SystemOverridePropertiesFactoryBean() {
    }

    @Override
    protected Properties createProperties() throws IOException {
        // Build the properties for the factory as normal
        final Properties properties = super.createProperties();
        // Get the system properties
        final Properties systemProperties = System.getProperties();

        // Compute the intersection of their keys, resulting in a set containing only the keys that are provided both
        // as system properties and factory properties
        final Set<Object> keys = properties.keySet();
        final Set<Object> systemKeys = Sets.newHashSet(systemProperties.keySet());
        systemKeys.retainAll(keys);

        // Overwrite the factory properties with system properties for each key in the intersecting set
        for (final Object key : systemKeys) {
            properties.put(key, systemProperties.get(key));
        }

        return new PlaceholderResolvingProperties(properties);
    }
}
