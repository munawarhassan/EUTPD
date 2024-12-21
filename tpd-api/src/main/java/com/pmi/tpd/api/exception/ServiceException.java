package com.pmi.tpd.api.exception;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * {@code ServiceException} is the abstract class for all exceptions that be
 * seen by end user.
 * <p>
 * Subclasses of {@code ServiceException} are <em>unchecked exceptions</em>.
 * Unchecked exceptions do <em>not</em> need
 * to be declared in a method or constructor's {@code throws} clause if they can
 * be thrown by the execution of the
 * method or constructor and propagate outside the method or constructor
 * boundary.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class ServiceException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 7477448637411850141L;

  /** */
  private final String localizedMessage;

  /** */
  private final String messageKey;

  /** */
  private transient KeyedMessage keyedMessage;

  /**
   * Constructs a new service exception with the specified i18n detail message.
   *
   * @param message
   *                the i18n detail message. The detail message is saved for later
   *                retrieval by the {@link #getMessage()}
   *                method.
   */
  public ServiceException(@Nonnull final KeyedMessage message) {
    this(message, null);
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
  public ServiceException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
    super(checkNotNull(message, "message").getRootMessage(), cause);

    this.localizedMessage = message.getLocalisedMessage();
    this.messageKey = message.getKey();

    keyedMessage = message;
  }

  /**
   * @return Returns the i18n detail message. The detail message is saved for
   *         later retrieval by the
   *         {@link #getMessage()} method.
   */
  @Nonnull
  public KeyedMessage getKeyedMessage() {
    if (keyedMessage == null) {
      // We may have been serialized and lost the message; reconstruct it
      keyedMessage = new KeyedMessage(messageKey, localizedMessage, getMessage());
    }

    return keyedMessage;
  }

  @Nonnull
  @Override
  public String getLocalizedMessage() {
    return localizedMessage;
  }

  /**
   * @return Returns a {@link String} representing the i18n message key of
   *         message.
   */
  @Nonnull
  public String getMessageKey() {
    return messageKey;
  }
}
