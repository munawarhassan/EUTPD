package com.pmi.tpd.api.scheduler;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.scheduler.status.RunOutcome;

/**
 * Scheduler service.
 * <p>
 * This service provides the ability to schedule services for execution, either as a one time task, as a repeating task,
 * or according to a formal {@code cron}-style schedule.
 * </p>
 * <p>
 * Applications and add-ons define the scheduled work to perform by registering a {@link IJobRunner} with a
 * {@link JobRunnerKey unique key}. The scheduled work is performed according to individual
 * {@link #scheduleJob(JobId,JobConfig) scheduled jobs} that the define its {@link JobConfig configuration}, including
 * {@link Schedule when it should run} and any {@link JobConfig#getParameters() parameters} that the job runner needs to
 * know The work to be performed is registered as a {@link IJobRunner}, and the schedule and associated data are set by
 * scheduling a specific job for that runner. Multiple jobs can be {@link #scheduleJob(JobId,JobConfig) scheduled} for a
 * given job runner.
 * </p>
 */
public interface ISchedulerService {

    /**
     * Registers the job runner for a given job runner key. Registration does not survive application restart, and must
     * be done on each node for clustered applications. A second registration with the same {@code jobRunnerKey} will
     * replace any existing registration.
     * <p>
     * A job that is scheduled to run but has no registered job runner is reported as {@link RunOutcome#UNAVAILABLE
     * unavailable}.
     * </p>
     *
     * @param jobRunnerKey
     *                     Globally unique job runner key.
     * @param jobRunner
     *                     the concrete object capable of running instances of this job
     */
    void registerJobRunner(@Nonnull JobRunnerKey jobRunnerKey, @Nonnull IJobRunner jobRunner);

    /**
     * Unregisters the specified job runner. Plugins should unregister their job runners as part of being disabled.
     * <p>
     * Jobs that fire with no registered job runner will fail to start.
     * </p>
     *
     * @param jobRunnerKey
     *                     Globally unique job runner key.
     */
    void unregisterJobRunner(@Nonnull JobRunnerKey jobRunnerKey);

    /**
     * Returns all of the job runner keys that currently have registered job runners, regardless of whether or not any
     * jobs have actually been {@link #scheduleJob(JobId, JobConfig) scheduled} for them. The job runner keys are not
     * guaranteed to be returned in any particular order.
     *
     * @return an immutable set containing all of the registered job runner keys
     * @see #getJobRunnerKeysForAllScheduledJobs()
     */
    @Nonnull
    Set<JobRunnerKey> getRegisteredJobRunnerKeys();

    /**
     * Returns all of the job runner keys that have been used to schedule jobs, regardless of whether or not
     * {@link IJobRunner}s are currently registered for them. The job runner keys are not guaranteed to be returned in
     * any particular order.
     *
     * @return an immutable set containing all of the job runner keys with scheduled jobs
     * @see #getRegisteredJobRunnerKeys()
     */
    @Nonnull
    Set<JobRunnerKey> getJobRunnerKeysForAllScheduledJobs();

    /**
     * Schedules a job with the given job ID.
     * <p>
     * If a job already exists with the given ID, then it will be replaced with the new run config. If the schedule is
     * eligible to run immediately and multiple nodes take this action at close to the same time, then the job might run
     * more than once as the instances replace one another.
     * </p>
     * <p>
     * In most cases, this will be harmless, but it can be avoided by making sure the job will not be eligible to run
     * until some time in the future. For example, when using an interval schedule, the caller can first check whether
     * or not the job already exists, and if it does not then specify an initial start date for the schedule, as in:
     * </p>
     * <code><pre>
     *     Schedule.forInterval(120000L, new Date(System.currentTimeMillis() + 15000L))
     * </pre></code>
     * <p>
     * Since the schedule will not be eligible to run until 15 seconds after the current time, any race conditions
     * between two nodes starting up at once and trying to schedule the same job should resolve before the job actually
     * fires. For cron expressions, this is a little bit more difficult, but you can set the seconds field to an
     * explicit value to accomplish the same thing. For example:
     * </p>
     * <code><pre>
     *     final Calendar calendar = new GregorianCalendar();
     *     calendar.add(15, Calendar.SECOND);
     *     final Schedule schedule = Schedule.forCronExpression(
     *             calendar.get(Calendar.SECOND) + " 0 2 * * ?");  // at or just after 2 A.M.
     *     scheduleJob(...
     * </pre></code>
     *
     * @param jobId
     *                  the Job ID
     * @param jobConfig
     *                  the configuration details for the job instance including schedule, run mode, run parameters,
     *                  etc.
     * @throws SchedulerServiceException
     *                                   if the job cannot be scheduled because there is a problem with either the
     *                                   provided configuration or within the scheduler implementation itself
     */
    void scheduleJob(JobId jobId, JobConfig jobConfig) throws SchedulerServiceException;

    /**
     * Schedules a "dynamic" job by generating a new unique job ID.
     * <p/>
     * This method should normally only be used when creating multiple jobs for a given job runner key that need to run
     * independently &mdash; most likely because these are created in response to user input.
     *
     * @param jobConfig
     *                  the configuration details for the job instance including schedule, run mode, run parameters,
     *                  etc.
     * @return the generated unique Job ID
     * @throws SchedulerServiceException
     *                                   if the job cannot be scheduled because there is a problem with either the
     *                                   provided configuration or within the scheduler implementation itself
     */
    @Nonnull
    JobId scheduleJobWithGeneratedId(JobConfig jobConfig) throws SchedulerServiceException;

    /**
     * Unschedules a previously scheduled job ID.
     * <p>
     * If no such job exists, then the request is ignored.
     * </p>
     *
     * @param jobId
     *              the Job ID to be unregistered
     */
    void unscheduleJob(JobId jobId);

    /**
     * Returns the next time that a job with the given schedule would be expected to run.
     * <p>
     * Caveats:
     * </p>
     * <ul>
     * <li>Interval schedules taken from {@link IJobDetails} are not aware of the job that they came from or whether or
     * not that job has previously run. They are calculated on the basis of the current time, not the original job's
     * history.</li>
     * <li>Schedules based on {@link Schedule#forCronExpression(String) cron expressions} are implicitly
     * {@link ICronExpressionValidator#validate(String) validated} by this request.</li>
     * <li>The initial run time reported is not a strong guarantee. The actual initial run time that is calculated by
     * the schedule may depend on the exact time that the schedule is created. The return value should be treated as an
     * estimate, only.</li>
     * </ul>
     *
     * @param schedule
     *                 the schedule to evaluate
     * @return the estimated time that the schedule would next run, or {@code null} if it would never run
     * @throws SchedulerServiceException
     *                                   if {@code schedule} is invalid.
     */
    @Nullable
    Date calculateNextRunTime(Schedule schedule) throws SchedulerServiceException;

    /**
     * Retrieves the details for the specified job ID.
     *
     * @param jobId
     *              the Job ID for which to retrieve the details
     * @return the job details, or {@code null} if no such job is defined
     */
    @CheckForNull
    IJobDetails getJobDetails(@Nonnull JobId jobId);

    /**
     * Retrieves the job details for all jobs with the given job runner key.
     *
     * @param jobRunnerKey
     *                     the job runner key to look up
     * @return the jobs that are registered with the given job runner key, or an empty collection if there are no jobs
     *         that use the given key; never {@code null}
     */
    @Nonnull
    List<IJobDetails> getJobsByJobRunnerKey(@Nonnull JobRunnerKey jobRunnerKey);
}
