package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * MaintenanceService extension with internal methods for locking and unlocking individual nodes for maintenance.
 */
public interface IInternalMaintenanceService extends IMaintenanceService {

    /**
     * Clears the cluster lock. This should only be one <em>after</em> all the node-local maintance locks have been
     * unlocked.
     */
    void clearClusterLock();

    /**
     * @return the node-local {@link IMaintenanceLock} if the node is locked
     */
    @Nullable
    IMaintenanceLock getNodeLock();

    /**
     * Locks the local node.
     *
     * @param maintenanceLock
     *            the lock information (unlock token, owner, etc)
     */
    void lockNode(@Nonnull IMaintenanceLock maintenanceLock);
}
