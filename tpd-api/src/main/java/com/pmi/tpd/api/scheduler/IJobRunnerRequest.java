package com.pmi.tpd.api.scheduler;

import java.util.Date;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;

/**
 * Represents a request to run a job, providing information such as the job's
 * configuration and intended start time.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IJobRunnerRequest {

  /**
   * Returns the time at which the job was started. When this job completes, the
   * {@link com.pmi.tpd.api.scheduler.status.IRunDetails} that stores the result
   * will use this exact time for
   * {@link com.pmi.tpd.api.scheduler.status.IRunDetails.status.RunDetails#getStartTime()}.
   *
   * @return the time at which the job was started.
   */
  @Nonnull
  Date getStartTime();

  /**
   * Returns the job ID that was used to schedule this job.
   *
   * @return the job ID that was used to schedule this job.
   */
  @Nonnull
  JobId getJobId();

  /**
   * Returns the job's configuration, such as its schedule and parameters.
   *
   * @return the job's configuration
   */
  @Nonnull
  JobConfig getJobConfig();

  /**
   * Returns {@code true} if the job runner should terminate its activities as
   * gracefully as possible and exit;
   * {@code false} to continue running normally.
   * <p>
   * Job cancellation is entirely cooperative. If a job is likely to take longer
   * than a few seconds to complete its
   * work, then it should periodically check this value and react to it. Normally,
   * cancellation is requested because
   * the application is trying to shut down, and continuing to run after this flag
   * has been set increases the chance
   * that the system administrator will grow impatient and forcibly kill the
   * application.
   * </p>
   *
   * @return {@code true} if cancellation is requested; {@code false} otherwise
   */
  boolean isCancellationRequested();
}
