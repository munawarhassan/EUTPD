package com.pmi.tpd.euceg.core.task;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import org.springframework.context.ApplicationContext;

import com.pmi.tpd.euceg.core.task.TrackingReportPhase.Builder;
import com.pmi.tpd.scheduler.exec.support.SpringTaskFactory;

public class DefaultEucegTaskFactory extends SpringTaskFactory implements IEucegTaskFactory {

    public DefaultEucegTaskFactory(final ApplicationContext applicationContext) {
        super(applicationContext);

    }

    @Override
    @Nonnull
    public Builder trackingReportPhaseBuilder(@Nonnull final ITrackingReportState state) {
        return create(TrackingReportPhase.Builder.class, "reportState", checkNotNull(state, "state"));
    }

    @Override
    @Nonnull
    public SubmissionTackingReportTask trackingReportTask(@Nonnull final ITrackingReportState state) {
        return create(SubmissionTackingReportTask.class, "reportState", checkNotNull(state, "state"));
    }

    @Override
    @Nonnull
    public SubmissionTrackingReportStep trackingReportStep(@Nonnull final ITrackingReportState state) {
        return create(SubmissionTrackingReportStep.class, "reportState", checkNotNull(state, "state"));
    }

}
