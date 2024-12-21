package com.pmi.tpd.scheduler.status;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.scheduler.AbstractSchedulerService;

/**
 * Converts a scheduler implementation's internal representation of a job into a
 * {@code JobDetails}. This class produces
 * {@code LazyJobDetails} instances when the {@code JobRunner} is registered as
 * opposed to deserializing the parameters
 * immediately.
 *
 * @param <T>
 *            The type that the scheduler implementation uses as its internal
 *            representation of a job.
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractJobDetailsFactory<T> {

  /** */
  private final AbstractSchedulerService schedulerService;

  /**
   * @param schedulerService
   */
  protected AbstractJobDetailsFactory(@Nonnull final AbstractSchedulerService schedulerService) {
    this.schedulerService = checkNotNull(schedulerService, "schedulerService");
  }

  /**
   * Transforms the scheduler's internal representation of a job into a
   * {@link IJobDetails}. This will attempt to
   * reconstruct the job's parameters using the {@code ClassLoader} of the job's
   * {@link com.pmi.tpd.core.scheduler.IJobRunner} and return an
   * {@link UnusableJobDetails} if the job runner is not
   * registered or its class loader cannot deserialize the parameters map.
   *
   * @param jobId
   *                the job's ID
   * @param jobData
   *                the internal representation of the job
   * @param runMode
   *                the expected run mode of the job
   * @return the corresponding job details
   */
  public IJobDetails buildJobDetails(@Nonnull final JobId jobId,
      @Nonnull final T jobData,
      @Nonnull final RunMode runMode) {
    checkNotNull(jobId, "jobId");
    checkNotNull(jobData, "jobData");
    checkNotNull(runMode, "runMode");

    final JobRunnerKey jobRunnerKey = checkNotNull(getJobRunnerKey(jobData), "jobRunnerKey");
    final Schedule schedule = checkNotNull(getSchedule(jobData), "schedule");
    final Date nextRunTime = getNextRunTime(jobData);
    final byte[] parameters = getSerializedParameters(jobData);
    return new LazyJobDetails(schedulerService, jobId, jobRunnerKey, runMode, schedule, nextRunTime, parameters);
  }

  /**
   * Provided by the scheduler implementation to extract the job's
   * {@link JobRunnerKey} from the scheduler's internal
   * representation of the job.
   *
   * @param jobData
   *                the scheduler's internal representation of the job
   * @return the key for the job's target job runner
   */
  @Nonnull
  protected abstract JobRunnerKey getJobRunnerKey(T jobData);

  /**
   * Provided by the scheduler implementation to extract the job's
   * {@link Schedule} from the scheduler's internal
   * representation of the job.
   *
   * @param jobData
   *                the scheduler's internal representation of the job
   * @return the job's corresponding {@link Schedule}
   */
  @Nonnull
  protected abstract Schedule getSchedule(T jobData);

  /**
   * Provided by the scheduler implementation to extract the job's next scheduled
   * run time from the scheduler's
   * internal representation of the job.
   *
   * @param jobData
   *                the scheduler's internal representation of the job
   * @return the job's next expected run time; may be {@code null} if the job will
   *         not be run again or if the
   *         scheduler does not provide information about future run times
   */
  @Nullable
  protected abstract Date getNextRunTime(T jobData);

  /**
   * Provided by the scheduler implementation to extract the job's parameters map
   * (in serialized form) from the
   * scheduler's internal representation of the job.
   *
   * @param jobData
   *                the scheduler's internal representation of the job
   * @return a byte array containing the parameters map in serialized form; may be
   *         {@code null}, in which case an
   *         empty map will be substituted
   */
  @Nullable
  protected abstract byte[] getSerializedParameters(T jobData);
}
