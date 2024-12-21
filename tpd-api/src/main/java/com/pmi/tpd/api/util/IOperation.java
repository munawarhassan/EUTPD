package com.pmi.tpd.api.util;

/**
 * Interface for operations carried out by services on behalf of plugins.
 *
 * @param <R>
 *            the return type of the perform operation
 * @param <E>
 *            the type of Throwable that can be thrown from perform.
 * @author Christophe Friederich
 * @since 1.3
 */
@FunctionalInterface
public interface IOperation<R, E extends Throwable> {

    /**
     * @return
     * @throws E
     */
    R perform() throws E;

}
