package com.pmi.tpd.core.server;

import javax.annotation.Nonnull;

/**
 * Monitor for the state of the server. Used by conditions to determine when to display warning banners to the user.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IApplicationStatusService {

    /**
     * @return the current state of the server
     */
    @Nonnull
    ApplicationState getState();

    /**
     * Retrieves a flag indicating whether a recent request has failed. The definition of "recent" is left to the
     * implementation, but should be within the last few minutes.
     *
     * @return {@code true} if acquiring a ticket has been rejected; otherwise, {@code false}
     */
    boolean hasRecentlyRejectedRequests();

    /**
     * Retrieves a flag indicating whether a recent request has been queued for longer than the timeout.
     *
     * @return {@code true} if acquiring a ticket has been queued for more than the timeout otherwise, {@code false}
     */
    boolean isQueueingRequests();
}
