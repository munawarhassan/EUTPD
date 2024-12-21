package com.pmi.tpd.core.backup.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when a backup is canceled by a system administrator.
 * <p>
 * This event is internally audited with a HIGH priority.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class BackupCanceledEvent extends BackupEndedEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public BackupCanceledEvent(@Nonnull final Object source) {
    super(source);
  }
}
