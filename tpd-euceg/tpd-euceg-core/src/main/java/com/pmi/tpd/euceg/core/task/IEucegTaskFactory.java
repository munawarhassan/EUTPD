package com.pmi.tpd.euceg.core.task;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.scheduler.ITaskFactory;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface IEucegTaskFactory extends ITaskFactory {

    @Nonnull
    TrackingReportPhase.Builder trackingReportPhaseBuilder(@Nonnull ITrackingReportState state);

    @Nonnull
    SubmissionTackingReportTask trackingReportTask(@Nonnull ITrackingReportState state);

    @Nonnull
    SubmissionTrackingReportStep trackingReportStep(@Nonnull ITrackingReportState state);

}
