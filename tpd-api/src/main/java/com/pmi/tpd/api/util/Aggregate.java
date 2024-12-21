package com.pmi.tpd.api.util;

/**
 * @author Christophe Friederich
 * @since 1.7
 * @param <T>
 */
public interface Aggregate<T> {

    T apply(T base, T other);
}
