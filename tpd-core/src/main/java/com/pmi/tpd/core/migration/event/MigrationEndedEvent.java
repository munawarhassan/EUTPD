package com.pmi.tpd.core.migration.event;

import javax.annotation.Nonnull;

/**
 * An abstract base class for constructing events raised when database migration ends, successfully or not.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class MigrationEndedEvent extends MigrationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected MigrationEndedEvent(@Nonnull final Object source) {
        super(source);
    }
}
