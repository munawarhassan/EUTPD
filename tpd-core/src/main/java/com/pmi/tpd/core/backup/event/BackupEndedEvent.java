package com.pmi.tpd.core.backup.event;

import javax.annotation.Nonnull;

/**
 * An abstract base class for constructing events raised when a backup ends,
 * successfully or not.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class BackupEndedEvent extends BackupEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  protected BackupEndedEvent(@Nonnull final Object source) {
    super(source);
  }
}
