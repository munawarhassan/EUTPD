package com.pmi.tpd.cluster.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * A wrapper around the standard Java {@code ScheduledExecutorService} interface
 * which captures the state of the current
 * thread at the time a job is executed and applies that state to and removes
 * that state from the worker thread on which
 * the job is executed.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class StateTransferringScheduledExecutorService extends StateTransferringExecutorService
    implements ScheduledExecutorService {

  /** */
  private final ScheduledExecutorService delegate;

  /**
   * @param delegate
   */
  public StateTransferringScheduledExecutorService(final ScheduledExecutorService delegate) {
    super(delegate);

    this.delegate = delegate;
  }

  @Nonnull
  @Override
  public ScheduledFuture<?> schedule(@Nonnull final Runnable runnable,
      final long delay,
      @Nonnull final TimeUnit unit) {
    return delegate.schedule(wrap(runnable), delay, unit);
  }

  @Nonnull
  @Override
  public <V> ScheduledFuture<V> schedule(@Nonnull final Callable<V> callable,
      final long delay,
      @Nonnull final TimeUnit unit) {
    return delegate.schedule(wrap(callable), delay, unit);
  }

  @Nonnull
  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull final Runnable runnable,
      final long initialDelay,
      final long period,
      @Nonnull final TimeUnit unit) {
    return delegate.scheduleAtFixedRate(wrap(runnable), initialDelay, period, unit);
  }

  @Nonnull
  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull final Runnable runnable,
      final long initialDelay,
      final long delay,
      @Nonnull final TimeUnit unit) {
    return delegate.scheduleWithFixedDelay(wrap(runnable), initialDelay, delay, unit);
  }
}
