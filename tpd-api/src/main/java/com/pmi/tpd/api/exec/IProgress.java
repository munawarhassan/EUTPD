package com.pmi.tpd.api.exec;

import javax.annotation.Nonnull;

/**
 * A representation of the current progress of a task.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IProgress {

  /**
   * @return the message for the current progress.
   */
  @Nonnull
  String getMessage();

  /**
   * @return the percentage complete of the associated task. Will always be an
   *         {@code int} between 0 and 100
   *         (inclusive)
   */
  int getPercentage();

}
