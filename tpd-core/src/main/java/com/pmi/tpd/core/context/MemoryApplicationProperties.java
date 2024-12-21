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
package com.pmi.tpd.core.context;

import static java.util.Optional.ofNullable;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.ApplicationConstants.PropertyKeys;
import com.pmi.tpd.api.context.IApplicationProperties;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class MemoryApplicationProperties implements IApplicationProperties {

    /** */
    private final Map<String, String> map;

    /**
     * Create new empty instance of {@link MemoryApplicationProperties}.
     */
    public MemoryApplicationProperties() {
        map = Maps.newConcurrentMap();
    }

    /**
     * Create new instance of {@link MemoryApplicationProperties} populate with {@code values}.
     *
     * @param values
     *            list a property values.
     */
    public MemoryApplicationProperties(final Map<String, String> values) {
        map = Maps.newConcurrentMap();
        map.putAll(values);
    }

    /**
     * Create new instance of {@link MemoryApplicationProperties} populate with {@code propertySources}.
     *
     * @param propertySources
     *            container of one or more {@link PropertySource}.
     */
    public MemoryApplicationProperties(final PropertySources propertySources) {
        map = Maps.newConcurrentMap();
        for (final PropertySource<?> propertySource : propertySources) {
            final Object source = propertySource.getSource();
            if (source instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> val = (Map<String, Object>) source;
                for (final Entry<String, Object> entry : val.entrySet()) {
                    map.put(entry.getKey(), entry.getValue().toString());
                }

            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getText(final String name) {
        return ofNullable(map.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getDefaultBackedText(final String name) {
        return ofNullable(map.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public void setText(final String name, final String value) {
        map.put(name, value);

    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getString(final String name) {
        return ofNullable(map.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getDefaultKeys() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getDefaultBackedString(final String name) {
        return ofNullable(map.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getDefaultBackedString(final String name, final String defaultValue) {
        return ofNullable(getDefaultBackedString(name).orElse(defaultValue));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getDefaultString(final String name) {
        return ofNullable(map.get(name));
    }

    /** {@inheritDoc} */
    @Override
    public void setString(final String name, final String value) {
        map.put(name, value);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Boolean> getOption(final String key) {
        return getString(key).map(value -> Boolean.valueOf(value));
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getKeys() {
        return map.keySet().stream().collect(Collectors.toUnmodifiableList());
    }

    /** {@inheritDoc} */
    @Override
    public void setOption(final String key, final boolean value) {
        map.put(key, Boolean.toString(value));
    }

    /** {@inheritDoc} */
    @Override
    public String getEncoding() {
        return "UTF-8";
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {

    }

    /** {@inheritDoc} */
    @Override
    public Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getKeysWithPrefix(final String prefix) {
        final Set<String> keys = Sets.newHashSet();
        for (final String key : map.keySet()) {
            if (key.startsWith(prefix)) {
                keys.add(key);
            }
        }
        return keys.stream().collect(Collectors.toUnmodifiableList());
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> temp = Maps.newHashMap();
        temp.putAll(map);
        return temp;
    }

    @Override
    public void setSetup(final boolean setup) {
        setString(PropertyKeys.SETUP_PROPERTY, Boolean.toString(setup));
    }

    @Override
    public boolean isAutoSetup() {
        return getDefaultBackedString(PropertyKeys.AUTO_SETUP_PROPERTY).map((value) -> "true".equals(value))
                .orElse(false);
    }

    @Override
    public boolean isSetup() {
        return getString(PropertyKeys.SETUP_PROPERTY).map((value) -> "true".equals(value)).orElse(false);
    }

    @Override
    public boolean isBootstrapped() {
        return isSetup();
    }

    @Override
    public Optional<URI> getBaseUrl() {
        return getString(ApplicationConstants.Setup.SETUP_BASE_URL).map((uri) -> URI.create(uri));
    }

    @Override
    public void setBaseURL(final URI uri) {
        setString(ApplicationConstants.Setup.SETUP_BASE_URL, uri.toString());
    }

    @Override
    public Optional<String> getDisplayName() {
        return getString(ApplicationConstants.Setup.SETUP_DISPLAY_NAME);
    }

    @Override
    public void setDisplayName(final String displayName) {
        setString(ApplicationConstants.Setup.SETUP_DISPLAY_NAME, displayName);
    }

    @Override
    public void setAvatarSource(final String avatarSource) {
        setString(ApplicationConstants.PropertyKeys.AVATAR_SOURCE, avatarSource);
    }

    @Override
    public Optional<String> getAvatarSource() {
        return getString(ApplicationConstants.PropertyKeys.AVATAR_SOURCE);
    }

    @Override
    public <T> T getConfiguration(final Class<T> configurationClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void storeConfiguration(final T configurationProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void removeConfiguration(final Class<T> configurationClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(final String key) {
        return this.map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAsActualType(final String key) {
        return (T) this.map.get(key);
    }

}
