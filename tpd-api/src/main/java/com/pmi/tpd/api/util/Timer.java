package com.pmi.tpd.api.util;

import javax.annotation.Nonnull;

/**
 * A callback object for consumers of {@link TimerUtils#start(String)}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface Timer extends AutoCloseable {

  /**
   * Stop the timer when the operation has complete.
   */
  @Override
  void close();

  /**
   * Stop the current {@link Timer} and start a new one.
   * <p>
   * This is equivalent to {@link #close() closing} the current timer and
   * {@link TimerUtils#start(String) starting} a
   * new one.
   *
   * @param newName
   *                the new name of the timer
   */
  void mark(@Nonnull String newName);

}
