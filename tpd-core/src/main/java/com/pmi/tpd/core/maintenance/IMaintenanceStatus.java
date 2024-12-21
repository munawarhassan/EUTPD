package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.cluster.latch.LatchState;
import com.pmi.tpd.scheduler.exec.IRunnableTaskStatus;

/**
 * Represents the current state of application maintenance system.
 */
public interface IMaintenanceStatus {

    /**
     * If maintenance is in progress, retrieves the current {@link IRunnableTaskStatus task} which can be used to track
     * its status. If maintenance is not in progress, retrieves the most-recently run {@link IRunnableTaskStatus task}
     * if any.
     *
     * @return task for the in-progress maintenance else the most-recently run maintenance else {@code null} if no
     *         maintenance has been run
     * @see IMaintenanceService#start(com.pmi.tpd.core.exec.IRunnableTask, MaintenanceType)
     */
    @Nullable
    IRunnableTaskStatus getLatestTask();

    /**
     * @return the current state of the database with respect to maintenance
     */
    @Nonnull
    LatchState getDatabaseState();
}
