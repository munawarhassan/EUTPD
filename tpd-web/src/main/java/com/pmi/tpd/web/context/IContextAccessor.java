package com.pmi.tpd.web.context;

import javax.annotation.Nullable;

/**
 * Value accessor depending of used context.
 *
 * @param <V>
 *            a object type.
 * @author Christophe Friederich
 * @since 1.0
 */
interface IContextAccessor<V> {

    /**
     * Gets the stored value in a specific context.
     *
     * @return returns the stored value in a specific context.
     */
    @Nullable
    V get();

    /**
     * Sets a value in a specific context.
     *
     * @param value
     *            a value to store (can be {@code null}).
     */
    void set(@Nullable V value);

}
