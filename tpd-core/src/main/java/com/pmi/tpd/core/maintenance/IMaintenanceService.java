package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exec.IRunnableTask;

/**
 * Defines a service for monitoring system maintenance and managing the maintenance lock.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMaintenanceService {

    /**
     * If the system is {@link #lock() locked} for maintenance, retrieves a {@link IMaintenanceLock lock} which can be
     * used to track its actions or unlock it.
     *
     * @return the active lock, or {@code null} if the system is not locked
     */
    @Nullable
    IMaintenanceLock getLock();

    /**
     * If maintenance is in progress, retrieves a {@link ITaskMaintenanceMonitor task handle} which can be used to track its
     * progress, wait for its completion or to cancel it.
     *
     * @return task for the in-progress maintenance, or {@code null} if no maintenance is in progress
     * @see #start(MaintenanceTask, MaintenanceType)
     */
    @Nullable
    ITaskMaintenanceMonitor getRunningTask();

    /**
     * Retrieves the current status of the maintenance service.
     *
     * @return the current status
     */
    @Nonnull
    IMaintenanceStatus getStatus();

    /**
     * Locks the system for maintenance. While the system is locked, users will not be able to access <i>any</i>
     * functionality, such as (but not limited to):
     * <ul>
     * <li>Cloning or fetching from, or pushing to, repositories</li>
     * <li>Pull requests</li>
     * <li>Normal administration screens</li>
     * </ul>
     * When the system is locked, a magic token is generated which can be used to unlock it again. The token may also be
     * used to bypass the lock and access the system anyway.
     *
     * @return the lock
     * @see IMaintenanceLock#unlock(String)
     */
    @Nonnull
    IMaintenanceLock lock();

    /**
     * Starts processing for the provided {@link MaintenanceTask task} and returns a
     * {@code MaintenanceTaskMonitor task handle} which can be used to track its progress, wait for its completion or
     * retrieve its output.
     *
     * @param task
     *            the task to start
     * @return the task handle which may be used to register listeners or wait for the task to complete
     * @throws IllegalStateException
     *             if another task is already in progress, or if the method is called outside the context of a user
     *             request
     */
    @Nonnull
    ITaskMaintenanceMonitor start(@Nonnull IRunnableTask task, @Nonnull MaintenanceType type);
}
