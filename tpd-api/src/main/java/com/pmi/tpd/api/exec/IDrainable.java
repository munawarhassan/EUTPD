package com.pmi.tpd.api.exec;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Indicates connection pools and similar data structures have a means of
 * waiting for resources to close/clean up. The
 * {@link #drain(long, java.util.concurrent.TimeUnit) drain} method may be used
 * to block while all resources are closed
 * or cleaned up in some implementation-specific way.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDrainable {

  /**
   * Waits for all resources to become free. For pooling resources, draining
   * ensures all pooled resources have been
   * returned to the pool.
   *
   * @param timeout
   *                the timeout which, using the specified unit, will be converted
   *                to milliseconds
   * @param unit
   *                the unit the provided timeout is in
   * @return {@code true} if the pool was drained; otherwise, {@code false} if 1
   *         or more resources could not be
   *         accounted for within the specified timeout
   */
  boolean drain(long timeout, @Nonnull TimeUnit unit);
}
