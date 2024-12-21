package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.scheduler.ITaskFactory;

public interface ITaskMaintenanceFactory extends ITaskFactory {

    @Nonnull
    MaintenanceModePhase.Builder maintenanceModePhaseBuilder();

}
