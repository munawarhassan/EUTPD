package com.pmi.tpd.core.bootstrap;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.IOperation;

import liquibase.exception.LockException;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBootstrapLock {

    /**
     * Executes an {@link Operation} after acquiring the lock.
     *
     * @param operation
     *            the operation to perform under lock
     * @return the result of the operation
     * @throws E
     *             exception the operation chooses to throw
     * @throws LockException
     *             when failed to acquire or release the lock
     */
    <T, E extends Throwable> T withLock(@Nonnull IOperation<T, E> operation) throws E;
}
