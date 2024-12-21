package com.pmi.tpd.api.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Base class for mail exceptions. Instance of this exception will be logged to
 * the mail log along with details of the
 * mail message being sent. It is at the discretion of the caller whether these
 * should be logged elsewhere.
 *
 * @since 2.0
 */
public class MailException extends ServiceException {

  /**
   *
   */
  private static final long serialVersionUID = -3858525073104495399L;

  /**
   * @param message
   *                the i18n detail message. The detail message is saved for later
   *                retrieval by the {@link #getMessage()}
   *                method.
   */
  public MailException(@Nonnull final KeyedMessage message) {
    super(message);
  }

  /**
   * @param message
   *                the i18n detail message. The detail message is saved for later
   *                retrieval by the {@link #getMessage()}
   *                method.
   * @param cause
   *                the cause (which is saved for later retrieval by the
   *                {@link #getCause()} method). (A <tt>null</tt>
   *                value is permitted, and indicates that the cause is
   *                nonexistent or unknown.)
   */
  public MailException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
