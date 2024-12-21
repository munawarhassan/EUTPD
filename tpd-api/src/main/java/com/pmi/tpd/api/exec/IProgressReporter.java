package com.pmi.tpd.api.exec;

import javax.annotation.Nonnull;

/**
 * A reporter for the current status of a task.
 */
public interface IProgressReporter {

  /**
   * Gets the name of reporter
   *
   * @return
   */
  String getName();

  /**
   * @return the current status of the underlying task
   */
  @Nonnull
  IProgress getProgress();
}
