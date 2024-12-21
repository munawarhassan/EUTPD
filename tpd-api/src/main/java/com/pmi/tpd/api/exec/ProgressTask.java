package com.pmi.tpd.api.exec;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * A default immutable implementation of the {@link IProgress} interface.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ProgressTask implements IProgress, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final String message;

  /** */
  private final int percentage;

  public ProgressTask(@Nonnull final String message, final int percentage) {
    Assert.checkNotNull(message, "message");
    Assert.isTrue(percentage >= 0, "percentage cannot be negative");
    Assert.isTrue(percentage <= 100, "percentage cannot greater than 100");
    this.message = message;
    this.percentage = percentage;
  }

  /**
   */
  public ProgressTask(@Nonnull final IProgress progress) {
    this(Assert.checkNotNull(progress, "progress").getMessage(), progress.getPercentage());
  }

  @Nonnull
  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public int getPercentage() {
    return percentage;
  }
}
