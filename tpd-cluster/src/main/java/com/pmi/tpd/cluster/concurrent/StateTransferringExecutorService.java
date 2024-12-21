package com.pmi.tpd.cluster.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;

/**
 * A wrapper around the standard Java {@code ExecutorService} interface which
 * captures the state of the current thread
 * at the time a job is executed and applies that state to and removes that
 * state from the worker thread on which the
 * job is executed.
 * <p>
 * Note: When using {@code invokeAll} or {@code invokeAny}, the current thread's
 * state will be captured <i>once</i>. The
 * same snapshot will then be applied to each worker when a job is executed.
 *
 * @author Christophe Friederich @since1.3
 */
public class StateTransferringExecutorService extends StateTransferringExecutor implements ExecutorService {

  /** */
  private final ExecutorService delegate;

  /** */
  private final Logger logger = LoggerFactory.getLogger(StateTransferringExecutorService.class);

  /**
   * @param delegate
   */
  public StateTransferringExecutorService(final ExecutorService delegate) {
    super(delegate);

    this.delegate = delegate;
  }

  @Override
  public boolean awaitTermination(final long timeout, @Nonnull final TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  @Nonnull
  @Override
  public <T> List<Future<T>> invokeAll(@Nonnull final Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return delegate.invokeAll(wrap(tasks));
  }

  @Nonnull
  @Override
  public <T> List<Future<T>> invokeAll(@Nonnull final Collection<? extends Callable<T>> tasks,
      final long timeout,
      @Nonnull final TimeUnit unit) throws InterruptedException {
    return delegate.invokeAll(wrap(tasks), timeout, unit);
  }

  @Nonnull
  @Override
  public <T> T invokeAny(@Nonnull final Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    return delegate.invokeAny(wrap(tasks));
  }

  @Override
  public <T> T invokeAny(@Nonnull final Collection<? extends Callable<T>> tasks,
      final long timeout,
      @Nonnull final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return delegate.invokeAny(wrap(tasks), timeout, unit);
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public void shutdown() {
    try {
      delegate.shutdown();
    } finally {
      onShutdown();
    }
  }

  @Override
  public List<Runnable> shutdownNow() {
    try {
      return delegate.shutdownNow();
    } finally {
      onShutdown();
    }
  }

  @Nonnull
  @Override
  public <T> Future<T> submit(@Nonnull final Callable<T> callable) {
    return delegate.submit(wrap(callable));
  }

  @Nonnull
  @Override
  public <T> Future<T> submit(@Nonnull final Runnable runnable, final T t) {
    return delegate.submit(wrap(runnable), t);
  }

  @Nonnull
  @Override
  public Future<?> submit(@Nonnull final Runnable runnable) {
    return delegate.submit(wrap(runnable));
  }

  protected <T> Callable<T> wrap(final Callable<T> callable) {
    return new StateTransferringCallable<>(callable, getState());
  }

  protected <T> Collection<Callable<T>> wrap(final Collection<? extends Callable<T>> callables) {
    final ITransferableState state = getState();

    return Collections2.transform(callables, callable -> new StateTransferringCallable<>(callable, state));
  }

  private void onShutdown() {
    // clear any strong references to the services on shutdown;
    // note that any still executing callable(s) on the executor are safe because
    // the callable(s) have
    // their own copies of the list of services in their CompositeTransferableState;
    // hence it is safe to remove the references on this executor assuming that new
    // jobs will be spawned.
    clearServices();
  }

  protected class StateTransferringCallable<T> implements Callable<T> {

    private final Callable<T> delegate;

    private final ITransferableState state;

    public StateTransferringCallable(final Callable<T> delegate, final ITransferableState state) {
      this.delegate = delegate;
      this.state = state;
    }

    @Override
    public T call() throws Exception {
      try {
        state.apply();
        try {
          return delegate.call();
        } finally {
          state.remove();
        }
      } catch (final Exception e) {
        logger.debug("Error while processing asynchronous task", e);
        throw e;
      } catch (final Error e) {
        logger.debug("Error while processing asynchronous task", e);
        throw e;
      }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof StateTransferringCallable)) {
        return false;
      }

      final StateTransferringCallable that = (StateTransferringCallable) o;

      return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
      return delegate.hashCode();
    }
  }
}
