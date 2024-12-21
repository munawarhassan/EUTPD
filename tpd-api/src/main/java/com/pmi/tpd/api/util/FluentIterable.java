/*
 * Copyright (C) 2008 The Guava Authors
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

package com.pmi.tpd.api.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

/**
 * {@code FluentIterable} provides a rich interface for manipulating
 * {@code Iterable} instances in a chained fashion. A
 * {@code FluentIterable} can be created from an {@code Iterable}, or from a set
 * of elements. The following types of
 * methods are provided on {@code FluentIterable}:
 * <ul>
 * <li>chained methods which return a new {@code FluentIterable} based in some
 * way on the contents of the current one
 * (for example {@link #transform})
 * <li>conversion methods which copy the {@code FluentIterable}'s contents into
 * a new collection or array (for example
 * {@link #toList})
 * <li>element extraction methods which facilitate the retrieval of certain
 * elements (for example {@link #last})
 * <li>query methods which answer questions about the {@code FluentIterable}'s
 * contents (for example {@link #anyMatch})
 * </ul>
 * <p>
 * Here is an example that merges the lists returned by two separate database
 * calls, transforms it by invoking
 * {@code toString()} on each element, and returns the first 10 elements as an
 * {@code ImmutableList}:
 *
 * <pre>
 *    {@code
 *
 *   FluentIterable
 *       .from(database.getClientList())
 *       .filter(activeInLastMonth())
 *       .transform(Functions.toStringFunction())
 *       .limit(10)
 *       .toList();}
 * </pre>
 * <p>
 * Anything which can be done using {@code FluentIterable} could be done in a
 * different fashion (often with
 * {@link Iterables}), however the use of {@code FluentIterable} makes many sets
 * of operations significantly more
 * concise.
 *
 * @author Marcin Mikosik
 * @author devacfr<christophefriederich@mac.com>
 * @since 12.0
 */
@GwtCompatible(emulated = true)
public abstract class FluentIterable<E> implements Iterable<E> {

  // We store 'iterable' and use it instead of 'this' to allow Iterables to
  // perform instanceof
  // checks on the _original_ iterable when FluentIterable.from is used.
  private final Iterable<E> iterable;

  /** Constructor for use by subclasses. */
  protected FluentIterable() {
    this.iterable = this;
  }

  FluentIterable(final Iterable<E> iterable) {
    this.iterable = checkNotNull(iterable);
  }

  /**
   * Returns a fluent iterable that wraps {@code iterable}, or {@code iterable}
   * itself if it is already a
   * {@code FluentIterable}.
   */
  public static <E> FluentIterable<E> from(final Iterable<E> iterable) {
    return iterable instanceof FluentIterable ? (FluentIterable<E>) iterable : new FluentIterable<>(iterable) {

      @Override
      public Iterator<E> iterator() {
        return iterable.iterator();
      }
    };
  }

  /**
   * Returns a fluent iterable that wraps {@code array}.
   */
  public static <E> FluentIterable<E> from(final E[] array) {
    final Iterable<E> iterable = Arrays.asList(array);
    return new FluentIterable<>(iterable) {

      @Override
      public Iterator<E> iterator() {
        return iterable.iterator();
      }
    };
  }

  /**
   * Returns a fluent iterable that wraps {@code iterable}, or {@code iterable}
   * itself if it is already a
   * {@code FluentIterable}.
   */
  public static <E> FluentIterable<E> from(final Iterator<E> iterator) {
    return new FluentIterable<>(once(iterator)) {

      @Override
      public Iterator<E> iterator() {
        return iterator;
      }
    };
  }

  /**
   * Returns a fluent iterable containing {@code elements} in the specified order.
   *
   * @since 18.0
   */
  @Beta
  public static <E> FluentIterable<E> of(final E[] elements) {
    return from(Lists.newArrayList(elements));
  }

  /**
   * Returns a string representation of this fluent iterable, with the format
   * {@code [e1, e2, ..., en]}.
   */
  @Override
  public String toString() {
    return Iterables.toString(iterable);
  }

  /**
   * Returns the number of elements in this fluent iterable.
   */
  public final int size() {
    return Iterables.size(iterable);
  }

  /**
   * Returns {@code true} if this fluent iterable contains any object for which
   * {@code equals(element)} is true.
   */
  public final boolean contains(@Nullable final Object element) {
    return Iterables.contains(iterable, element);
  }

