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
package com.pmi.tpd.api.context;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.annotation.ConfigurationProperties;

/**
 * This can be used to lookup application properties. This uses a two stage strategy for finding property values. First
 * the database is checked to see if a value exists. If it doesn't exists, it falls back to the application properties
 * file for a value. Once a key is placed in the database (via an upgrade task or UI interaction) then it will always be
 * loaded from the database. NOTE : Be very careful with boolean property values. Because of the way OSPropertySets
 * work, its impossible to distinguish between properties that have a false value and properties that have NO value.
 * Therefore it is usually better to have a "String" property set to the value "true" or "false" and then use
 * Boolean.valueOf() in it. This way its possible detects the absence of a property value from it being set to false.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IApplicationProperties {

    /** */
    static final String APPLICATION_PROPERTIES = "applicationProperties";

    /**
     * @param setup
     * @since 1.3
     */
    void setSetup(boolean setup);

    /**
     * @return
     * @since 1.3
     */
    boolean isSetup();

    /**
     * @return
     * @since 1.3
     */
    boolean isAutoSetup();

    /**
     * @return
     */
    boolean isBootstrapped();

    /**
     * @return
     * @since 1.3
     */
    Optional<String> getDisplayName();

    /**
     * @param displayNameConfig
     * @since 1.3
     */
    void setDisplayName(String displayName);

    /**
     * @return
     * @since 1.3
     */
    Optional<URI> getBaseUrl();

    /**
     * @param uri
     * @since 1.3
     */
    void setBaseURL(URI uri);

    /**
     * Sets the identifier of the configured {@link ApplicationConstants.PropertyKeys.AVATAR_SOURCE}, which will be used
     * on subsequent requests (and by default on subsequent executions) to provide avatar URLs.
     *
     * @param avatarSource
     *                     the new avatar source, which may be {@code null} to use the default
     * @since 2.4
     */
    void setAvatarSource(final String avatarSource);

    /**
     * Retrieves the identifier of the configured {@link IAvatarSource AvatarSource}.
     *
     * @return the configured avatar source, which may be {@code null} if one has not been configured explicitly
     * @since 2.4
     */
    Optional<String> getAvatarSource();

    /**
     * @return Returns {@code true} Whether the specified key is present in the backing PropertySet. Typically called
     *         before {@link #getOption(String)}
     */
    boolean exists(final String key);

    /**
     * <p>
     * getText.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    Optional<String> getText(String name);

    /**
     * <p>
     * Get the property from the application properties, but if not found, try to get from the default properties file.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    Optional<String> getDefaultBackedText(String name);

    /**
     * <p>
     * setText.
     * </p>
     *
     * @param name
     *              a {@link java.lang.String} object.
     * @param value
     *              a {@link java.lang.String} object.
     */
    void setText(String name, String value);

    /**
     * <p>
     * Gets property value from persistent storage if exists.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} proprety name to use.
     * @return a {@link java.lang.String} representing value of property name from persistent storage.
     */
    Optional<String> getString(@Nonnull String name);

    /**
     * <p>
     * Gets a collection of all default property keys.
     * </p>
     *
     * @return a {@link java.util.Collection} representing collection of all default property keys.
     */
    Collection<String> getDefaultKeys();

    /**
     * <p>
     * Gets the property from the application properties, but if not found, try to get from the default properties file.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} property name to use.
     * @return a {@link java.lang.String} representing the property from the application properties, but if not found,
     *         try to get from the default properties file.
     */
    Optional<String> getDefaultBackedString(@Nonnull String name);

    /**
     * <p>
     * Gets the property from the application properties, but if not found, try to get from the default properties file.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} property name to use.
     * @return a {@link java.lang.String} representing the property from the application properties, but if not found,
     *         try to get from the default properties file.
     * @since 2.2
     */
    Optional<String> getDefaultBackedString(final String name, final String defaultValue);

    /**
     * <p>
     * getDefaultString.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    Optional<String> getDefaultString(String name);

    /**
     * <p>
     * setString.
     * </p>
     *
     * @param name
     *              a {@link java.lang.String} object.
     * @param value
     *              a {@link java.lang.String} object.
     */
    void setString(String name, String value);

    /**
     * <p>
     * getOption.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    Optional<Boolean> getOption(String key);

    /**
     * @param key
     * @return
     */
    <T> T getAsActualType(final String key);

    /**
     * <p>
     * getKeys.
     * </p>
     *
     * @return a {@link java.util.Collection} object.
     */
    List<String> getKeys();

    /**
     * <p>
     * setOption.
     * </p>
     *
     * @param key
     *              a {@link java.lang.String} object.
     * @param value
     *              a boolean.
     */
    void setOption(String key, boolean value);

    /**
     * <p>
     * getEncoding.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getEncoding();

    /**
     * <p>
     * refresh.
     * </p>
     */
    void refresh();

    /**
     * <p>
     * getDefaultLocale.
     * </p>
     *
     * @return a {@link java.util.Locale} object.
     */
    Locale getDefaultLocale();

    /**
     * <p>
     * List all keys starting with supplied prefix of certain type.
     * </p>
     *
     * @param prefix
     *               a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<String> getKeysWithPrefix(String prefix);

    /**
     * This will return all application and typed values. For example if the property is a boolean then a Boolean object
     * will be returned. If an application property has a null value, then the key will still be in the
     * {@link java.util.Map#keySet()}
     *
     * @return a map of key to actual value object
     */
    Map<String, Object> asMap();

    /**
     * create and bind configuration object with environment properties
     * <p>
     * Note: the configuration class must be annotated with {@link ConfigurationProperties}
     * </p>
     *
     * @param configurationClass
     *                           configuration properties class to use.
     * @return Return new instance.
     * @see ConfigurationProperties
     * @since 2.2
     */
    @Nonnull
    <T> T getConfiguration(Class<T> configurationClass);

    /**
     * Persits all properties associated to {@code configuration properties}.
     *
     * @param configurationProperties
     *                                configuration properties to use.
     * @see ConfigurationProperties
     * @since 2.2
     */
    <T> void storeConfiguration(@Nonnull T configurationProperties);

    /**
     * Remove all properties associated to {@code configuration properties}.
     *
     * @param configurationClass
     *                           configuration properties class to use.
     * @see ConfigurationProperties
     * @since 2.2
     */
    <T> void removeConfiguration(@Nonnull final Class<T> configurationClass);
}
