package com.pmi.tpd.core.user.permission.internal;

import java.util.Comparator;
import java.util.Iterator;

import com.google.common.base.Predicate;

/**
 * A predicate which can be used to find the intersection between the given {@code Iterator} and a target one being
 * filtered.
 * <p>
 * Note, this Predicate _is_ stateful, _not_ repeatable and can _only_ be used if the following conditions are met:
 * <ul>
 * <li>The specified {@code Iterator} and the target {@code Iterator} being filtered must be sorted using identical
 * ordering</li>
 * <li>The {@code Comparator} specified must be consistent with the ordering applied to _both_ {@code Iterator}s</li>
 * <li>The Predicate can only be used to filter one target Iterator once</li>
 * </ul>
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class InIteratorPredicate<E> implements Predicate<E> {

    /** */
    private final Comparator<? super E> comparator;

    /** */
    private final Iterator<E> it;

    /** */
    private E current;

    /**
     * @param comparator
     * @param it
     */
    public InIteratorPredicate(final Comparator<? super E> comparator, final Iterator<E> it) {
        this.comparator = comparator;
        this.it = it;
    }

    @Override
    public boolean apply(final E value) {
        while (current != null || it.hasNext()) {
            if (current == null) {
                current = it.next();
            }
            final int i = comparator.compare(current, value);
            if (i > 0) {
                // current is after value. Provided both iterators are ordered correctly
                // we would have already value in the iterator.
                return false;
            } else if (i < 0) {
                // We haven't reached value yet. null out current and keep going
                current = null;
            } else {
                // We have a match
                return true;
            }
        }

        // No more in the specified iterator. the remainder will always be false
        return false;
    }
}