  /**
   * Returns a fluent iterable whose {@code Iterator} cycles indefinitely over the
   * elements of this fluent iterable.
   * <p>
   * That iterator supports {@code remove()} if {@code iterable.iterator()} does.
   * After {@code remove()} is called,
   * subsequent cycles omit the removed element, which is no longer in this fluent
   * iterable. The iterator's
   * {@code hasNext()} method returns {@code true} until this fluent iterable is
   * empty.
   * <p>
   * <b>Warning:</b> Typical uses of the resulting iterator may produce an
   * infinite loop. You should use an explicit
   * {@code break} or be certain that you will eventually remove all the elements.
   */
  @CheckReturnValue
  public final FluentIterable<E> cycle() {
    return from(Iterables.cycle(iterable));
  }

  /**
   * Returns a fluent iterable whose iterators traverse first the elements of this
   * fluent iterable, followed by those
   * of {@code other}. The iterators are not polled until necessary.
   * <p>
   * The returned iterable's {@code Iterator} supports {@code remove()} when the
   * corresponding {@code Iterator}
   * supports it.
   *
   * @since 18.0
   */
  @Beta
  @CheckReturnValue
  public final FluentIterable<E> append(final Iterable<? extends E> other) {
    return from(Iterables.concat(iterable, other));
  }

  /**
   * Returns a fluent iterable whose iterators traverse first the elements of this
   * fluent iterable, followed by
   * {@code elements}.
   *
   * @since 18.0
   */
  @SafeVarargs
  @Beta
  @CheckReturnValue
  public final FluentIterable<E> append(final E... elements) {
    return from(Iterables.concat(iterable, Arrays.asList(elements)));
  }

  /**
   * Returns the elements from this fluent iterable that satisfy a predicate. The
   * resulting fluent iterable's iterator
   * does not support {@code remove()}.
   */
  @CheckReturnValue
  public final FluentIterable<E> filter(final Predicate<? super E> predicate) {
    return from(Iterables.filter(iterable, predicate));
  }

  /**
   * Returns the elements from this fluent iterable that are instances of class
   * {@code type}.
   *
   * @param type
   *             the type of elements desired
   */
  @GwtIncompatible("Class.isInstance")
  @CheckReturnValue
  public final <T> FluentIterable<T> filter(final Class<T> type) {
    return from(Iterables.filter(iterable, type));
  }

  /**
   * Returns {@code true} if any element in this fluent iterable satisfies the
   * predicate.
   */
  public final boolean anyMatch(final Predicate<? super E> predicate) {
    return Iterables.any(iterable, predicate);
  }

  /**
   * Returns {@code true} if every element in this fluent iterable satisfies the
   * predicate. If this fluent iterable is
   * empty, {@code true} is returned.
   */
  public final boolean allMatch(final Predicate<? super E> predicate) {
    return Iterables.all(iterable, predicate);
  }

  /**
   * Returns an {@link Optional} containing the first element in this fluent
   * iterable that satisfies the given
   * predicate, if such an element exists.
   * <p>
   * <b>Warning:</b> avoid using a {@code predicate} that matches {@code null}. If
   * {@code null} is matched in this
   * fluent iterable, a {@link NullPointerException} will be thrown.
   */
  public final Optional<E> firstMatch(final Predicate<? super E> predicate) {
    return Iterables.tryFind(iterable, predicate);
  }

  /**
   * Returns a fluent iterable that applies {@code function} to each element of
   * this fluent iterable.
   * <p>
   * The returned fluent iterable's iterator supports {@code remove()} if this
   * iterable's iterator does. After a
   * successful {@code remove()} call, this fluent iterable no longer contains the
   * corresponding element.
   */
  public final <T> FluentIterable<T> transform(final Function<? super E, T> function) {
    return from(Iterables.transform(iterable, function));
  }

  /**
   * Applies {@code function} to each element of this fluent iterable and returns
   * a fluent iterable with the
   * concatenated combination of results. {@code function} returns an Iterable of
   * results.
   * <p>
   * The returned fluent iterable's iterator supports {@code remove()} if this
   * function-returned iterables' iterator
   * does. After a successful {@code remove()} call, the returned fluent iterable
   * no longer contains the corresponding
   * element.
   *
   * @since 13.0 (required {@code Function<E, Iterable<T>>} until 14.0)
   */
  public <T> FluentIterable<T> transformAndConcat(
      final Function<? super E, ? extends Iterable<? extends T>> function) {
    return from(Iterables.concat(transform(function)));
  }

