package com.pmi.tpd.cluster.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Indicates an error when using locks.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LockException extends ServiceException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LockException(@Nonnull final KeyedMessage message) {
    super(message);
  }

  public LockException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
