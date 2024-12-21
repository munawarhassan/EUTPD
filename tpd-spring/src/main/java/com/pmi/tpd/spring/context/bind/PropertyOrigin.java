package com.pmi.tpd.spring.context.bind;

import org.springframework.core.env.PropertySource;

/**
 * The origin of a property, specifically its source and its name before any prefix was removed.
 *
 * @author Andy Wilkinson
 * @since 1.3.0
 */
public class PropertyOrigin {

    /** */
    private final PropertySource<?> source;

    /** */
    private final String name;

    PropertyOrigin(final PropertySource<?> source, final String name) {
        this.name = name;
        this.source = source;
    }

    public PropertySource<?> getSource() {
        return this.source;
    }

    public String getName() {
        return this.name;
    }

}
