package com.pmi.tpd.api.context;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Christophe Friederich
 */
public interface IPropertyAccessor {

  /**
   * @param <T>
   * @param key
   * @return
   */
  <T> T getAsActualType(String key);

  /**
   * @param name
   * @return
   */
  Optional<String> getText(String name);

  /**
   * @param name
   * @param value
   * @param callback
   */
  void setText(@Nonnull String name,
      @Nullable String value,
      @Nullable final IPropertyChangeCallback<String> callback);

  /**
   * @param name
   * @param value
   */
  void setText(String name, @Nullable String value);

  /**
   * @param name
   * @return
   */
  Optional<String> getString(@Nonnull String name);

  /**
   * @param name
   * @param value
   * @param callback
   */
  void setString(@Nonnull String name,
      @Nullable String value,
      @Nullable final IPropertyChangeCallback<String> callback);

  /**
   * @param name
   * @param value
   */
  void setString(@Nonnull String name, @Nullable String value);

  /**
   * @param key
   * @return
   */
  Optional<Date> getDate(@Nonnull String key);

  /**
   * @param key
   * @param date
   */
  void setDate(@Nonnull String key, @Nullable Date date);

  /**
   * @param key
   * @return
   */
  Optional<Long> getLong(@Nonnull String key);

  /**
   * @param key
   * @param value
   */
  void setLong(@Nonnull String key, @Nullable Long value);

  /**
   * @param key
   * @return
   */
  boolean exists(@Nonnull String key);

  /**
   * @return
   */
  @Nonnull
  List<String> getKeys();

  /**
   * @param key
   * @return
   */
  Optional<Boolean> getBoolean(@Nonnull String key);

  /**
   * @param key
   * @param value
   */
  void setBoolean(@Nonnull String key, @Nullable Boolean value);

  /**
   * @param key
   * @param value
   * @param callback
   */
  void setBoolean(@Nonnull String key,
      @Nullable Boolean value,
      @Nullable final IPropertyChangeCallback<Boolean> callback);

  /**
   * @param prefix
   * @return
   */
  List<String> getKeysWithPrefix(@Nonnull String prefix);

  /**
   * @param key
   */
  void remove(@Nonnull final String key);

  /**
   * @param pThat
   * @return
   */
  boolean identical(@Nonnull final IPropertyAccessor pThat);

  /**
   *
   */
  void flush();

  /**
   *
   */
  void refresh();

  @FunctionalInterface
  public interface IPropertySetFunction<T> {

    void set(@Nonnull String name, @Nullable T value);
  }

  @FunctionalInterface
  public interface IPropertyGetFunction<T> {

    @Nullable
    T get(@Nonnull String name);
  }

}
