package com.pmi.tpd.api.scheduler.status;

import java.util.Date;

import javax.annotation.Nonnull;

/**
 * A report of the result from a specific attempt to run a job. The job status
 * may optionally also include a
 * {@link #getMessage() status message} for informational or troubleshooting
 * reasons.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRunDetails {

  /**
   * The maximum length that is permitted for the message string returned in a
   * {@link com.pmi.tpd.core.scheduler.JobRunnerResponse}. Any return value that
   * exceeds this length (
   * {@value} {@code char}s) is silently truncated.
   */
  int MAXIMUM_MESSAGE_LENGTH = 255;

  /**
   * Returns the starting time of this job run.
   *
   * @return the starting time of this job run.
   */
  @Nonnull
  Date getStartTime();

  /**
   * Returns the duration (in milliseconds) that the job took to complete.
   *
   * @return the duration (in milliseconds) that the job took to complete.
   */
  long getDurationInMillis();

  /**
   * Returns the overall result of the job.
   *
   * @return the overall result of the job.
   */
  @Nonnull
  RunOutcome getRunOutcome();

  /**
   * Returns any additional message that the job would like to report about this
   * job run. If the job failed, this will
   * generally include a brief summary of the exception that was thrown. This may
   * be blank for successful statuses,
   * but it will never be {@code null}.
   *
   * @return any additional message that the job would like to report about its
   *         status.
   */
  @Nonnull
  String getMessage();
}
