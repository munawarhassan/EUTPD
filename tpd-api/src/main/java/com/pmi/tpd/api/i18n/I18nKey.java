package com.pmi.tpd.api.i18n;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * A container object to allow the passing of a property key and its associated
 * context variables. This allows a
 * property to be expanded just-in-time.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class I18nKey {

  /** */
  private final Object[] arguments;

  /** */
  private final String key;

  /**
   * @param key
   *                  the i18n key
   * @param arguments
   *                  argument associated to key.
   */
  public I18nKey(@Nonnull final String key, @Nonnull final Object... arguments) {
    this.key = checkNotNull(key, "key");
    this.arguments = checkNotNull(arguments, "context");
  }

  /**
   * @return Returns a array of arguments
   */
  @Nonnull
  public Object[] getArguments() {
    return Arrays.copyOf(arguments, arguments.length);
  }

  /**
   * @return Returns the i18n key.
   */
  @Nonnull
  public String getKey() {
    return key;
  }
}
