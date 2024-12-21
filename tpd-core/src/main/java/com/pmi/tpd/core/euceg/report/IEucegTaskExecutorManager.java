package com.pmi.tpd.core.euceg.report;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.task.ITrackingReportState;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;

public interface IEucegTaskExecutorManager {

    @Nonnull
    ITaskMonitor trackingReport(@Nonnull ITrackingReportState state);

    /**
     * @param id
     * @return
     */
    Optional<ITaskMonitor> getTaskMonitor(@Nonnull String cancelToken);

    void cancelTask(@Nonnull String cancelToken);
}
