package com.pmi.tpd.core.migration.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when database migration fails.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class MigrationFailedEvent extends MigrationEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MigrationFailedEvent(@Nonnull final Object source) {
        super(source);
    }
}
