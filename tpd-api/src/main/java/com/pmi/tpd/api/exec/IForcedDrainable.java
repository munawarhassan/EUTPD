package com.pmi.tpd.api.exec;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Indicates connection pools and similar data structures which have a means of
 * waiting for resources to close/clean up
 * and additionally support a more aggressive form of draining which attempts to
 * forcibly release the underlying
 * resources.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IForcedDrainable extends IDrainable {

  /**
   * A more aggressive form of
   * {@link #drain(long, java.util.concurrent.TimeUnit)}. If after the supplied
   * timeout the
   * underlying resources are not free then this object will take additional steps
   * to ensure they are free. Callers
   * are advised to first {@link #drain(long, java.util.concurrent.TimeUnit)
   * drain} before forcing to minimise adverse
   * effects from this more aggressive approach.
   * <p>
   * Note that this method is not guaranteed to succeed and the caller must take
   * care to handle the possibility that
   * some or all of the underlying resources may still be in use after this method
   * returns.
   *
   * @param timeout
   *                the timeout which, using the specified unit, will be converted
   *                to milliseconds
   * @param unit
   *                the unit the provided timeout is in
   * @return {@code true} if the pool was drained; otherwise, {@code false} if 1
   *         or more resources could not be
   *         accounted for or forced within the specified timeout
   */
  boolean forceDrain(long timeout, @Nonnull TimeUnit unit);
}
