package com.pmi.tpd.scheduler.status;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.JobRunnerNotRegisteredException;
import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;

/**
 * Concrete implementation of {@code JobDetails} that always throws an exception
 * when the parameters are accessed. This
 * can be used when the {@code JobRunner} is unavailable or an exception occurs
 * while trying to deserialize the
 * parameters.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class UnusableJobDetails extends AbstractJobDetails {

  /** */
  private final Throwable cause;

  /**
   * @param cause
   *              the reason the parameters are unavailable. If left {@code null},
   *              then it is assumed that the
   *              {@code JobRunner} is not available.
   */
  public UnusableJobDetails(final JobId jobId, final JobRunnerKey jobRunnerKey, final RunMode runMode,
      final Schedule schedule, @Nullable final Date nextRunTime, final byte[] parameters,
      @Nullable final Throwable cause) {
    super(jobId, jobRunnerKey, runMode, schedule, nextRunTime, parameters);
    this.cause = cause != null ? cause : new JobRunnerNotRegisteredException(jobRunnerKey);
  }

  /**
   * The parameters are not available because the map could not be reconstructed.
   *
   * @return never returns normally
   * @throws SchedulerRuntimeException
   *                                   Unconditionally. The exception's
   *                                   {@code cause} will be either a
   *                                   {@link JobRunnerNotRegisteredException} or
   *                                   the error that occurred during
   *                                   deserialization.
   */
  @Override
  @Nonnull
  public Map<String, Serializable> getParameters() {
    throw new SchedulerRuntimeException("The parameters cannot be accessed: " + cause, cause);
  }

  @Override
  public boolean isRunnable() {
    return false;
  }

  @Override
  protected void appendToStringDetails(final StringBuilder sb) {
    sb.append(",cause=").append(cause);
  }
}
