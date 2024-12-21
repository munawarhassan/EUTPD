package com.pmi.tpd.core.migration.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when database migration begins.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class MigrationStartedEvent extends MigrationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MigrationStartedEvent(@Nonnull final Object source) {
        super(source);
    }
}
