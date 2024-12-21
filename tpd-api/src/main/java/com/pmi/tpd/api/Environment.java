package com.pmi.tpd.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * Environment class.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public enum Environment {

  /** Constant <code>DEVELOPMENT</code>. */
  DEVELOPMENT("development"),

  /** Constant <code>TEST</code>. */
  TEST("test"),

  /** Constant <code>Staging</code>. */
  STATING("staging"),

  /** Constant <code>TEST</code>. */
  QA("qa"),

  /** Constant <code>INTEGRATION</code>. */
  INTEGRATION("integration"),

  /** Constant <code>INTEGRATION_TEST</code>. */
  INTEGRATION_TEST("integration-test"),

  /** Constant <code>PRODUCTION</code>. */
  PRODUCTION("production");

  private String name;

  private Environment(final String name) {
    this.name = Assert.notNull(name);
  }

  /**
   * @return
   */
  @JsonValue
  public String getName() {
    return name;
  }

  /**
   * <p>
   * get.
   * </p>
   *
   * @param type
   *             a {@link java.lang.String} object.
   * @return a {@link com.pmi.tpd.api.Environment} object.
   */
  @Nullable
  public static Environment get(final String type) {
    return getEnumIgnoreCase(Environment.class, type);
  }

  /**
   * <p>
   * map.
   * </p>
   *
   * @return a {@link java.util.Map} object.
   */
  @Nonnull
  public static Map<String, Environment> map() {
    return getEnumMap(Environment.class);
  }

  /**
   * <p>
   * list.
   * </p>
   *
   * @return a {@link java.util.List} object.
   */
  @Nonnull
  public static List<Environment> list() {
    return getEnumList(Environment.class);
  }

  /**
   * <p>
   * Gets the enum for the class, returning {@code null} if not found.
   * </p>
   * <p>
   * This method differs from {@link Enum#valueOf} in that it does not throw an
   * exception for an invalid enum name and
   * performs case insensitive matching of the name.
   * </p>
   *
   * @param <E>
   *                  the type of the enumeration
   * @param enumClass
   *                  the class of the enum to query, not null
   * @param enumName
   *                  the enum name, null returns null
   * @return the enum, null if not found
   * @since 2.5
   */
  private static <E extends Enum<E>> E getEnumIgnoreCase(final Class<E> enumClass, final String enumName) {
    if (enumName == null || !enumClass.isEnum()) {
      return null;
    }
    for (final E each : enumClass.getEnumConstants()) {
      if (each.name().equalsIgnoreCase(enumName)) {
        return each;
      }
    }
    return null;
  }

  /**
   * <p>
   * Gets the {@code Map} of enums by name.
   * </p>
   * <p>
   * This method is useful when you need a map of enums by name.
   * </p>
   *
   * @param <E>
   *                  the type of the enumeration
   * @param enumClass
   *                  the class of the enum to query, not null
   * @return the modifiable map of enum names to enums, never null
   * @since 2.5
   */
  private static <E extends Enum<E>> Map<String, E> getEnumMap(final Class<E> enumClass) {
    final Map<String, E> map = Maps.newHashMap();
    for (final E e : enumClass.getEnumConstants()) {
      map.put(e.name(), e);
    }
    return map;
  }

  /**
   * <p>
   * Gets the {@code List} of enums.
   * </p>
   * <p>
   * This method is useful when you need a list of enums rather than an array.
   * </p>
   *
   * @param <E>
   *                  the type of the enumeration
   * @param enumClass
   *                  the class of the enum to query, not null
   * @return the modifiable list of enums, never null
   */
  private static <E extends Enum<E>> List<E> getEnumList(final Class<E> enumClass) {
    return Lists.newArrayList(Arrays.asList(enumClass.getEnumConstants()));
  }

}
