package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Supplier;

/**
 * A container to hold the latest status for runnable tasks.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMaintenanceTaskStatusSupplier extends Supplier<IRunnableMaintenanceTaskStatus> {

    /**
     * @return the status of the latest task
     */
    @Nullable
    @Override
    IRunnableMaintenanceTaskStatus get();

    /**
     * Set the status of the latest task.
     *
     * @param status
     *               the new status
     */
    void set(@Nonnull IRunnableMaintenanceTaskStatus status);
}
