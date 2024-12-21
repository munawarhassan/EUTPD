package com.pmi.tpd.api.exception;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Thrown when a argument(s) component of application has failed during the
 * validation.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ArgumentValidationException extends ServiceException {

  /** */
  private static final long serialVersionUID = 1;

  /**
   * Constructs a new argument exception with the specified i18n detail message.
   *
   * @param message
   *                the i18n detail message. The detail message is saved for later
   *                retrieval by the {@link #getMessage()}
   *                method.
   */
  public ArgumentValidationException(@Nonnull final KeyedMessage message) {
    super(message);
  }
}
