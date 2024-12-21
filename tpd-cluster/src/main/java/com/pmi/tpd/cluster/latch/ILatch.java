package com.pmi.tpd.cluster.latch;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Describes a latch which can be acquired for a given resource to prevent new
 * operations from beginning and to
 * {@link #drain(long, TimeUnit) drain} existing operations.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILatch {

  /**
   * Drains the latched resource, ensuring currently-running operations are
   * complete. The exact type of operations
   * drained depends on the resource. The contract for the latch is that when this
   * method returns {@code true} the
   * resource must be in a quiesced state.
   *
   * @param timeout
   *                 the maximum time to wait for the resource to drain
   * @param timeUnit
   *                 the time unit
   * @return {@code true} if the resource was drained before {@code timeout}
   *         expired
   * @see #forceDrain(long, TimeUnit)
   */
  boolean drain(long timeout, @Nonnull TimeUnit timeUnit);

  /**
   * Attempts to forcibly drain the latched resource, ensuring currently-running
   * operations are complete. The exact
   * type of operations drained depends on the resource as is the exact method (if
   * any) by which forcible draining
   * occurs. The contract for the latch is that when this method returns
   * {@code true} the resource must be in a
   * quiesced state. Callers are advised to first
   * {@link #drain(long, java.util.concurrent.TimeUnit) drain} before
   * forcibly draining to minimise adverse effects from this more aggressive
   * approach.
   * <p>
   * Note that this method is not guaranteed to succeed and the caller must take
   * care to handle the possibility that
   * some or all of the underlying resources may still be in use after this method
   * returns.
   *
   * @param timeout
   *                 the maximum time to wait for the resource to drain before
   *                 attempting to do so forcibly
   * @param timeUnit
   *                 the time unit
   * @return {@code true} if the resource was forcibly drained before
   *         {@code timeout} expired
   * @see #drain(long, TimeUnit)
   * @since 3.7
   */
  boolean forceDrain(long timeout, @Nonnull TimeUnit timeUnit);

  /**
   * @return {@link LatchMode#LOCAL} for a local latch, {@link LatchMode#CLUSTER}
   *         for a clustered latch
   */
  @Nonnull
  LatchMode getMode();

  /**
   * Unlatches the resource, allowing new operations to begin.
   */
  void unlatch();
}
