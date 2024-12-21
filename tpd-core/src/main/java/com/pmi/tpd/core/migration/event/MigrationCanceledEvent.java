package com.pmi.tpd.core.migration.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when database migration is canceled.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class MigrationCanceledEvent extends MigrationEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MigrationCanceledEvent(@Nonnull final Object source) {
        super(source);
    }
}
