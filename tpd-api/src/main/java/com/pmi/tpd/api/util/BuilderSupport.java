package com.pmi.tpd.api.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Iterables;

/**
 * <p>
 * BuilderSupport class.
 * </p>
 *
 * @since 1.0
 * @author Christophe Friederich
 */
@SuppressWarnings("PMD")
public abstract class BuilderSupport {

  /**
   * <p>
   * addIf.
   * </p>
   *
   * @param predicate
   *                   a {@link com.google.common.base.Predicate} object.
   * @param collection
   *                   a {@link java.util.Collection} object.
   * @param value
   *                   a T object.
   * @param <T>
   *                   a T object.
   * @return a boolean.
   */
  protected static <T> boolean addIf(@Nonnull final Predicate<? super T> predicate,
      @Nonnull final Collection<T> collection,
      @Nullable final T value) {
    return predicate.apply(value) && collection.add(value);
  }

  /**
   * <p>
   * addIf.
   * </p>
   *
   * @param predicate
   *                  a {@link com.google.common.base.Predicate} object.
   * @param builder
   *                  a
   *                  {@link com.google.common.collect.ImmutableCollection.Builder}
   *                  object.
   * @param value
   *                  a T object.
   * @param <T>
   *                  a T object.
   * @return a boolean.
   */
  protected static <T> boolean addIf(@Nonnull final Predicate<? super T> predicate,
      @Nonnull final ImmutableCollection.Builder<T> builder,
      @Nullable final T value) {
    if (predicate.apply(value)) {
      builder.add(value);

      return true;
    }
    return false;
  }

  /**
   * <p>
   * addIf.
   * </p>
   *
   * @param predicate
   *                   verifies elements prior to adding them to the collection
   * @param collection
   *                   the collection to which elements accepted by the predicate
   *                   should be added
   * @param value
   *                   the first element to add
   * @param values
   *                   a varargs array containing 0 or more elements to add after
   *                   the first
   * @param <T>
   *                   The type of element contained by the collection.
   */
  @SafeVarargs
  protected static <T> void addIf(@Nonnull final Predicate<? super T> predicate,
      @Nonnull final Collection<T> collection,
      @Nullable final T value,
      @Nullable final T... values) {
    addIf(predicate, collection, value);
    if (values != null && values.length > 0) {
      addIf(predicate, collection, Arrays.asList(values));
    }
  }

  /**
   * <p>
   * addIf.
   * </p>
   *
   * @param predicate
   *                  a {@link com.google.common.base.Predicate} object.
   * @param builder
   *                  a
   *                  {@link com.google.common.collect.ImmutableCollection.Builder}
   *                  object.
   * @param value
   *                  a T object.
   * @param values
   *                  a T object.
   * @param <T>
   *                  a T object.
   */
  @SafeVarargs
  protected static <T> void addIf(@Nonnull final Predicate<? super T> predicate,
      @Nonnull final ImmutableCollection.Builder<T> builder,
      @Nullable final T value,
      @Nullable final T... values) {
    addIf(predicate, builder, value);
    if (values != null && values.length > 0) {
      addIf(predicate, builder, Arrays.asList(values));
    }
  }

  /**
   * <p>
   * addIf.
   * </p>
   *
   * @param predicate
   *                   a {@link com.google.common.base.Predicate} object.
   * @param collection
   *                   a {@link java.util.Collection} object.
   * @param values
   *                   a {@link java.lang.Iterable} object.
   * @param <T>
   *                   a T object.
   */
  protected static <T> void addIf(@Nonnull final Predicate<? super T> predicate,
      @Nonnull final Collection<T> collection,
      @Nullable final Iterable<? extends T> values) {
    if (values != null) {
      Iterables.addAll(collection, Iterables.filter(values, predicate));
    }
  }

  /**
   * <p>
   * addIf.
   * </p>
   *
   * @param predicate
   *                  a {@link com.google.common.base.Predicate} object.
   * @param builder
   *                  a
   *                  {@link com.google.common.collect.ImmutableCollection.Builder}
   *                  object.
   * @param values
   *                  a {@link java.lang.Iterable} object.
   * @param <T>
   *                  a T object.
   */
  protected static <T> void addIf(@Nonnull final Predicate<? super T> predicate,
      @Nonnull final ImmutableCollection.Builder<T> builder,
      @Nullable final Iterable<? extends T> values) {
    if (values != null) {
      builder.addAll(Iterables.filter(values, predicate));
    }
  }

  /**
   * <p>
   * checkNotBlank.
   * </p>
   *
   * @param value
   *              a {@link java.lang.String} object.
   * @param name
   *              a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  protected static String checkNotBlank(final String value, final String name) {
    checkNotNull(value, name);
    checkArgument(!value.trim().isEmpty(), "A non-blank " + name + " is required");

    return value;
  }

}
