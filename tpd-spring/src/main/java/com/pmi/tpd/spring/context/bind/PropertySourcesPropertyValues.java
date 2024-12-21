package com.pmi.tpd.spring.context.bind;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;
import org.springframework.validation.DataBinder;

/**
 * A {@link PropertyValues} implementation backed by a {@link PropertySources}, bridging the two abstractions and
 * allowing (for instance) a regular {@link DataBinder} to be used with the latter.
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class PropertySourcesPropertyValues implements PropertyValues {

    /** */
    private static final Pattern COLLECTION_PROPERTY = Pattern.compile("\\[(\\d+)\\]");

    /** */
    private final PropertySources propertySources;

    /** */
    private final Collection<String> nonEnumerableFallbackNames;

    /** */
    private final PropertyNamePatternsMatcher includes;

    /** */
    private final Map<String, PropertyValue> propertyValues = new LinkedHashMap<>();

    /** */
    private final ConcurrentHashMap<String, PropertySource<?>> collectionOwners = new ConcurrentHashMap<>();

    /**
     * Create a new PropertyValues from the given PropertySources.
     *
     * @param propertySources
     *            a PropertySources instance
     */
    public PropertySourcesPropertyValues(final PropertySources propertySources) {
        this(propertySources, (Collection<String>) null, PropertyNamePatternsMatcher.ALL);
    }

    /**
     * Create a new PropertyValues from the given PropertySources.
     *
     * @param propertySources
     *            a PropertySources instance
     * @param includePatterns
     *            property name patterns to include from system properties and environment variables
     * @param nonEnumerableFallbackNames
     *            the property names to try in lieu of an {@link EnumerablePropertySource}.
     */
    public PropertySourcesPropertyValues(final PropertySources propertySources,
            final Collection<String> includePatterns, final Collection<String> nonEnumerableFallbackNames) {
        this(propertySources, nonEnumerableFallbackNames, new PatternPropertyNamePatternsMatcher(includePatterns));
    }

    /**
     * Create a new PropertyValues from the given PropertySources.
     *
     * @param propertySources
     *            a PropertySources instance
     * @param nonEnumerableFallbackNames
     *            the property names to try in lieu of an {@link EnumerablePropertySource}.
     * @param includes
     *            the property name patterns to include
     */
    PropertySourcesPropertyValues(final PropertySources propertySources,
            final Collection<String> nonEnumerableFallbackNames, final PropertyNamePatternsMatcher includes) {
        Assert.notNull(propertySources, "PropertySources must not be null");
        Assert.notNull(includes, "Includes must not be null");
        this.propertySources = propertySources;
        this.nonEnumerableFallbackNames = nonEnumerableFallbackNames;
        this.includes = includes;
        final PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(propertySources);
        for (final PropertySource<?> source : propertySources) {
            processPropertySource(source, resolver);
        }
    }

    private void processPropertySource(final PropertySource<?> source, final PropertySourcesPropertyResolver resolver) {
        if (source instanceof CompositePropertySource) {
            processCompositePropertySource((CompositePropertySource) source, resolver);
        } else if (source instanceof EnumerablePropertySource) {
            processEnumerablePropertySource((EnumerablePropertySource<?>) source, resolver, this.includes);
        } else {
            processNonEnumerablePropertySource(source, resolver);
        }
    }

    private void processCompositePropertySource(final CompositePropertySource source,
        final PropertySourcesPropertyResolver resolver) {
        for (final PropertySource<?> nested : source.getPropertySources()) {
            processPropertySource(nested, resolver);
        }
    }

    private void processEnumerablePropertySource(final EnumerablePropertySource<?> source,
        final PropertySourcesPropertyResolver resolver,
        final PropertyNamePatternsMatcher includes) {
        if (source.getPropertyNames().length > 0) {
            for (final String propertyName : source.getPropertyNames()) {
                if (includes.matches(propertyName)) {
                    final Object value = getEnumerableProperty(source, resolver, propertyName);
                    putIfAbsent(propertyName, value, source);
                }
            }
        }
    }

    private Object getEnumerableProperty(final EnumerablePropertySource<?> source,
        final PropertySourcesPropertyResolver resolver,
        final String propertyName) {
        try {
            return resolver.getProperty(propertyName, Object.class);
        } catch (final RuntimeException ex) {
            // Probably could not resolve placeholders, ignore it here
            return source.getProperty(propertyName);
        }
    }

    private void processNonEnumerablePropertySource(final PropertySource<?> source,
        final PropertySourcesPropertyResolver resolver) {
        // We can only do exact matches for non-enumerable property names, but
        // that's better than nothing...
        if (this.nonEnumerableFallbackNames == null) {
            return;
        }
        for (final String propertyName : this.nonEnumerableFallbackNames) {
            if (!source.containsProperty(propertyName)) {
                continue;
            }
            Object value = null;
            try {
                value = resolver.getProperty(propertyName, Object.class);
            } catch (final RuntimeException ex) {
                // Probably could not convert to Object, weird, but ignorable
            }
            if (value == null) {
                value = source.getProperty(propertyName.toUpperCase());
            }
            putIfAbsent(propertyName, value, source);
        }
    }

    @Override
    public PropertyValue[] getPropertyValues() {
        final Collection<PropertyValue> values = this.propertyValues.values();
        return values.toArray(new PropertyValue[values.size()]);
    }

    @Override
    public PropertyValue getPropertyValue(final String propertyName) {
        PropertyValue propertyValue = this.propertyValues.get(propertyName);
        if (propertyValue != null) {
            return propertyValue;
        }
        for (final PropertySource<?> source : this.propertySources) {
            final Object value = source.getProperty(propertyName);
            propertyValue = putIfAbsent(propertyName, value, source);
            if (propertyValue != null) {
                return propertyValue;
            }
        }
        return null;
    }

    private PropertyValue putIfAbsent(final String propertyName, final Object value, final PropertySource<?> source) {
        if (value != null && !this.propertyValues.containsKey(propertyName)) {
            final PropertySource<?> collectionOwner = this.collectionOwners
                    .putIfAbsent(COLLECTION_PROPERTY.matcher(propertyName).replaceAll("[]"), source);
            if (collectionOwner == null || collectionOwner == source) {
                final PropertyValue propertyValue = new OriginCapablePropertyValue(propertyName, value, propertyName,
                        source);
                this.propertyValues.put(propertyName, propertyValue);
                return propertyValue;
            }
        }
        return null;
    }

    @Override
    public PropertyValues changesSince(final PropertyValues old) {
        final MutablePropertyValues changes = new MutablePropertyValues();
        // for each property value in the new set
        for (final PropertyValue newValue : getPropertyValues()) {
            // if there wasn't an old one, add it
            final PropertyValue oldValue = old.getPropertyValue(newValue.getName());
            if (oldValue == null || !oldValue.equals(newValue)) {
                changes.addPropertyValue(newValue);
            }
        }
        return changes;
    }

    @Override
    public boolean contains(final String propertyName) {
        return getPropertyValue(propertyName) != null;
    }

    @Override
    public boolean isEmpty() {
        return this.propertyValues.isEmpty();
    }

}
