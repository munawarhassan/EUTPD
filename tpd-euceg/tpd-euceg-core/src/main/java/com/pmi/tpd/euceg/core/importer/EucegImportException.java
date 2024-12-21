package com.pmi.tpd.euceg.core.importer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Thrown when the import has failed.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class EucegImportException extends RuntimeException {

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
   * Constructs a new EUCEG import exception with the specified i18n detail
   * message.
   *
   * @param message
   *                the i18n detail message. The detail message is saved for later
   *                retrieval by the {@link #getMessage()}
   *                method.
   */
  public EucegImportException(@Nonnull final KeyedMessage message) {
    this(message, null);
  }

  /**
   * Constructs a new EUCEG import exception with the specified i18n detail
   * message.
   *
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
  public EucegImportException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
    super(Preconditions.checkNotNull(message, "message is required").getRootMessage(), cause);

    this.localizedMessage = message.getLocalisedMessage();
    this.messageKey = message.getKey();

    keyedMessage = message;
  }

  /**
   * @return Returns the i18n detail message.
   */
  @Nonnull
  public KeyedMessage getKeyedMessage() {
    if (keyedMessage == null) {
      // We may have been serialised and lost the message; reconstruct it
      keyedMessage = new KeyedMessage(messageKey, localizedMessage, getMessage());
    }

    return keyedMessage;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public String getLocalizedMessage() {
    return localizedMessage;
  }

  /**
   * @return Returns a string representing the message key associated to.
   */
  @Nonnull
  public String getMessageKey() {
    return messageKey;
  }
}
