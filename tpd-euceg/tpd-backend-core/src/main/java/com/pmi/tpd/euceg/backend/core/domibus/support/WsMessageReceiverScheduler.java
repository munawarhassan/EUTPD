package com.pmi.tpd.euceg.backend.core.domibus.support;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.ws.WebServiceException;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IJobRunnerRequest;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.euceg.backend.core.BackendProperties;

/**
 * Schedules a job to treat received message response.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class WsMessageReceiverScheduler implements IScheduledJobSource {

    /** */
    private static final JobId CLEANUP_JOB_ID = JobId.of(MessageReceiverSchedulerJob.class.getSimpleName());

    /** */
    private static final JobRunnerKey CLEANUP_JOB_RUNNER_KEY = JobRunnerKey
            .of(MessageReceiverSchedulerJob.class.getName());

    /** */
    private volatile AbstractWsMessageSender<?, ?> sender;

    private final IApplicationProperties applicationProperties;

    /** */
    private long intervalSeconds = 30; // default

    /**
     * Default constructor.
     *
     * @param submissionService
     *                          a submission service.
     */
    @Inject
    public WsMessageReceiverScheduler(@Nonnull final AbstractWsMessageSender<?, ?> submissionService,
            @Nonnull final IApplicationProperties applicationProperties) {
        this.sender = checkNotNull(submissionService, "submissionService");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        final BackendProperties properties = applicationProperties.getConfiguration(BackendProperties.class);
        setInterval(properties.getWsOptions().getPendingInterval());
        schedulerService.registerJobRunner(CLEANUP_JOB_RUNNER_KEY, new MessageReceiverSchedulerJob());

        final long intervalMillis = TimeUnit.SECONDS.toMillis(intervalSeconds);
        schedulerService.scheduleJob(CLEANUP_JOB_ID,
            JobConfig.forJobRunnerKey(CLEANUP_JOB_RUNNER_KEY)
                    .withRunMode(RunMode.RUN_LOCALLY)
                    .withSchedule(
                        Schedule.forInterval(intervalMillis, new Date(System.currentTimeMillis() + intervalMillis))));
    }

    /**
     * Sets the minimum time interval (in seconds) between runs.
     *
     * @param intervalSeconds
     *                        the interval in seconds.
     */
    public void setInterval(final long intervalSeconds) {
        this.intervalSeconds = Math.max(1, intervalSeconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        schedulerService.unregisterJobRunner(CLEANUP_JOB_RUNNER_KEY);
    }

    /**
     * @author Christophe Friederich
     */
    private class MessageReceiverSchedulerJob implements IJobRunner {

        @Nullable
        @Override
        public JobRunnerResponse runJob(final @Nonnull IJobRunnerRequest request) {
            try {
                sender.updatePendingMessage();
            } catch (final WebServiceException ex) {
                return JobRunnerResponse.failed("Problem connection with Domibus");
            }
            return JobRunnerResponse.success();
        }
    }
}
