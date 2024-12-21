package com.pmi.tpd.api.exec;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.util.Assert;

/**
 * A default immutable implementation of the {@link Progress} interface.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ProgressImpl implements IProgress, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final String message;

  /** */
  private final int percentage;

  /**
   * @param message
   * @param percentage
   */
  public ProgressImpl(@Nonnull final String message, final int percentage) {
    Assert.checkNotNull(message, "message cannot be null");
    Assert.isTrue(percentage >= 0, "percentage cannot be negative");
    Assert.isTrue(percentage <= 100, "percentage cannot greater than 100");
    this.message = message;
    this.percentage = percentage;
  }

  public ProgressImpl(@Nonnull final IProgress progress) {
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("message", message).add("percentage", percentage).toString();
  }
}
