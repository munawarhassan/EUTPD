package com.pmi.tpd.core.maintenance.event;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.api.event.annotation.EventName;

/**
 * Fired when maintenance has begun.
 * <p>
 * On receiving this event plugins must avoid running any operations that may use database connections or alter the
 * product's {@link ApplicationPropertiesService#getHomeDir() home} or
 * {@link ApplicationPropertiesService#getSharedHomeDir() shared home} until maintenance has
 * {@link MaintenanceEndedEvent ended}.
 * <p>
 * Operations running when the event is fired should be terminated at the earliest opportunity. Scheduled operations
 * (that are not scheduled through Atlassian APIs) should be disabled until maintenance is complete.
 * <p>
 * Failure to observe this may result in used database connections becoming invalidated, may prevent the product from
 * starting its maintenance task or in the worst case may make the maintenance tasks (such as backup or database
 * migration) complete unreliable.
 *
 * @since 3.7
 */
@AsynchronousPreferred
@EventName("app.maintenance.started")
public class MaintenanceStartedEvent extends MaintenanceEvent {

    /**
     *
     */
    private static final long serialVersionUID = -1769609557600444734L;

    public MaintenanceStartedEvent(@Nonnull final Object source) {
        super(source);
    }
}
