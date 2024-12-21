package com.pmi.tpd.cluster.util;

/**
 * @param <T>
 * @author Christophe Friederich
 * @since 1.3
 */
public class IOsgiSafe<T> {

    /**
     *
     */
    private final T value;

    /**
     * @param value
     */
    public IOsgiSafe(final T value) {
        this.value = value;
    }

    /**
     * @return
     */
    public T getValue() {
        return value;
    }

}
