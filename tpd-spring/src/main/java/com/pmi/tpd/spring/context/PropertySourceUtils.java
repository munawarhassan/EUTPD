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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

/**
 * Convenience class for manipulating PropertySources.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class PropertySourceUtils {

    private PropertySourceUtils() {
    }

    /**
     * Return a Map of all values from the specified {@link org.springframework.core.env.PropertySources} that start
     * with a particular key.
     *
     * @param propertySources
     *            the property sources to scan
     * @param keyPrefix
     *            the key prefixes to test
     * @return a map of all sub properties starting with the specified key prefixes.
     * @see PropertySourceUtils#getSubProperties(PropertySources, String, String)
     */
    public static Map<String, Object> getSubProperties(final PropertySources propertySources, final String keyPrefix) {
        return PropertySourceUtils.getSubProperties(propertySources, null, keyPrefix);
    }

    /**
     * Return a Map of all values from the specified {@link org.springframework.core.env.PropertySources} that start
     * with a particular key.
     *
     * @param propertySources
     *            the property sources to scan
     * @param rootPrefix
     *            a root prefix to be prepended to the keyPrefex (can be {@code null})
     * @param keyPrefix
     *            the key prefixes to test
     * @return a map of all sub properties starting with the specified key prefixes.
     * @see #getSubProperties(PropertySources, String, String)
     */
    public static Map<String, Object> getSubProperties(final PropertySources propertySources,
        final String rootPrefix,
        final String keyPrefix) {
        final RelaxedNames keyPrefixes = new RelaxedNames(keyPrefix);
        final Map<String, Object> subProperties = new LinkedHashMap<String, Object>();
        for (final PropertySource<?> source : propertySources) {
            if (source instanceof EnumerablePropertySource) {
                for (final String name : ((EnumerablePropertySource<?>) source).getPropertyNames()) {
                    final String key = PropertySourceUtils.getSubKey(name, rootPrefix, keyPrefixes);
                    if (key != null && !subProperties.containsKey(key)) {
                        subProperties.put(key, source.getProperty(name));
                    }
                }
            }
        }
        return Collections.unmodifiableMap(subProperties);
    }

    private static String getSubKey(final String name, String rootPrefixes, final RelaxedNames keyPrefix) {
        rootPrefixes = rootPrefixes == null ? "" : rootPrefixes;
        for (final String rootPrefix : new RelaxedNames(rootPrefixes)) {
            for (final String candidateKeyPrefix : keyPrefix) {
                if (name.startsWith(rootPrefix + candidateKeyPrefix)) {
                    return name.substring((rootPrefix + candidateKeyPrefix).length());
                }
            }
        }
        return null;
    }

}
