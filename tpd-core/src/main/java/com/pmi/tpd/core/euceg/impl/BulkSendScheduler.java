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
import com.pmi.tpd.core.euceg.ISendDeferredSubmissionJob;
import com.pmi.tpd.core.security.IEscalatedSecurityContext;
import com.pmi.tpd.core.security.ISecurityService;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.security.permission.Permission;

/**
 * Schedules a job to send deferred submission.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class BulkSendScheduler implements IScheduledJobSource {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkSendScheduler.class);

    /** */
    private static final JobId SEND_DEFERRED_JOB_ID = JobId.of(BulkSendSchedulerJob.class.getSimpleName());

    /** */
    private static final JobRunnerKey SEND_DEFERRED_JOB_RUNNER_KEY = JobRunnerKey
            .of(BulkSendSchedulerJob.class.getName());

    /** */
    private volatile ISendDeferredSubmissionJob deferredSubmissionJob;

    private final IApplicationProperties applicationProperties;

    /** */
    private volatile IEscalatedSecurityContext asUser;

    /** */
    private long intervalSeconds = 30; // default

    /** */
    private int batchSize = 5; // default

    /**
     * Default constructor.
     *
     * @param submissionService
     *                          a submission service.
     */
    @Inject
    public BulkSendScheduler(@Nonnull final ISendDeferredSubmissionJob deferredSubmissionJob,
            @Nonnull final ISecurityService securityService,
            @Nonnull final IApplicationProperties applicationProperties) {
        this.deferredSubmissionJob = Assert.checkNotNull(deferredSubmissionJob, "deferredSubmissionJob");
        this.applicationProperties = Assert.checkNotNull(applicationProperties, "applicationProperties");
        this.asUser = Assert.checkNotNull(securityService, "securityService")
                .withPermission(Permission.USER, "BulkSendScheduler");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        Assert.checkNotNull(schedulerService, "schedulerService");
        final BackendProperties properties = applicationProperties.getConfiguration(BackendProperties.class);
        setInterval(properties.getOptions().getDeferredInterval());
        setBatchSize(properties.getOptions().getDeferredBatchSize());
        schedulerService.registerJobRunner(SEND_DEFERRED_JOB_RUNNER_KEY, new BulkSendSchedulerJob());

        final long intervalMillis = TimeUnit.SECONDS.toMillis(intervalSeconds);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting Bulk Send Scheduler Job : intervalSeconds:{}, batchSize:{}",
                intervalSeconds,
                batchSize);
        }
        schedulerService.scheduleJob(SEND_DEFERRED_JOB_ID,
            JobConfig.forJobRunnerKey(SEND_DEFERRED_JOB_RUNNER_KEY)
                    .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
                    .withSchedule(
                        Schedule.forInterval(intervalMillis, new Date(System.currentTimeMillis() + intervalMillis))));
    }

    /**
     * the minimum time interval (in seconds) between runs.
     *
     * @param intervalSeconds
     *                        the interval in seconds.
     */
    public void setInterval(final long intervalSeconds) {
        this.intervalSeconds = Math.max(10, intervalSeconds);
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = Math.max(5, batchSize);;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        schedulerService.unregisterJobRunner(SEND_DEFERRED_JOB_RUNNER_KEY);
    }

    /**
     * @author Christophe Friederich
     */
    private class BulkSendSchedulerJob implements IJobRunner {

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public JobRunnerResponse runJob(final @Nonnull IJobRunnerRequest request) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Call send deferred submission job -> batchSize:{} ", batchSize);
            }

            asUser.call(() -> {
                deferredSubmissionJob.sendDeferredSubmission(batchSize);
                return null;
            });

            return JobRunnerResponse.success();
        }
    }
}
