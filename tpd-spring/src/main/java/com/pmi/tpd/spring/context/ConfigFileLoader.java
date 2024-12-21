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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmi.tpd.spring.env.PropertySourcesLoader;

/**
 * <p>
 * ConfigFileLoader class.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public class ConfigFileLoader {

    // Note the order is from least to most specific (last one wins)
    /** default search location. */
    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/";

    /** Default configuration file name. */
    private static final String DEFAULT_NAMES = "application";

    /** Property name referencing active profiles (delimited with ',' separator). */
    public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";

    /** Property name referencing unconditional active profiles (delimited with ',' separator). */
    public static final String INCLUDE_PROFILES_PROPERTY = "spring.profiles.include";

    /** Property name referencing the name of configuration file. */
    public static final String CONFIG_NAME_PROPERTY = "spring.config.name";

    /** Property name referencing the location of configuration file. */
    public static final String CONFIG_LOCATION_PROPERTY = "spring.config.location";

    /** The associated environment used. */
    private final ConfigurableEnvironment environment;

    /** the resource loader used, can be specific or use the default {@link DefaultResourceLoader}. */
    private final ResourceLoader resourceLoader;

    /** Used to load property source. */
    private PropertySourcesLoader propertiesLoader;

    /** List of all profiles to load. */
    private Queue<String> profiles;

    /** flag allowing load once only active profiles. */
    private boolean activatedProfiles;

    /** specific search locations. */
    private String searchLocations;

    /** list of configuration file to load (delimited with ',' separator). */
    private String names;

    /**
     * <p>
     * load.
     * </p>
     *
     * @param environment
     *            a {@link org.springframework.core.env.ConfigurableEnvironment} object.
     * @param RESOURCE_LOADER
     *            a {@link org.springframework.core.io.ResourceLoader} object.
     * @throws java.io.IOException
     *             if any.
     */
    public static void load(final ConfigurableEnvironment environment, final ResourceLoader resourceLoader)
            throws IOException {
        final ConfigFileLoader loader = new ConfigFileLoader(environment, resourceLoader);
        loader.setSearchLocations("config/");
        loader.setSearchNames("application");
        loader.load();
    }

    /**
     * <p>
     * load.
     * </p>
     *
     * @param environment
     *            a {@link org.springframework.core.env.ConfigurableEnvironment} object.
     * @throws java.io.IOException
     *             if any.
     */
    public static void load(final ConfigurableEnvironment environment) throws IOException {
        load(environment, null);
    }

    /**
     * <p>
     * Constructor for ConfigFileLoader.
     * </p>
     *
     * @param environment
     *            a {@link org.springframework.core.env.ConfigurableEnvironment} object.
     * @param RESOURCE_LOADER
     *            a {@link org.springframework.core.io.ResourceLoader} object.
     */
    public ConfigFileLoader(final ConfigurableEnvironment environment, final ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader == null ? new DefaultResourceLoader() : resourceLoader;
    }

    /**
     * Set the search locations that will be considered as a comma-separated list. Each search location should be a
     * directory path (ending in "/") and it will be prefixed by the file names constructed from
     * {@link #setSearchNames(String) search names} and profiles (if any) plus file extensions supported by the
     * properties loaders. Locations are considered in the order specified, with later items taking precedence (like a
     * map merge).
     *
     * @param locations
     *            the search locations
     */
    public void setSearchLocations(final String locations) {
        Assert.hasLength(locations, "Locations must not be empty");
        this.searchLocations = locations;
    }

    /**
     * Sets the names of the files that should be loaded (excluding file extension) as a comma-separated list.
     *
     * @param names
     *            the names to load
     */
    public void setSearchNames(final String names) {
        Assert.hasLength(names, "Names must not be empty");
        this.names = names;
    }

    /**
     * <p>
     * load.
     * </p>
     *
     * @throws java.io.IOException
     *             if any.
     */
    public void load() throws IOException {
        this.propertiesLoader = new PropertySourcesLoader();
        this.profiles = Collections.asLifoQueue(new LinkedList<String>());
        this.activatedProfiles = false;
        if (this.environment.containsProperty(ACTIVE_PROFILES_PROPERTY)) {
            // Any pre-existing active profiles set via property sources (e.g. System
            // properties) take precedence over those added in config files.
            maybeActivateProfiles(this.environment.getProperty(ACTIVE_PROFILES_PROPERTY));
        } else {
            // Pre-existing active profiles set via Environment.setActiveProfiles()
            // are additional profiles and config files are allowed to add more if
            // they want to, so don't call addActiveProfiles() here.
            final List<String> list = new ArrayList<>(Arrays.asList(this.environment.getActiveProfiles()));
            // Reverse them so the order is the same as from getProfilesForValue()
            // (last one wins when properties are eventually resolved)
            Collections.reverse(list);
            this.profiles.addAll(list);
        }

        // The default profile for these purposes is represented as null. We add it
        // last so that it is first out of the queue (active profiles will then
        // override any settings in the defaults when the list is reversed later).
        this.profiles.add(null);

        while (!this.profiles.isEmpty()) {
            final String profile = this.profiles.poll();
            for (final String location : getSearchLocations()) {
                if (!location.endsWith("/")) {
                    // location is a filename already, so don't search for more
                    // filenames
                    load(location, null, profile);
                } else {
                    for (final String name : getSearchNames()) {
                        load(location, name, profile);
                    }
                }
            }
        }

        addConfigurationProperties(this.propertiesLoader.getPropertySources());
    }

    private void load(final String location, final String name, final String profile) throws IOException {
        final String group = "profile=" + (profile == null ? "" : profile);
        if (!StringUtils.hasText(name)) {
            // Try to load directly from the location
            loadIntoGroup(group, location, profile);
        } else {
            // Search for a file with the given name
            for (final String ext : this.propertiesLoader.getAllFileExtensions()) {
                if (profile != null) {
                    // Try the profile specific file
                    loadIntoGroup(group, location + name + "-" + profile + "." + ext, null);
                    // Sometimes people put "spring.profiles: dev" in
                    // application-dev.yml (gh-340). Arguably we should try and error
                    // out on that, but we can be kind and load it anyway.
                    loadIntoGroup(group, location + name + "-" + profile + "." + ext, profile);
                }
                // Also try the profile specific section (if any) of the normal file
                loadIntoGroup(group, location + name + "." + ext, profile);
            }
        }
    }

    private PropertySource<?> loadIntoGroup(final String identifier, final String location, final String profile)
            throws IOException {
        final Resource resource = this.resourceLoader.getResource(location);
        PropertySource<?> propertySource = null;
        if (resource != null) {
            final String name = "applicationConfig: [" + location + "]";
            final String group = "applicationConfig: [" + identifier + "]";
            propertySource = this.propertiesLoader.load(resource, group, name, profile);
            if (propertySource != null) {
                maybeActivateProfiles(propertySource.getProperty(ACTIVE_PROFILES_PROPERTY));
                addIncludeProfiles(propertySource.getProperty(INCLUDE_PROFILES_PROPERTY));
            }
        }
        return propertySource;
    }

    private void maybeActivateProfiles(final Object value) {
        if (this.activatedProfiles) {
            return;
        }

        final Set<String> profiles = getProfilesForValue(value);
        activateProfiles(profiles);
        if (!profiles.isEmpty()) {
            this.activatedProfiles = true;
        }
    }

    private void addIncludeProfiles(final Object value) {
        final Set<String> profiles = getProfilesForValue(value);
        activateProfiles(profiles);
    }

    private Set<String> getProfilesForValue(final Object property) {
        return asResolvedSet(property == null ? null : property.toString(), null);
    }

    private void activateProfiles(final Set<String> profiles) {
        for (final String profile : profiles) {
            this.profiles.add(profile);
            // test if profile isn't active in current environment.
            if (!this.environment.acceptsProfiles(Profiles.of(profile))) {
                // If it's already accepted we assume the order was set
                // intentionally
                prependProfile(this.environment, profile);
            }
        }
    }

    private void prependProfile(final ConfigurableEnvironment environment, final String profile) {
        final Set<String> profiles = new LinkedHashSet<>();
        environment.getActiveProfiles(); // ensure they are initialized
        // But this one should go first (last wins in a property key clash)
        profiles.add(profile);
        profiles.addAll(Arrays.asList(environment.getActiveProfiles()));
        environment.setActiveProfiles(profiles.toArray(new String[profiles.size()]));
    }

    private Set<String> getSearchLocations() {
        final Set<String> locations = Sets.newLinkedHashSet();
        // User-configured settings take precedence, so we do them first
        if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
            for (String path : asResolvedSet(this.environment.getProperty(CONFIG_LOCATION_PROPERTY), null)) {
                if (!path.contains("$")) {
                    if (!path.contains(":")) {
                        path = "file:" + path;
                    }
                    path = StringUtils.cleanPath(path);
                }
                locations.add(path);
            }
        }
        locations.addAll(asResolvedSet(this.searchLocations, DEFAULT_SEARCH_LOCATIONS));
        return locations;
    }

    private Set<String> getSearchNames() {
        if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
            return asResolvedSet(this.environment.getProperty(CONFIG_NAME_PROPERTY), null);
        }
        return asResolvedSet(this.names, DEFAULT_NAMES);
    }

    private Set<String> asResolvedSet(final String value, final String fallback) {
        final List<String> list = Arrays.asList(StringUtils.commaDelimitedListToStringArray(
            value != null ? this.environment.resolvePlaceholders(value) : fallback));
        Collections.reverse(list);
        return new LinkedHashSet<>(list);
    }

    private void addConfigurationProperties(final MutablePropertySources sources) {
        final List<PropertySource<?>> reorderedSources = Lists.newArrayList();
        for (final PropertySource<?> item : sources) {
            reorderedSources.add(item);
        }
        // Maybe we should add before the DEFAULT_PROPERTIES if it exists?
        this.environment.getPropertySources().addLast(new ConfigurationPropertySources(reorderedSources));
    }

}
