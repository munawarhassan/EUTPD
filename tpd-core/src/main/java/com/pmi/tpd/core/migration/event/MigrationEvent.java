package com.pmi.tpd.core.migration.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;

/**
 * An abstract base class for constructing events related to migrating the system between databases.
 * <p>
 * This event is internally audited with a HIGH priority.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class MigrationEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected MigrationEvent(@Nonnull final Object source) {
        super(source);
    }
}
