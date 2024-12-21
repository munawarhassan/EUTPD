package com.pmi.tpd.core.backup.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a backup completes successfully.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class BackupSucceededEvent extends BackupEndedEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public BackupSucceededEvent(@Nonnull final Object source) {
    super(source);
  }
}
