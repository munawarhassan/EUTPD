package com.pmi.tpd.scheduler.spi;

import javax.annotation.CheckForNull;

import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.status.IRunDetails;

/**
 * Service provided by the host application to persist {@code RunDetails}
 * objects.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRunDetailsDao {

  /**
   * Returns the result of the most recent attempt to run this job.
   *
   * @param jobId
   *              the job ID of interest
   * @return the result information for the most recent run attempt, or
   *         {@code null} if there is no recorded run
   *         history for this job
   */
  @CheckForNull
  IRunDetails getLastRunForJob(JobId jobId);

  /**
   * Returns the result of the most recent successful run of this job.
   *
   * @param jobId
   *              the job ID of interest
   * @return the result information for the most recent run attempt, or
   *         {@code null} if there is no successful result
   *         recorded for this job
   */
  @CheckForNull
  IRunDetails getLastSuccessfulRunForJob(JobId jobId);

  /**
   * Records the result of an attempt to run the specified job.
   *
   * @param jobId
   *                   the job ID of the job that the scheduler attempted to run
   * @param runDetails
   *                   the result of the run
   */
  void addRunDetails(JobId jobId, IRunDetails runDetails);
}
