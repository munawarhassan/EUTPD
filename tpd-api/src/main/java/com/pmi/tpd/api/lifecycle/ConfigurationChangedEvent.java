package com.pmi.tpd.api.lifecycle;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * ConfigurationChangedEvent class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class ConfigurationChangedEvent<T> {

  private final T configuration;

  public ConfigurationChangedEvent(@Nonnull final T configuration) {
    this.configuration = Assert.checkNotNull(configuration, "configuration");
  }

  public boolean isAssignable(final Class<?> expectedClass) {

    return configuration.getClass().isAssignableFrom(expectedClass);
  }

  public T getNewConfiguration() {
    return configuration;
  }
}
