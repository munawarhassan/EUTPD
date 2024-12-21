package com.pmi.tpd.spring.env;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.pmi.tpd.api.util.Assert;

/**
 * Utility that can be used to {@link MutablePropertySources} using
 * {@link PropertySourceLoader}s.
 *
 * @author Dave Syer
 * @author Christophe Friederich
 * @since 1.0
 */
public class PropertySourcesLoader {

  /** LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertySourcesLoader.class);

  /**
   * the destination property sources.
   */
  private final MutablePropertySources propertySources;

  /**
   * list of available {@link PropertySourceLoader} factories.
   */
  private final List<PropertySourceLoader> loaders;

  /**
   * Create a new {@link PropertySourceLoader} instance backed by a new
   * {@link MutablePropertySources}.
   */
  public PropertySourcesLoader() {
    this(new MutablePropertySources());
  }

  /**
   * Create a new {@link PropertySourceLoader} instance backed by the specified
   * {@link MutablePropertySources}.
   *
   * @param propertySources
   *                        the destination property sources
   */
  public PropertySourcesLoader(final MutablePropertySources propertySources) {
    Assert.notNull(propertySources, "PropertySources must not be null");
    this.propertySources = propertySources;
    this.loaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, null);
  }

  /**
   * Load the specified resource (if possible) and add it as the first source.
   *
   * @param resource
   *                 the source resource (may be {@code null}).
   * @return the loaded property source or {@code null}
   * @throws IOException
   *                     if loading failed.
   */
  public PropertySource<?> load(final Resource resource) throws IOException {
    return load(resource, null);
  }

  /**
   * Load the profile-specific properties from the specified resource (if any) and
   * add it as the first source.
   *
   * @param resource
   *                 the source resource (may be {@code null}).
   * @param profile
   *                 a specific profile to load or {@code null} to load the
   *                 default.
   * @return the loaded property source or {@code null}
   * @throws IOException
   *                     if loading failed.
   */
  public PropertySource<?> load(final Resource resource, final String profile) throws IOException {
    return load(resource, resource.getDescription(), profile);
  }

  /**
   * Load the profile-specific properties from the specified resource (if any),
   * give the name provided and add it as
   * the first source.
   *
   * @param resource
   *                 the source resource (may be {@code null}).
   * @param name
   *                 the root property name (may be {@code null}).
   * @param profile
   *                 a specific profile to load or {@code null} to load the
   *                 default.
   * @return the loaded property source or {@code null}
   * @throws IOException
   *                     if loading failed.
   */
  public PropertySource<?> load(final Resource resource, final String name, final String profile) throws IOException {
    return load(resource, null, name, profile);
  }

  /**
   * Load the profile-specific properties from the specified resource (if any),
   * give the name provided and add it to a
   * group of property sources identified by the group name. Property sources are
   * added to the end of a group, but new
   * groups are added as the first in the chain being assembled. This means the
   * normal sequence of calls is to first
   * create the group for the default (null) profile, and then add specific groups
   * afterwards (with the highest
   * priority last). Property resolution from the resulting sources will consider
   * all keys for a given group first and
   * then move to the next group.
   *
   * @param resource
   *                 the source resource (may be {@code null}).
   * @param group
   *                 an identifier for the group that this source belongs to
   * @param name
   *                 the root property name (may be {@code null}).
   * @param profile
   *                 a specific profile to load or {@code null} to load the
   *                 default.
   * @return the loaded property source or {@code null}
   * @throws IOException
   *                     if loading failed.
   */
  public PropertySource<?> load(final Resource resource, final String group, final String name, final String profile)
      throws IOException {
    if (resource instanceof ClassPathResource || isFile(resource)) {
      final String sourceName = generatePropertySourceName(name, profile);
      for (final PropertySourceLoader loader : this.loaders) {
        if (canLoadFileExtension(loader, resource)) {
          try {
            final PropertySource<?> specific = loader.load(sourceName, resource, profile);
            addPropertySource(group, specific, profile);
            return specific;
          } catch (final FileNotFoundException | RuntimeException e) {
            return null;
          }
        }
      }
    }
    return null;
  }

  private boolean isFile(final Resource resource) {
    return resource != null && resource.exists()
        && StringUtils.hasText(StringUtils.getFilenameExtension(resource.getFilename()));
  }

  @Nonnull
  private String generatePropertySourceName(@Nonnull final String name, @Nullable final String profile) {
    return profile == null ? name : name + "#" + profile;
  }

  private boolean canLoadFileExtension(@Nonnull final PropertySourceLoader loader, @Nonnull final Resource resource) {
    checkNotNull(loader, "loader");
    checkNotNull(resource, "resource");
    if (Strings.isNullOrEmpty(resource.getFilename())) {
      return false;
    }
    final String filename = resource.getFilename().toLowerCase();
    for (final String extension : loader.getFileExtensions()) {
      if (filename.endsWith("." + extension.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private void addPropertySource(final String basename, final PropertySource<?> source, final String profile) {

    if (source == null) {
      return;
    }

    if (basename == null) {
      this.propertySources.addLast(source);
      return;
    }

    final EnumerableCompositePropertySource group = getGeneric(basename);
    group.add(source);
    LOGGER.trace("Adding PropertySource: " + source + " in group: " + basename);
    if (this.propertySources.contains(group.getName())) {
      this.propertySources.replace(group.getName(), group);
    } else {
      this.propertySources.addFirst(group);
    }

  }

  private EnumerableCompositePropertySource getGeneric(final String name) {
    final PropertySource<?> source = this.propertySources.get(name);
    if (source instanceof EnumerableCompositePropertySource) {
      return (EnumerableCompositePropertySource) source;
    }
    return new EnumerableCompositePropertySource(name);
  }

  /**
   * Return the {@link MutablePropertySources} being loaded.
   *
   * @return the property sources
   */
  public MutablePropertySources getPropertySources() {
    return this.propertySources;
  }

  /**
   * Returns all file extensions that could be loaded.
   *
   * @return the file extensions
   */
  public Set<String> getAllFileExtensions() {
    final Set<String> fileExtensions = new HashSet<>();
    for (final PropertySourceLoader loader : this.loaders) {
      fileExtensions.addAll(Arrays.asList(loader.getFileExtensions()));
    }
    return fileExtensions;
  }

}
