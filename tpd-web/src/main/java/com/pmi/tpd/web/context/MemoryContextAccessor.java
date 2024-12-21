package com.pmi.tpd.web.context;

import javax.annotation.Nullable;

/**
 * A support class to help access the memory context.
 *
 * @author Christophe Friederich
 * @param <V>
 *            a object type.
 * @author Christophe Friederich
 * @since 1.0
 */
class MemoryContextAccessor<V> implements IContextAccessor<V> {

    /** */
    private V value;

    MemoryContextAccessor(final String attributeName) {
    }

    @Override
    @Nullable
    public V get() {
        return value;

    }

    @Override
    public void set(@Nullable final V value) {
        this.value = value;
    }
}
