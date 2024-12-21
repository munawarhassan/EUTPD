package com.pmi.tpd.core.maintenance.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;

/**
 * A base class for maintenance events.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class MaintenanceEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = -4474751658983121738L;

    /**
     * @param source
     */
    protected MaintenanceEvent(@Nonnull final Object source) {
        super(source);
    }
}
