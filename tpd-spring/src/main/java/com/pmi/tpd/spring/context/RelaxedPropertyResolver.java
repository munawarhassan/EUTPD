/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.spring.context;

import static java.lang.String.format;

import java.util.Map;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.core.env.PropertyResolver} that attempts to resolve values using
 * {@link com.pmi.tpd.spring.context.RelaxedNames}.
 *
 * @author devacfr
 * @since 1.0
 */
public class RelaxedPropertyResolver implements PropertyResolver {

    /** */
    private final PropertyResolver resolver;

    /** */
    private final String prefix;

    /**
     * <p>
     * Constructor for RelaxedPropertyResolver.
     * </p>
     *
     * @param resolver
     *            a {@link org.springframework.core.env.PropertyResolver} object.
     */
    public RelaxedPropertyResolver(final PropertyResolver resolver) {
        this(resolver, null);
    }

    /**
     * <p>
     * Constructor for RelaxedPropertyResolver.
     * </p>
     *
     * @param resolver
     *            a {@link org.springframework.core.env.PropertyResolver} object.
     * @param prefix
     *            a {@link java.lang.String} object.
     */
    public RelaxedPropertyResolver(final PropertyResolver resolver, final String prefix) {
        Assert.notNull(resolver, "PropertyResolver must not be null");
        this.resolver = resolver;
        this.prefix = prefix == null ? "" : prefix;
    }

    /** {@inheritDoc} */
    @Override
    public String getRequiredProperty(final String key) throws IllegalStateException {
        return getRequiredProperty(key, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getRequiredProperty(final String key, final Class<T> targetType) throws IllegalStateException {
        final T value = getProperty(key, targetType);
        Assert.state(value != null, format("required key [%s] not found", key));
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String getProperty(final String key) {
        return getProperty(key, String.class, null);
    }

    /** {@inheritDoc} */
    @Override
    public String getProperty(final String key, final String defaultValue) {
        return getProperty(key, String.class, defaultValue);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getProperty(final String key, final Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getProperty(final String key, final Class<T> targetType, final T defaultValue) {
        final RelaxedNames prefixes = new RelaxedNames(this.prefix);
        final RelaxedNames keys = new RelaxedNames(key);
        for (final String prefix : prefixes) {
            for (final String relaxedKey : keys) {
                if (this.resolver.containsProperty(prefix + relaxedKey)) {
                    return this.resolver.getProperty(prefix + relaxedKey, targetType);
                }
            }
        }
        return defaultValue;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsProperty(final String key) {
        final RelaxedNames prefixes = new RelaxedNames(this.prefix);
        final RelaxedNames keys = new RelaxedNames(key);
        for (final String prefix : prefixes) {
            for (final String relaxedKey : keys) {
                if (this.resolver.containsProperty(prefix + relaxedKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String resolvePlaceholders(final String text) {
        throw new UnsupportedOperationException("Unable to resolve placeholders with relaxed properties");
    }

    /** {@inheritDoc} */
    @Override
    public String resolveRequiredPlaceholders(final String text) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Unable to resolve placeholders with relaxed properties");
    }

    /**
     * Return a Map of all values from all underlying properties that start with the specified key. NOTE: this method
     * can only be used if the underlying resolver is a {@link org.springframework.core.env.ConfigurableEnvironment}.
     *
     * @param keyPrefix
     *            the key prefix used to filter results
     * @return a map of all sub properties starting with the specified key prefix.
     * @see PropertySourceUtils#getSubProperties(org.springframework.core.env.PropertySources, String)
     * @see PropertySourceUtils#getSubProperties(org.springframework.core.env.PropertySources, String, String)
     */
    public Map<String, Object> getSubProperties(final String keyPrefix) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, this.resolver, "SubProperties not available.");
        final ConfigurableEnvironment env = (ConfigurableEnvironment) this.resolver;
        return PropertySourceUtils.getSubProperties(env.getPropertySources(), this.prefix, keyPrefix);
    }

}
