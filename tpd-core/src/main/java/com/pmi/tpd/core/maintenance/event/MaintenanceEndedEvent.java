package com.pmi.tpd.core.maintenance.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.api.event.annotation.EventName;

/**
 * Fired when maintenance has ended. Plugins may resume using database connections and other resources at this point.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
@EventName("app.maintenance.ended")
public class MaintenanceEndedEvent extends MaintenanceEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1728219856470666445L;

    public MaintenanceEndedEvent(@Nonnull final Object source) {
        super(source);
    }
}
