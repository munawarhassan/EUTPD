package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;

/**
 * A helper for locking and unlocking the system using a provided {@link MaintenanceApplicationEvent}. The primary goal
 * of this helper is to allow locking to have different strategies which can be applied as necessary.
 * <p>
 * When used in a cluster, this helper will lock the <i>entire cluster</i>, not just the local node.
 * </p>
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMaintenanceModeHelper {

    /**
     * Locks the system, using the provided event as a key.
     *
     * @param event
     *            the key event
     */
    void lock(@Nonnull MaintenanceApplicationEvent event);

    /**
     * Unlocks the system <i>if it is locked using the provided event</i>. If another event was used to lock the system,
     * it will not be unlocked.
     *
     * @param event
     *            the key event
     */
    void unlock(@Nonnull MaintenanceApplicationEvent event);
}
