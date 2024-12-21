package com.pmi.tpd.spring.context;

import java.util.Properties;

import org.springframework.util.PropertyPlaceholderHelper;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class PlaceholderResolvingProperties extends Properties {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final PropertyPlaceholderHelper propertyPlaceholderHelper;

    /** */
    private final PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver;

    /**
     * @param defaults
     */
    public PlaceholderResolvingProperties(final Properties defaults) {
        super(defaults);

        propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
        placeholderResolver = new PropertyPlaceholderHelper.PlaceholderResolver() {

            @Override
            public String resolvePlaceholder(final String key) {
                return defaults.getProperty(key);
            }
        };
    }

    /**
     * Parses a property and returns the resolved value of the property.
     *
     * @param key
     *            the property key
     * @return the result of resolving the property value's placeholders.
     */
    @Override
    public String getProperty(final String key) {
        String value = super.getProperty(key);
        if (value != null) {
            value = propertyPlaceholderHelper.replacePlaceholders(value, placeholderResolver);
        }

        return value;
    }
}
