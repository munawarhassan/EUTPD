package com.pmi.tpd.core.bootstrap;

import javax.annotation.Nonnull;

import com.pmi.tpd.cluster.concurrent.ILockService;

/**
 * Extends {@link ILockService} with functionality that's not going to be available to plugins.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBootstrapLockService extends ILockService {

    /**
     * Creates a {@link IBootstrapLock}.
     *
     * @return the bootstrap lock
     */
    @Nonnull
    IBootstrapLock getBootstrapLock();
}