  /**
   * Returns an {@link Optional} containing the first element in this fluent
   * iterable. If the iterable is empty,
   * {@code Optional.absent()} is returned.
   *
   * @throws NullPointerException
   *                              if the first element is null; if this is a
   *                              possibility, use {@code iterator().next()} or
   *                              {@link Iterables#getFirst} instead.
   */
  public final Optional<E> first() {
    final Iterator<E> iterator = iterable.iterator();
    return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.<E>absent();
  }

  /**
   * Returns an {@link Optional} containing the last element in this fluent
   * iterable. If the iterable is empty,
   * {@code Optional.absent()} is returned.
   *
   * @throws NullPointerException
   *                              if the last element is null; if this is a
   *                              possibility, use {@link Iterables#getLast}
   *                              instead.
   */
  public final Optional<E> last() {
    // Iterables#getLast was inlined here so we don't have to throw/catch a NSEE

    // TODO(kevinb): Support a concurrently modified collection?
    if (iterable instanceof List) {
      final List<E> list = (List<E>) iterable;
      if (list.isEmpty()) {
        return Optional.absent();
      }
      return Optional.of(list.get(list.size() - 1));
    }
    final Iterator<E> iterator = iterable.iterator();
    if (!iterator.hasNext()) {
      return Optional.absent();
    }

    /*
     * TODO(kevinb): consider whether this "optimization" is worthwhile. Users with
     * SortedSets tend to know they are
     * SortedSets and probably would not call this method.
     */
    if (iterable instanceof SortedSet) {
      final SortedSet<E> sortedSet = (SortedSet<E>) iterable;
      return Optional.of(sortedSet.last());
    }

    while (true) {
      final E current = iterator.next();
      if (!iterator.hasNext()) {
        return Optional.of(current);
      }
    }
  }

  /**
   * Returns a view of this fluent iterable that skips its first
   * {@code numberToSkip} elements. If this fluent
   * iterable contains fewer than {@code numberToSkip} elements, the returned
   * fluent iterable skips all of its
   * elements.
   * <p>
   * Modifications to this fluent iterable before a call to {@code iterator()} are
   * reflected in the returned fluent
   * iterable. That is, the its iterator skips the first {@code numberToSkip}
   * elements that exist when the iterator is
   * created, not when {@code skip()} is called.
   * <p>
   * The returned fluent iterable's iterator supports {@code remove()} if the
   * {@code Iterator} of this fluent iterable
   * supports it. Note that it is <i>not</i> possible to delete the last skipped
   * element by immediately calling
   * {@code remove()} on the returned fluent iterable's iterator, as the
   * {@code Iterator} contract states that a call
   * to {@code * remove()} before a call to {@code next()} will throw an
   * {@link IllegalStateException}.
   */
  @CheckReturnValue
  public final FluentIterable<E> skip(final int numberToSkip) {
    return from(Iterables.skip(iterable, numberToSkip));
  }

  /**
   * Creates a fluent iterable with the first {@code size} elements of this fluent
   * iterable. If this fluent iterable
   * does not contain that many elements, the returned fluent iterable will have
   * the same behavior as this fluent
   * iterable. The returned fluent iterable's iterator supports {@code remove()}
   * if this fluent iterable's iterator
   * does.
   *
   * @param size
   *             the maximum number of elements in the returned fluent iterable
   * @throws IllegalArgumentException
   *                                  if {@code size} is negative
   */
  @CheckReturnValue
  public final FluentIterable<E> limit(final int size) {
    return from(Iterables.limit(iterable, size));
  }

  /**
   * Determines whether this fluent iterable is empty.
   */
  public final boolean isEmpty() {
    return !iterable.iterator().hasNext();
  }

  /**
   * Returns an {@code ImmutableList} containing all of the elements from this
   * fluent iterable in proper sequence.
   *
   * @since 14.0 (since 12.0 as {@code toImmutableList()}).
   */
  public final ImmutableList<E> toList() {
    return ImmutableList.copyOf(iterable);
  }

  /**
   * Returns an {@code ImmutableList} containing all of the elements from this
   * {@code
   * FluentIterable} in the order specified by {@code comparator}. To produce an
   * {@code
   * ImmutableList} sorted by its natural ordering, use
   * {@code toSortedList(Ordering.natural())}.
   *
   * @param comparator
   *                   the function by which to sort list elements
   * @throws NullPointerException
   *                              if any element is null
   * @since 14.0 (since 13.0 as {@code toSortedImmutableList()}).
   */
  public final ImmutableList<E> toSortedList(final Comparator<? super E> comparator) {
    return Ordering.from(comparator).immutableSortedCopy(iterable);
  }

