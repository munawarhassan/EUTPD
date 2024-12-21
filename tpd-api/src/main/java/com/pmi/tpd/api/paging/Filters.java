package com.pmi.tpd.api.paging;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.RandomAccess;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

public class Filters implements List<Filter<?>>, RandomAccess, Cloneable, java.io.Serializable {

    private static final Filters EMPTY_LIST = new Filters(Collections.emptyList());

    private static final long serialVersionUID = 1L;

    public static Filters empty() {
        return EMPTY_LIST;
    }

    private final List<Filter<?>> delegate;

    public Filters() {
        this(Lists.newArrayList());
    }

    public Filters(final Filter<?>... filters) {
        this(Arrays.asList(filters));
    }

    public Filters(final List<Filter<?>> list) {
        delegate = list;
    }

    public Filter<?> get(@Nonnull final String property) {
        return this.delegate.stream().filter(f -> f.getProperty().equals(property)).findFirst().get();
    }

    public Optional<Filter<?>> findFirst(@Nonnull final String property) {
        return this.delegate.stream().filter(f -> f.getProperty().equals(property)).findFirst();
    }

    public boolean addOrReplace(final Filter<?> filter) {
        findFirst(filter.getProperty()).ifPresent(this::remove);
        return add(filter);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<Filter<?>> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(final Filter<?> e) {
        return delegate.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends Filter<?>> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends Filter<?>> c) {
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();

    }

    @Override
    public Filter<?> get(final int index) {
        return delegate.get(index);
    }

    @Override
    public Filter<?> set(final int index, final Filter<?> element) {
        return delegate.set(index, element);
    }

    @Override
    public void add(final int index, final Filter<?> element) {
        delegate.add(index, element);
    }

    @Override
    public Filter<?> remove(final int index) {
        return delegate.remove(index);
    }

    @Override
    public int indexOf(final Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public ListIterator<Filter<?>> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<Filter<?>> listIterator(final int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<Filter<?>> subList(final int fromIndex, final int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
