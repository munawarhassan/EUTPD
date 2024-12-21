package com.pmi.tpd.api.util;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.profiling.ProfilingTimerStack;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class TimerUtils {

  private TimerUtils() {
    throw new UnsupportedOperationException(
        getClass().getName() + " is a utility class and should not be instantiated");
  }

  /**
   * @return true if profiling is currently enabled
   */
  public static boolean isActive() {
    return ProfilingTimerStack.isActive();
  }

  /**
   * Creates a {@link Timer} which can be used to safely stop timing.
   * <p>
   * The normal usage pattern should look like this: <code><pre>
   *
   * try (Timer timer = TimerUtils.start("some string")) {
   *     // Do something
   * }
   * </pre></code>
   * </p>
   *
   * @param name
   *             of the timer
   * @return a {@code Timer} callback
   */
  public static Timer start(@Nonnull final String name) {
    return new DefaultTimer(name);
  }

  /**
   * Time the specified operation.
   * <p>
   * See also the example in {@link #start} for doing the same without having to
   * construct an Operation object.
   *
   * @param name
   *                 of the timer
   * @param supplier
   *                 which will be timed
   * @return the result of {@code operation}
   */
  public static <T, E extends Throwable> T time(final String name, final IOperation<T, E> supplier) throws E {
    ProfilingTimerStack.push(name);
    try {
      return supplier.perform();
    } finally {
      ProfilingTimerStack.pop(name);
    }
  }

  private static final class DefaultTimer implements Timer {

    /** */
    private String currentName;

    /** */
    private boolean stopped;

    private DefaultTimer(final String currentName) {
      this.currentName = checkNotNull(currentName, "currentName");
      ProfilingTimerStack.push(currentName);
    }

    @Override
    public void close() {
      if (stopped) {
        throw new IllegalStateException("Stop should only be called once");
      }
      ProfilingTimerStack.pop(currentName);
      stopped = true;
    }

    @Override
    public void mark(@Nonnull final String newName) {
      checkNotNull(newName, "newName");
      ProfilingTimerStack.pop(currentName);
      currentName = newName;
      ProfilingTimerStack.push(newName);
    }

  }

}
