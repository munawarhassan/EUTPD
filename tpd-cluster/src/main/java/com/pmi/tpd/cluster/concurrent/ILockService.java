package com.pmi.tpd.cluster.concurrent;

import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;

/**
 * Provides various types of locks, all of which are safe to use in a cluster.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILockService {

  /**
   * Creates or retrieves a {@link Lock} by name.
   *
   * @param lockName
   *                 the name of the lock
   * @return the lock
   */
  @Nonnull
  Lock getLock(@Nonnull String lockName);

}
