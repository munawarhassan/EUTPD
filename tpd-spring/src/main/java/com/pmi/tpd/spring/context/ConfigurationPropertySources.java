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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.pmi.tpd.spring.env.EnumerableCompositePropertySource;

/**
 * Holds the configuration {@link PropertySource}s as they are loaded can relocate them once configuration classes have
 * been processed.
 */
class ConfigurationPropertySources extends EnumerablePropertySource<Collection<PropertySource<?>>> {

    /** Specific name of this property source. */
    private static final String NAME = "applicationConfigurationProperties";

    /** list of associated property sources. */
    private final Collection<PropertySource<?>> sources;

    /** array of all property sourc names. */
    private final String[] names;

    /**
     * <p>
     * Constructor for ConfigurationPropertySources.
     * </p>
     *
     * @param sources
     *            a {@link java.util.Collection} object.
     */
    ConfigurationPropertySources(final Collection<PropertySource<?>> sources) {
        super(NAME, sources);
        this.sources = sources;
        final List<String> names = new ArrayList<String>();
        for (final PropertySource<?> source : sources) {
            if (source instanceof EnumerablePropertySource) {
                names.addAll(Arrays.asList(((EnumerablePropertySource<?>) source).getPropertyNames()));
            }
        }
        this.names = names.toArray(new String[names.size()]);
    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(final String name) {
        for (final PropertySource<?> propertySource : this.sources) {
            final Object value = propertySource.getProperty(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * <p>
     * finishAndRelocate.
     * </p>
     *
     * @param propertySources
     *            a {@link org.springframework.core.env.MutablePropertySources} object.
     */
    public static void finishAndRelocate(final MutablePropertySources propertySources) {
        final ConfigurationPropertySources removed = //
        (ConfigurationPropertySources) propertySources.get(ConfigurationPropertySources.NAME);
        String name = ConfigurationPropertySources.NAME;
        if (removed != null) {
            for (final PropertySource<?> propertySource : removed.sources) {
                if (propertySource instanceof EnumerableCompositePropertySource) {
                    final EnumerableCompositePropertySource composite = //
                    (EnumerableCompositePropertySource) propertySource;
                    for (final PropertySource<?> nested : composite.getSource()) {
                        propertySources.addAfter(name, nested);
                        name = nested.getName();
                    }
                } else {
                    propertySources.addAfter(name, propertySource);
                }
            }
            propertySources.remove(ConfigurationPropertySources.NAME);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String[] getPropertyNames() {
        return Arrays.copyOf(this.names, this.names.length);
    }

}
