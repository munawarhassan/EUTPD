package com.pmi.tpd.api.scheduler.status;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;

/**
 * All the static details for a given scheduled job. This is similar to a
 * {@link com.pmi.tpd.api.scheduler.config.JobConfig}, but also includes
 * information about the job's current state, such
 * as whether it is currently {@link #isRunnable() runnable}.
 *
 * @see com.pmi.tpd.api.scheduler.config.JobConfig
 * @see IRunDetails
 */
@SuppressWarnings("checkstyle:linelength")
@Immutable
public interface IJobDetails {

  /**
   * Returns the job ID that was used to
   * {@link com.pmi.tpd.core.scheduler.ISchedulerService#scheduleJob(JobId,com.pmi.tpd.api.scheduler.config.JobConfig)
   * schedule} this job, or the one that was generated for it if the job was
   * scheduled
   * {@link com.pmi.tpd.core.scheduler.ISchedulerService#scheduleJobWithGeneratedId(JobConfig)
   * without specifying
   * one}.
   *
   * @return the job ID
   */
  @Nonnull
  JobId getJobId();

  /**
   * Returns the
   * {@link com.pmi.tpd.api.scheduler.config.JobConfig#getJobRunnerKey() job
   * runner key} that was
   * specified when this job was
   * {@link com.pmi.tpd.core.scheduler.ISchedulerService#scheduleJob(JobId,com.pmi.tpd.api.scheduler.config.JobConfig)
   * scheduled}.
   *
   * @return the job runner key
   */
  @Nonnull
  JobRunnerKey getJobRunnerKey();

  /**
   * Returns the {@link com.pmi.tpd.api.scheduler.config.JobConfig#getRunMode()
   * configured run mode} that was
   * specified when this job was
   * {@link com.pmi.tpd.core.scheduler.ISchedulerService#scheduleJob(JobId,com.pmi.tpd.api.scheduler.config.JobConfig)
   * scheduled}.
   *
   * @return the run mode
   */
  @Nonnull
  RunMode getRunMode();

  /**
   * Returns the schedule that the Job will run under.
   *
   * @return the schedule that the Job will run under.
   */
  @Nonnull
  Schedule getSchedule();

  /**
   * Returns the next time at which this job will run, if known.
   * <p>
   * <em>OPTIONAL</em> &mdash; Scheduler implementations are not required to
   * provide this information. The return
   * value will be {@code null} if:
   * </p>
   * <ul>
   * <li>The job will never run again because its schedule does not match any time
   * in the future.</li>
   * <li>The next time the job will run is so far in the future that the scheduler
   * service gave up on trying to figure
   * out when the next run would be.</li>
   * <li>The scheduler implementation does not provide this information.</li>
   * </ul>
   *
   * @return the next time at which this job will run, if known; {@code null}
   *         otherwise.
   */
  @CheckForNull
  Date getNextRunTime();

  /**
   * Returns the configured runtime parameters for this job.
   * <p>
   * <strong>WARNING</strong>: If this job was created by a plugin that is not
   * currently active, then it may not
   * actually be possible to access the job's parameters, and this method will
   * throw a
   * {@link SchedulerRuntimeException}. Callers are encouraged to first call the
   * {@link #isRunnable()} method, as a
   * {@code false} return value from that method usually <em>guarantees</em> that
   * this method will fail.
   * </p>
   *
   * @return the configured runtime parameters for this job.
   * @throws SchedulerRuntimeException
   *                                   if the parameters could not be loaded,
   *                                   likely because the job runner is not
   *                                   registered
   */
  @Nonnull
  Map<String, Serializable> getParameters();

  /**
   * Returns {@code true} if this job could be successfully run at this time. This
   * requires it to have a registered
   * {@link com.pmi.tpd.core.scheduler.IJobRunner} whose class loader can
   * successfully reconstruct the job's
   * {@link JobConfig#getParameters() parameter map}.
   *
   * @return {@code true} if this job is currently runnable; {@code false}
   *         otherwise
   */
  boolean isRunnable();
}
