package com.pmi.tpd.api.util;

/**
 * <p>
 * Abstract AbstractEntityBuilder class.
 * </p>
 *
 * @author Christophe Friederich
 * @param <T>
 * @param <B>
 * @since 1.3
 */
public abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> extends BuilderSupport {

    /**
     * <p>
     * self.
     * </p>
     *
     * @return a B object.
     */
    protected abstract B self();

    /**
     * <p>
     * build.
     * </p>
     *
     * @return a T object.
     */
    public abstract T build();
}
