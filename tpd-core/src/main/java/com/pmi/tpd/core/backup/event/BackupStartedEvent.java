package com.pmi.tpd.core.backup.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when the system starts creating a new backup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class BackupStartedEvent extends BackupEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public BackupStartedEvent(@Nonnull final Object source) {
    super(source);
  }
}
