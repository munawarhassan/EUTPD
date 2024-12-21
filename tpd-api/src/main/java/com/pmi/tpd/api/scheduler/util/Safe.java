package com.pmi.tpd.api.scheduler.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

/**
 * Static utility methods for things like {@code null}-testing and defensive
 * copies.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class Safe {

  /** Make a defensive copy of a mutable {@link Date}. */
  @Nullable
  public static Date copy(@Nullable final Date date) {
    return date != null ? (Date) date.clone() : null;
  }

  /**
   * Make a defensive copy of a byte array.
   */
  @Nullable
  public static byte[] copy(@Nullable final byte[] bytes) {
    return bytes != null ? bytes.clone() : null;
  }

  /**
   * Make a defensive copy of a possibly mutable map. An empty map is substituted
   * for {@code null}.
   */
  @Nonnull
  public static Map<String, Serializable> copy(@Nullable final Map<String, Serializable> map) {
    final Map<String, Serializable> copy;
    if (map == null) {
      copy = ImmutableMap.of();
    } else if (map instanceof ImmutableMap) {
      copy = map;
    } else {
      copy = Collections.unmodifiableMap(new HashMap<String, Serializable>(map));
    }
    return copy;
  }

  private Safe() {
    throw new Error("I am static-only.");
  }
}