  /**
   * Returns an {@code ImmutableSet} containing all of the elements from this
   * fluent iterable with duplicates removed.
   *
   * @since 14.0 (since 12.0 as {@code toImmutableSet()}).
   */
  public final ImmutableSet<E> toSet() {
    return ImmutableSet.copyOf(iterable);
  }

  /**
   * Returns an {@code ImmutableSortedSet} containing all of the elements from
   * this {@code
   * FluentIterable} in the order specified by {@code comparator}, with duplicates
   * (determined by
   * {@code comparator.compare(x, y) == 0}) removed. To produce an
   * {@code ImmutableSortedSet} sorted by its natural
   * ordering, use {@code toSortedSet(Ordering.natural())}.
   *
   * @param comparator
   *                   the function by which to sort set elements
   * @throws NullPointerException
   *                              if any element is null
   * @since 14.0 (since 12.0 as {@code toImmutableSortedSet()}).
   */
  public final ImmutableSortedSet<E> toSortedSet(final Comparator<? super E> comparator) {
    return ImmutableSortedSet.copyOf(comparator, iterable);
  }

  /**
   * Returns an immutable map for which the elements of this
   * {@code FluentIterable} are the keys in the same order,
   * mapped to values by the given function. If this iterable contains duplicate
   * elements, the returned map will
   * contain each distinct element once in the order it first appears.
   *
   * @throws NullPointerException
   *                              if any element of this iterable is {@code null},
   *                              or if {@code
   *     valueFunction}        produces {@code null} for any key
   * @since 14.0
   */
  public final <V> ImmutableMap<E, V> toMap(final Function<? super E, V> valueFunction) {
    return Maps.toMap(iterable, valueFunction);
  }

  /**
   * Creates an index {@code ImmutableListMultimap} that contains the results of
   * applying a specified function to each
   * item in this {@code FluentIterable} of values. Each element of this iterable
   * will be stored as a value in the
   * resulting multimap, yielding a multimap with the same size as this iterable.
   * The key used to store that value in
   * the multimap will be the result of calling the function on that value. The
   * resulting multimap is created as an
   * immutable snapshot. In the returned multimap, keys appear in the order they
   * are first encountered, and the values
   * corresponding to each key appear in the same order as they are encountered.
   *
   * @param keyFunction
   *                    the function used to produce the key for each value
   * @throws NullPointerException
   *                              if any of the following cases is true:
   *                              <ul>
   *                              <li>{@code keyFunction} is null
   *                              <li>An element in this fluent iterable is null
   *                              <li>{@code keyFunction} returns {@code null} for
   *                              any element of this iterable
   *                              </ul>
   * @since 14.0
   */
  public final <K> ImmutableListMultimap<K, E> index(final Function<? super E, K> keyFunction) {
    return Multimaps.index(iterable, keyFunction);
  }

  /**
   * Returns an immutable map for which the {@link java.util.Map#values} are the
   * elements of this
   * {@code FluentIterable} in the given order, and each key is the product of
   * invoking a supplied function on its
   * corresponding value.
   *
   * @param keyFunction
   *                    the function used to produce the key for each value
   * @throws IllegalArgumentException
   *                                  if {@code keyFunction} produces the same key
   *                                  for more than one value in this fluent
   *                                  iterable
   * @throws NullPointerException
   *                                  if any element of this fluent iterable is
   *                                  null, or if {@code keyFunction} produces
   *                                  {@code null} for
   *                                  any value
   * @since 14.0
   */
  public final <K> ImmutableMap<K, E> uniqueIndex(final Function<? super E, K> keyFunction) {
    return Maps.uniqueIndex(iterable, keyFunction);
  }

  /**
   * Returns an array containing all of the elements from this fluent iterable in
   * iteration order.
   *
   * @param type
   *             the type of the elements
   * @return a newly-allocated array into which all the elements of this fluent
   *         iterable have been copied
   */
  @GwtIncompatible("Array.newArray(Class, int)")
  public final E[] toArray(final Class<E> type) {
    return Iterables.toArray(iterable, type);
  }

  /**
   * Copies all the elements from this fluent iterable to {@code collection}. This
   * is equivalent to calling
   * {@code Iterables.addAll(collection, this)}.
   *
   * @param collection
   *                   the collection to copy elements to
   * @return {@code collection}, for convenience
   * @since 14.0
   */
  public final <C extends Collection<? super E>> C copyInto(final C collection) {
    checkNotNull(collection);
    if (iterable instanceof Collection) {
      collection.addAll(cast(iterable));
    } else {
      for (final E item : iterable) {
        collection.add(item);
      }
    }
    return collection;
  }

