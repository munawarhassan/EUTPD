package com.pmi.tpd.api.model;

/**
 * <p>
 * IIdentityEntity interface.
 * </p>
 *
 * @param <T>
 *            a entity type.
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IIdentityEntity<T> {

    /**
     * <p>
     * getId.
     * </p>
     *
     * @return a T object.
     */
    T getId();

}
