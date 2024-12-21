package com.pmi.tpd.scheduler;

import static com.pmi.tpd.api.scheduler.JobRunnerResponse.aborted;
import static com.pmi.tpd.api.scheduler.JobRunnerResponse.failed;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.UNAVAILABLE;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.JobRunnerNotRegisteredException;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.config.IntervalScheduleInfo;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.scheduler.support.RunningJobImpl;

//CHECKSTYLE:OFF
/**
 * Scheduler implementations can (and should) use {@code JobLauncher} to invoke jobs when it is time to run them. It
 * will do the necessary checks to make sure that the job runner is registered, has the appropriate run mode, can
 * reconstruct the job's parameter map, and so on. If everything checks out, it calls the job runner's
 * {@link IJobRunner#runJob(JobRunnerRequest) runJob} method and records the the resulting RunDetails using
 * {@link AbstractSchedulerService#addRunDetails(JobId,Date,com.pmi.tpd.api.scheduler.status.RunOutcome,String)
 * addRunDetails}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
// CHECKSTYLE:ON
public class JobLauncher {

    /** */
    protected static final Logger LOG = LoggerFactory.getLogger(JobLauncher.class);

    /** */
    protected final AbstractSchedulerService schedulerService;

    /** */
    protected final RunMode schedulerRunMode;

    /** */
    @Nonnull
    protected final Date firedAt;

    /** */
    @Nonnull
    protected final JobId jobId;

    /** Derived (in this order). */
    private IJobDetails jobDetails;

    /** */
    private IJobRunner jobRunner;

    /** */
    private JobConfig jobConfig;

    /** */
    private JobRunnerResponse response;

    /**
     * Creates a job launcher to handle the running of a scheduled job.
     *
     * @param schedulerService
     *                         the scheduler that is invoking the job
     * @param schedulerRunMode
     *                         the expected {@link RunMode run mode} for the jobs that this scheduler owns
     * @param firedAt
     *                         the time that the job was started, if known; may be {@code null}, in which case the
     *                         current time is used
     * @param jobId
     *                         the job ID of the job to run
     */
    public JobLauncher(@Nonnull final AbstractSchedulerService schedulerService,
            @Nonnull final RunMode schedulerRunMode, @Nullable final Date firedAt, @Nonnull final JobId jobId) {
        this(schedulerService, schedulerRunMode, firedAt, jobId, null);
    }

    /**
     * Creates a job launcher to handle the running of a scheduled job.
     *
     * @param schedulerService
     *                         the scheduler that is invoking the job
     * @param schedulerRunMode
     *                         the expected {@link RunMode run mode} for the jobs that this scheduler owns
     * @param firedAt
     *                         the time that the job was started, if known; may be {@code null}, in which case the
     *                         current time is used
     * @param jobId
     *                         the job ID of the job to run
     * @param jobDetails
     *                         the already loaded job details, if they are available
     */
    public JobLauncher(@Nonnull final AbstractSchedulerService schedulerService,
            @Nonnull final RunMode schedulerRunMode, @Nullable final Date firedAt, @Nonnull final JobId jobId,
            @Nullable final IJobDetails jobDetails) {
        this.schedulerService = checkNotNull(schedulerService, "schedulerService");
        this.schedulerRunMode = checkNotNull(schedulerRunMode, "schedulerRunMode");
        this.firedAt = firedAt != null ? firedAt : new Date();
        this.jobId = checkNotNull(jobId, "jobId");
        this.jobDetails = jobDetails;
    }

    /**
     * Call this to validate the job, run it, and update its status.
     */
    public void launch() {
        LOG.debug("launch: {}: {}", schedulerRunMode, jobId);
        try {
            final JobRunnerResponse response = launchAndBuildResponse();
            schedulerService.addRunDetails(jobId, firedAt, response.getRunOutcome(), response.getMessage());
        } catch (final JobRunnerNotRegisteredException ex) {
            LOG.debug("Scheduled job with ID '{}' is unavailable because its job runner is not registered: {}",
                jobId,
                ex.getJobRunnerKey());
            schedulerService.addRunDetails(jobId,
                firedAt,
                UNAVAILABLE,
                "Job runner key '" + ex.getJobRunnerKey() + "' is not registered");
        }
        deleteIfRunOnce();
    }

    @Nonnull
    private JobRunnerResponse launchAndBuildResponse() throws JobRunnerNotRegisteredException {
        try {
            response = validate();
            if (response == null) {
                response = runJob();
            }
        } catch (final RuntimeException ex) {
            LOG.error("Scheduled job with ID '{}' failed", jobId, ex);
            response = failed(ex);
        } catch (final LinkageError err) {
            LOG.error("Scheduled job with ID '{}' failed due to binary incompatibilities", jobId, err);
            response = failed(err);
        }
        return response;
    }

    @Nonnull
    private JobRunnerResponse runJob() {
        final IRunningJob job = new RunningJobImpl(firedAt, jobId, jobConfig);

        final IRunningJob existing = schedulerService.enterJob(jobId, job);
        if (existing != null) {
            LOG.debug("Unable to start job {} because it is already running as {}", job, existing);
            return JobRunnerResponse.aborted("Already running");
        }

        schedulerService.preJob();
        final Thread thd = Thread.currentThread();
        final ClassLoader originalClassLoader = thd.getContextClassLoader();
        try {
            // Ensure that the Job runs with its own class loader set as the
            // thread's CCL
            thd.setContextClassLoader(jobRunner.getClass().getClassLoader());
            final JobRunnerResponse response = jobRunner.runJob(job);
            return response != null ? response : JobRunnerResponse.success();
        } finally {
            thd.setContextClassLoader(originalClassLoader);
            schedulerService.leaveJob(jobId, job);
            schedulerService.postJob();
        }
    }

    @Nullable
    private JobRunnerResponse validate() throws JobRunnerNotRegisteredException {
        JobRunnerResponse response = validateJobDetails();
        if (response == null) {
            response = validateJobRunner();
            if (response == null) {
                response = validateJobConfig();
            }
        }
        return response;
    }

    @Nullable
    private JobRunnerResponse validateJobDetails() {
        if (jobDetails == null) {
            jobDetails = schedulerService.getJobDetails(jobId);
            if (jobDetails == null) {
                return aborted("No corresponding job details");
            }
        }

        if (jobDetails.getRunMode() != schedulerRunMode) {
            return aborted(
                "Inconsistent run mode: expected '" + jobDetails.getRunMode() + "' got: '" + schedulerRunMode + '\'');
        }
        return null;
    }

    @Nullable
    private JobRunnerResponse validateJobRunner() throws JobRunnerNotRegisteredException {
        jobRunner = schedulerService.getJobRunner(jobDetails.getJobRunnerKey());
        if (jobRunner == null) {
            // We can't create a JobRunnerResponse for this...
            throw new JobRunnerNotRegisteredException(jobDetails.getJobRunnerKey());
        }
        return null;
    }

    @Nullable
    private JobRunnerResponse validateJobConfig() {
        try {
            jobConfig = JobConfig.forJobRunnerKey(jobDetails.getJobRunnerKey())
                    .withRunMode(jobDetails.getRunMode())
                    .withSchedule(jobDetails.getSchedule())
                    .withParameters(jobDetails.getParameters());
            return null;
        } catch (final SchedulerRuntimeException sre) {
            return aborted(jobDetails.toString());
        }
    }

    private void deleteIfRunOnce() {
        if (jobDetails != null) {
            final IntervalScheduleInfo info = jobDetails.getSchedule().getIntervalScheduleInfo();
            if (info != null && info.getIntervalInMillis() == 0L) {
                LOG.debug("deleteIfRunOnce: deleting completed job: {}", jobId);
                schedulerService.unscheduleJob(jobId);
            }
        }
    }

    @Override
    public String toString() {
        return "JobLauncher[jobId=" + jobId + ",jobDetails=" + jobDetails + ",response=" + response + ']';
    }
}