  /**
   * Returns a {@link String} containing all of the elements of this fluent
   * iterable joined with {@code joiner}.
   *
   * @since 18.0
   */
  @Beta
  public final String join(final Joiner joiner) {
    return joiner.join(this);
  }

  /**
   * Returns the element at the specified position in this fluent iterable.
   *
   * @param position
   *                 position of the element to return
   * @return the element at the specified position in this fluent iterable
   * @throws IndexOutOfBoundsException
   *                                   if {@code position} is negative or greater
   *                                   than or equal to the size of this fluent
   *                                   iterable
   */
  public final E get(final int position) {
    return Iterables.get(iterable, position);
  }

  /**
   * @param predicate
   *                  a predicate use to filter
   * @return Returns the first element found or {@code null}.
   */
  public E firstOrDefault(final Predicate<E> predicate) {
    return Iterables.tryFind(iterable, predicate).orNull();
  }

  public E firstOrDefault(final Predicate<E> predicate, final E defaultValue) {
    return Iterables.tryFind(iterable, predicate).or(defaultValue);
  }

  /**
   * <p>
   * equivalent to method {@link Iterables#find(Iterable, Predicate)}.
   * </p>
   *
   * @param predicate
   *                  a predicate use to filter
   * @return Returns the first element in iterable that satisfies the given
   *         predicate; use this method only when such
   *         an element is known to exist.
   */
  public E single(final Predicate<E> predicate) {
    return Iterables.find(iterable, predicate);
  }

  /**
   * @return Returns the single element in iterable that satisfies the given
   *         predicate; use this method only when such
   *         an element is known to exist.
   */
  public Optional<E> singleOrDefault() {
    try {
      return Optional.of(Iterables.getOnlyElement(iterable));
    } catch (final NoSuchElementException e) {
      return Optional.absent();
    }
  }

  /**
   * @param predicate
   * @return
   */
  public FluentIterable<E> skipWhile(final Predicate<E> predicate) {
    final List<E> to = Lists.newArrayList();

    boolean started = false;
    for (final E x : iterable) {
      if (!started && predicate.apply(x)) {
        continue;
      }

      started = true;
      to.add(x);
    }
    return from(to);
  }

  /**
   * @param xs
   * @param predicate
   * @return
   */
  public FluentIterable<E> takeWhile(final Predicate<E> predicate) {
    final List<E> to = Lists.newArrayList();

    for (final E x : iterable) {
      if (!predicate.apply(x)) {
        return from(to);
      }
      to.add(x);
    }
    return from(to);
  }

  /**
   * @param function
   * @return
   */
  public <F> FluentIterable<F> selectMany(final Function<E, Iterable<F>> function) {
    final List<F> list = Lists.newArrayList();
    for (final E item : iterable) {
      Iterables.addAll(list, function.apply(item));
    }
    return from(list);
  }

  /**
   * @param aggregate
   * @return
   */
  public E aggregate(final Aggregate<E> aggregate) {
    E element = null;
    for (final E t : iterable) {
      if (element == null) {
        element = t;
        continue;
      }
      element = aggregate.apply(element, t);
    }
    return element;
  }

  /**
   * @param function
   * @return
   */
  public FluentIterable<E> orderByDescending(final Ordering<E> ordering) {
    return from(ordering.reverse().sortedCopy(iterable));
  }

  /**
   * Sort the underlying iterable using {@code comparator}.
   *
   * @param comparator
   *                   comparator to sort
   * @return new sorted copy of the original
   */
  @Nonnull
  public FluentIterable<E> sort(@Nonnull final Comparator<? super E> comparator) {
    return sort(Ordering.from(comparator));
  }

  /**
   * Sort the underlying iterable using {@code ordering}.
   *
   * @param ordering
   *                 ordering to sort
   * @return new sorted copy of the original
   */
  @Nonnull
  public FluentIterable<E> sort(@Nonnull final Ordering<? super E> ordering) {
    return from(checkNotNull(ordering, "ordering").sortedCopy(iterable));
  }

  /**
   * Used to avoid http://bugs.sun.com/view_bug.do?bug_id=6558557
   */
  private static <T> Collection<T> cast(final Iterable<T> iterable) {
    return (Collection<T>) iterable;
  }

  /**
   * @param source
   * @return
   */
  private static <T> Iterable<T> once(final Iterator<T> source) {
    return new Iterable<>() {

      private final AtomicBoolean exhausted = new AtomicBoolean();

      @Override
      public Iterator<T> iterator() {
        Preconditions.checkState(!exhausted.getAndSet(true));
        return source;
      }
    };
  }
}
