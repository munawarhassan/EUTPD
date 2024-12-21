package com.pmi.tpd.api.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class NoSuchEntityException extends ServiceException {

  /**
   *
   */
  private static final long serialVersionUID = -3969927798296335762L;

  /** */

  public NoSuchEntityException(@Nonnull final KeyedMessage message) {
    super(message);
  }

  public NoSuchEntityException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
