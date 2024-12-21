package com.pmi.tpd.spring.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * An mutable, enumerable, composite property source. New sources are added last (and hence resolved with lowest
 * priority).
 *
 * @author Dave Syer
 * @author Christophe Friederich
 * @since 1.0
 */
public class EnumerableCompositePropertySource extends EnumerablePropertySource<Collection<PropertySource<?>>> {

    /** list of property names. */
    private volatile String[] names;

    /**
     * Create new instance of {@link EnumerableCompositePropertySource} with the given name.
     *
     * @param name
     *            the name of this {@code EnumerableCompositePropertySource}
     * @see PropertySource
     */
    public EnumerableCompositePropertySource(final String name) {
        super(name, new LinkedHashSet<PropertySource<?>>());
    }

    @Override
    public Object getProperty(final String name) {
        for (final PropertySource<?> propertySource : getSource()) {
            final Object value = propertySource.getProperty(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String[] getPropertyNames() {
        String[] result = this.names;
        if (result == null) {
            final List<String> names = new ArrayList<String>();
            for (final PropertySource<?> source : new ArrayList<PropertySource<?>>(getSource())) {
                if (source instanceof EnumerablePropertySource) {
                    names.addAll(Arrays.asList(((EnumerablePropertySource<?>) source).getPropertyNames()));
                }
            }
            this.names = names.toArray(new String[0]);
            result = this.names;
        }
        return result;
    }

    /**
     * Adds {@link PropertySource} to this instance.
     *
     * @param source
     *            a {@link PropertySource} to add.
     */
    public void add(final PropertySource<?> source) {
        getSource().add(source);
        this.names = null;
    }

}
