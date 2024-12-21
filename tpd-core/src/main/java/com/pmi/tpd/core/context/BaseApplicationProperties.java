package com.pmi.tpd.core.context;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opensymphony.module.propertyset.PropertyException;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.ApplicationConstants.PropertyKeys;
import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.IPropertiesManager;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.server.ApplicationConfigurationChangedEvent;
import com.pmi.tpd.spring.context.bind.ConfigurationPropertiesBinder;
import com.pmi.tpd.spring.context.bind.ConfigurationPropertiesBinder.PropertyPath;
import com.pmi.tpd.spring.i18n.LocaleParser;

import io.atlassian.util.concurrent.LazyReference;

/**
 * A class to manage the interface with a single property set, used for
 * application properties.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class BaseApplicationProperties implements IApplicationProperties {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseApplicationProperties.class);

  /** */
  private final Provider<IPropertiesManager> propertiesManager;

  /** */
  private final IEventPublisher publisher;

  /** */
  private final Environment environment;

  /** */
  private final BeanFactory beanFactory;

  /** */
  private ConversionService conversionService;

  private ConfigurationPropertiesBinder binder;

  /** */
  private final LazyReference<Map<String, String>> defaultProperties = new LazyReference<>() {

    @Override
    protected Map<String, String> create() throws Exception {
      final Properties properties = loadDefaultProperties(environment);
      // We want to turn the Properties object into an immutable HashMap
      // for scalability.
      // (Properties is backed by a HashTable which is synchronised).
      final Map<String, String> defaultPropertyMap = new HashMap<>(properties.size());
      for (final Object keyObj : properties.keySet()) {
        if (keyObj instanceof String) {
          // this should always be the case because of the contract of
          // Properties
          final String key = (String) keyObj;
          defaultPropertyMap.put(key, properties.getProperty(key));
        }
      }
      return Collections.unmodifiableMap(defaultPropertyMap);
    }
  };

  /** */
  private final Locale defaultLocale = Locale.ENGLISH;

  @Inject
  public BaseApplicationProperties(@Nonnull final IEventPublisher publisher,
      @Nonnull final Provider<IPropertiesManager> propertiesManager, @Nonnull final Environment environment,
      final BeanFactory beanFactory) {
    this.publisher = checkNotNull(publisher, "publisher");
    this.propertiesManager = checkNotNull(propertiesManager, "propertiesManager");
    this.environment = checkNotNull(environment, "environment");
    this.beanFactory = checkNotNull(beanFactory, "beanFactory");
  }

  @PostConstruct
  public void init() {
    this.conversionService = beanFactory.getBean(ConversionService.class);
    if (conversionService == null) {
      this.conversionService = new DefaultConversionService();
    }
    if (this.binder == null) {
      this.binder = new ConfigurationPropertiesBinder(beanFactory);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final IPropertiesManager propertiesManager = getPropertiesManager();
    return propertiesManager != null ? propertiesManager.hashCode() : 0;
  }

  /**
   * @param event
   */
  @EventListener
  public void onClearCache(final ClearCacheEvent event) {
    refresh();
  }

  public void setConversionService(final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public void setBinder(final ConfigurationPropertiesBinder binder) {
    this.binder = binder;
  }

  /**
   *
   */
  public void flush() {
    final IPropertiesManager propertiesManager = getPropertiesManager();
    if (propertiesManager != null) {
      propertiesManager.flush();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getEncoding() {
    return getString(PropertyKeys.WEB_ENCODING).orElseGet(() -> {
      final String encoding = "UTF-8";
      setString(PropertyKeys.WEB_ENCODING, encoding);
      return encoding;
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Locale getDefaultLocale() {
    // The default locale almost never changes, but we must handle it
    // correctly when it does.
    // We store the Locale for the defaultLocale string, which is expensive
    // to create, in a very small cache (map).
    return getDefaultBackedString(PropertyKeys.I18N_DEFAULT_LOCALE).map(LocaleParser::parseLocale)
        .orElse(defaultLocale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getText(final String name) {
    return getPropertyAccessor().flatMap(prop -> prop.getText(name));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setText(final String name, final String value) {
    getPropertyAccessor().ifPresent(prop -> {
      prop.setText(name,
          value,
          (oldValue, newValue) -> this.publishUpdateConfiguration(name, oldValue, newValue));
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getString(@Nonnull final String name) {
    return getPropertyAccessor().flatMap(prop -> prop.getString(name));
  }

  /**
   * Get all the keys from the default properties.
   */
  @Override
  public Collection<String> getDefaultKeys() {
    return getDefaultProperties().keySet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getDefaultBackedString(final String name) {
    return getString(name).or(() -> getDefaultString(name));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getDefaultBackedString(final String name, final String defaultValue) {
    return getDefaultBackedString(name).or(() -> Optional.ofNullable(defaultValue));
  }

  /**
   * <p>
   * Get the property from the application properties, but if not found, try to
   * get from the default properties file.
   * </p>
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getDefaultBackedText(final String name) {
    return getText(name).or(() -> getDefaultString(name));
  }

  /**
   * Get the default property (if the property is not set).
   *
   * @param name
   *             the name of the property.
   */
  @Override
  public Optional<String> getDefaultString(final String name) {
    return ofNullable(getDefaultProperties().get(name));
  }

  /**
   *
   */
  // @Transactional(propagation = Propagation.REQUIRED, rollbackFor =
  // Exception.class)
  @Override
  public void setString(final String name, final String value) {
    getPropertyAccessor().ifPresent(prop -> prop.setString(name,
        value,
        (oldValue, newValue) -> this.publishUpdateConfiguration(name, oldValue, newValue)));
  }

  /**
   * @return Returns {@code true} Whether the specified key is present in the
   *         backing PropertySet. Typically called
   *         before {@link #getOption(String)}
   */
  @Override
  public final boolean exists(final String key) {
    return getPropertyAccessor().map(prop -> prop.exists(key)).orElse(false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAsActualType(final String key) {
    return (T) getPropertyAccessor().map(prop -> (T) prop.getAsActualType(key));
  }

  /**
   * <p>
   * Get the option from the application properties, but if not found, try to get
   * from the default properties file.
   * </p>
   * {@inheritDoc}
   */
  @Override
  public Optional<Boolean> getOption(final String key) {
    return getPropertyAccessor().filter(prop -> prop.exists(key))
        .flatMap(prop -> prop.getBoolean(key))
        .or(() -> getDefaultOption(key));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getKeys() {
    return getPropertyAccessor().map(IPropertyAccessor::getKeys).orElse(Collections.emptyList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> asMap() {
    final Map<String, String> defaultProperties = getDefaultProperties();
    final Optional<IPropertyAccessor> propertySet = getPropertyAccessor();
    if (propertySet.isEmpty()) {
      return Collections.emptyMap();
    }
    final Set<String> smooshedKeys = new HashSet<>();
    smooshedKeys.addAll(getKeys());
    smooshedKeys.addAll(defaultProperties.keySet());

    final Map<String, Object> allProperties = new HashMap<>(smooshedKeys.size());
    for (final String key : smooshedKeys) {
      Object value = null;
      try {
        value = propertySet.get().getAsActualType(key);
      } catch (final PropertyException ignored) {
        // if a key cannot be found it will throw an exception.
        // Brilliant!
      }
      if (value == null) {
        value = defaultProperties.get(key);
      }
      allProperties.put(key, value);
    }
    return allProperties;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setOption(final String key, final boolean value) {
    getPropertyAccessor().ifPresent(prop -> prop.setBoolean(key,
        value,
        (oldValue, newValue) -> this.publishUpdateConfiguration(key, oldValue, newValue)));
  }

  /**
   * Convenience method to get the content type for an application.
   */
  public String getCharacterSet() {
    return getString(ApplicationConstants.PropertyKeys.WEB_CHARACTER_SET).orElseGet(() -> {
      final String encoding = "UTF-8";
      setString(ApplicationConstants.PropertyKeys.WEB_CHARACTER_SET, encoding);
      return encoding;
    });
  }

  /**
   * <p>
   * Refresh application properties object by refreshing the PropertiesManager.
   * </p>
   * {@inheritDoc}
   */
  @Override
  public void refresh() {
    final IPropertiesManager propertiesManager = getPropertiesManager();
    if (propertiesManager != null) {
      propertiesManager.refresh();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("propertiesManager", propertiesManager).toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultApplicationProperties)) {
      return false;
    }

    final BaseApplicationProperties applicationProperties = (BaseApplicationProperties) o;

    final IPropertiesManager propertiesManager = getPropertiesManager();

    return propertiesManager == null ? applicationProperties.propertiesManager == null
        : propertiesManager.equals(applicationProperties.getPropertiesManager());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getKeysWithPrefix(final String prefix) {
    return getPropertyAccessor().map(prop -> prop.getKeysWithPrefix(prefix)).orElse(Collections.emptyList());
  }

  /**
   * @param key
   */
  public void remove(final String key) {
    getPropertyAccessor().ifPresent(prop -> {
      if (prop.exists(key)) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Remove property path '{}'", key);
        }
        prop.remove(key);
      }
    });
  }

  private <T extends Serializable> void publishUpdateConfiguration(@Nonnull final String propertyName,
      @Nullable final T oldValue,
      @Nullable final T newValue) {
    if (propertyName.contains("password")) {
      this.publisher.publish(
          new ApplicationConfigurationChangedEvent<>(this, propertyName, "************", "************"));
    } else {
      this.publisher.publish(new ApplicationConfigurationChangedEvent<>(this, propertyName, oldValue, newValue));
    }
  }

  @Override
  public <T> void storeConfiguration(@Nonnull final T configuration) {
    checkNotNull(configuration, "configurationProperties");
    checkIsConfiguration(configuration.getClass());
    final List<PropertyPath> propertyPaths = binder.resolvePath(configuration);
    propertyPaths.stream().forEach(propertyPath -> {
      final String value = this.conversionService.convert(propertyPath.getPropertyValue(), String.class);
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Store property path '{}' with value {}", propertyPath.getPath(), value);
      }
      this.setString(propertyPath.getPath(), value);
    });
    this.publisher.publish(new ConfigurationChangedEvent<>(configuration));
  }

  @Override
  public <T> T getConfiguration(@Nonnull final Class<T> configurationClass) {
    checkIsConfiguration(configurationClass);
    return binder.createAndBindConfiguration(configurationClass);
  }

  @Override
  public <T> void removeConfiguration(@Nonnull final Class<T> configurationClass) {
    checkIsConfiguration(configurationClass);
    final String targetName = binder.getTargetName(configurationClass);
    final T oldConfiguration = getConfiguration(configurationClass);
    getKeysWithPrefix(targetName).forEach(this::remove);
    this.publisher.publish(new ConfigurationChangedEvent<>(oldConfiguration));
  }

  private void checkIsConfiguration(@Nonnull final Class<?> configurationClass) {
    checkNotNull(configurationClass, "configurationClass");
    Assert.state(configurationClass.isAnnotationPresent(ConfigurationProperties.class));
  }

  private Optional<Boolean> getDefaultOption(final String name) {
    if (getDefaultProperties().containsKey(name)) {
      return Optional.of(Boolean.valueOf(getDefaultProperties().get(name)));
    }
    return Optional.empty();
  }

  private Map<String, String> getDefaultProperties() {
    return defaultProperties.get();
  }

  /**
   * Loads the list of default properties from the application.properties file.
   * <p>
   * This should only be called once, and then the results cached.
   *
   * @return the list of default properties from the application.properties or
   *         application.yml file.
   */
  @SuppressWarnings("unchecked")
  protected Properties loadDefaultProperties(final Environment environment) {
    final List<Class<?>> declaredClasses = Lists.asList(ApplicationConstants.PropertyKeys.class,
        ApplicationConstants.PropertyKeys.class.getDeclaredClasses());
    final Set<Field> fields = Sets.newHashSet();
    for (final Class<?> cl : declaredClasses) {
      fields.addAll(ReflectionUtils.getAllFields(cl));
    }
    final Properties defaultProperties = new Properties();
    try {
      for (final Field field : fields) {
        // reject private field
        if (Modifier.isPrivate(field.getModifiers())) {
          continue;
        }
        final String key = (String) field.get(null);
        if (environment.containsProperty(key)) {
          final String value = environment.getProperty(key);
          if (!Strings.isNullOrEmpty(value)) {
            defaultProperties.put(key, value);
          }
        }

      }
    } catch (final Exception e) {
      LOGGER.error("Could not load default properties from 'spring environment'.  Not using default properties",
          e);
    }
    return defaultProperties;
  }

  protected Optional<IPropertyAccessor> getPropertyAccessor() {
    final IPropertiesManager propertiesManager = getPropertiesManager();
    if (propertiesManager == null) {
      return Optional.empty();
    }
    return propertiesManager.getPropertyAccessor();
  }

  protected IPropertiesManager getPropertiesManager() {
    try {
      return this.propertiesManager.get();
    } catch (final BeanCreationException ex) {
      return null;
    }
  }

}
