package com.pmi.tpd.core.euceg.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.ISendAwaitingPayloadJob;
import com.pmi.tpd.euceg.backend.core.BackendProperties;

/**
 * Schedules a job to send awaiting payload.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class SendAwaitPayloadScheduler implements IScheduledJobSource {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SendAwaitPayloadScheduler.class);

    /** */
    private static final JobId SEND_PENDING_JOB_ID = JobId.of(SendAwaitPayloadSchedulerJob.class.getSimpleName());

    /** */
    private static final JobRunnerKey SEND_PENDING_JOB_RUNNER_KEY = JobRunnerKey
            .of(SendAwaitPayloadSchedulerJob.class.getName());

    /** */
    private volatile ISendAwaitingPayloadJob awaitingPayloadJob;

    private final IApplicationProperties applicationProperties;

    /** */
    private long intervalSeconds = 30; // default

    /** */
    private int sendBatchSize = 10; // default

    /**
     * Default constructor.
     *
     * @param submissionService
     *                          the submission service.
     */
    @Inject
    public SendAwaitPayloadScheduler(@Nonnull final ISendAwaitingPayloadJob awaitingPayloadJob,
            @Nonnull final IApplicationProperties applicationProperties) {
        this.awaitingPayloadJob = Assert.checkNotNull(awaitingPayloadJob, "awaitingPayloadJob");
        this.applicationProperties = Assert.checkNotNull(applicationProperties, "applicationProperties");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        Assert.checkNotNull(schedulerService, "schedulerService");
        final BackendProperties properties = applicationProperties.getConfiguration(BackendProperties.class);
        setInterval(properties.getOptions().getSendInterval());
        setSendBatchSize(properties.getOptions().getSendBatchSize());

        schedulerService.registerJobRunner(SEND_PENDING_JOB_RUNNER_KEY, new SendAwaitPayloadSchedulerJob());

        final long intervalMillis = TimeUnit.SECONDS.toMillis(intervalSeconds);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting Send Await Payload Scheduler Job : intervalSeconds:{},sendBatchSize: {}",
                sendBatchSize);
        }
        schedulerService.scheduleJob(SEND_PENDING_JOB_ID,
            JobConfig.forJobRunnerKey(SEND_PENDING_JOB_RUNNER_KEY)
                    .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
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
        this.intervalSeconds = Math.max(5, intervalSeconds);
    }

    public void setSendBatchSize(final int sendBatchSize) {
        this.sendBatchSize = Math.max(10, sendBatchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        Assert.checkNotNull(schedulerService, "schedulerService");
        schedulerService.unregisterJobRunner(SEND_PENDING_JOB_RUNNER_KEY);
    }

    /**
     * @author Christophe Friederich
     */
    private class SendAwaitPayloadSchedulerJob implements IJobRunner {

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public JobRunnerResponse runJob(final @Nonnull IJobRunnerRequest request) {
            awaitingPayloadJob.sendAwaitPayload(sendBatchSize);
            return JobRunnerResponse.success();
        }
    }
}
