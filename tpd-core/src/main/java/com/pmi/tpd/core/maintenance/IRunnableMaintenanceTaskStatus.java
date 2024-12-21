package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import com.pmi.tpd.scheduler.exec.IRunnableTaskStatus;

public interface IRunnableMaintenanceTaskStatus extends IRunnableTaskStatus {

    /**
     * @return the type of the task
     * @see MaintenanceType
     */
    @Nonnull
    MaintenanceType getType();

}
