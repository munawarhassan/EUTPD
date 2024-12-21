package com.pmi.tpd.core.migration.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;

/**
 * Raised when database migration completes successfully. When this event is raised, the system is running on the new
 * database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class MigrationSucceededEvent extends MigrationEndedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MigrationSucceededEvent(@Nonnull final Object source) {
        super(source);
    }
}
