package com.pmi.tpd.cluster.latch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Common interface which can be shared across services which manage
 * {@link ILatch latchable} resources.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILatchableService<T extends ILatch> {

  /**
   * Latches the resource, blocking new operations from starting. The exact type
   * of operation prevented, and the
   * manner in which they are prevented, varies by resource.
   * <p>
   * The contract for acquiring a latch is that <i>new</i> operations must be
   * blocked. <i>Existing</i> operations may
   * still be in flight and should, where possible, be allowed to continue.
   * {@link ILatch#drain(long, java.util.concurrent.TimeUnit) Draining} or
   * {@link ILatch#forceDrain(long, java.util.concurrent.TimeUnit) forcibly
   * draining} the returned latch allows the
   * caller to ensure all <i>existing</i> operations have completed.
   *
   * @param latchMode
   *                  whether the resource should be latched locally or
   *                  cluster-wide
   * @return the latch
   */
  @Nonnull
  T acquireLatch(@Nonnull LatchMode latchMode);

  /**
   * Latches the resource, blocking new operations from starting. The exact type
   * of operation prevented, and the
   * manner in which they are prevented, varies by resource.
   * <p>
   * The contract for acquiring a latch is that <i>new</i> operations must be
   * blocked. <i>Existing</i> operations may
   * still be in flight and should, where possible, be allowed to continue.
   * {@link ILatch#drain(long, java.util.concurrent.TimeUnit) Draining} or
   * {@link ILatch#forceDrain(long, java.util.concurrent.TimeUnit) forcibly
   * draining} the returned latch allows the
   * caller to ensure all <i>existing</i> operations have completed.
   *
   * @param latchMode
   *                  whether the resource should be latched locally or
   *                  cluster-wide
   * @param latchId
   *                  the unique ID under which the latch should be latched. If
   *                  {@code null} a unique ID is generated and if
   *                  the resource is being latched cluster-wide, other nodes are
   *                  latched with the same ID.
   * @return the latch
   */
  @Nonnull
  T acquireLatch(@Nonnull LatchMode latchMode, @Nullable String latchId);

  /**
   * @return the latch if the resource is currently latched; otherwise
   *         {@code null} if it isn't
   */
  @Nullable
  T getCurrentLatch();

  /**
   * Retrieves a flag indicating whether the resource is latched. When
   * {@code true} {@link #getCurrentLatch()} will
   * <i>generally</i> return a non-{@code null} {@link ILatch}. Any such code must
   * account for the race condition
   * implicit in calling 2 methods; the resource may be unlatched between the two
   * invocations.
   *
   * @return {@code true} if the resource is latched; otherwise {@code false}
   */
  boolean isLatched();

  /**
   * Retrieves the current state of the underling resource.
   *
   * @return the latch state
   * @since 3.4
   */
  LatchState getState();
}
