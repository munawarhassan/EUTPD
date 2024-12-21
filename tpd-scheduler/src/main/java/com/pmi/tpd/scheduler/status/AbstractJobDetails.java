package com.pmi.tpd.scheduler.status;

import static com.pmi.tpd.api.scheduler.util.Safe.copy;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;

/**
 * Base implementation for {@link IJobDetails}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractJobDetails implements IJobDetails {

  /** */
  protected final JobId jobId;

  /** */
  protected final JobRunnerKey jobRunnerKey;

  /** */
  protected final RunMode runMode;

  /** */
  protected final Schedule schedule;

  /** */
  private final Date nextRunTime;

  /** */
  private final byte[] rawParameters;

  /**
   * @param jobId
   * @param jobRunnerKey
   * @param runMode
   * @param schedule
   * @param nextRunTime
   * @param rawParameters
   */
  protected AbstractJobDetails(final JobId jobId, final JobRunnerKey jobRunnerKey, final RunMode runMode,
      final Schedule schedule, @Nullable final Date nextRunTime, @Nullable final byte[] rawParameters) {
    this.jobId = checkNotNull(jobId, "jobId");
    this.jobRunnerKey = checkNotNull(jobRunnerKey, "jobRunnerKey");
    this.runMode = checkNotNull(runMode, "runMode");
    this.schedule = checkNotNull(schedule, "schedule");
    this.nextRunTime = copy(nextRunTime);
    this.rawParameters = copy(rawParameters);
  }

  @Override
  @Nonnull
  public final JobId getJobId() {
    return jobId;
  }

  @Override
  @Nonnull
  public final JobRunnerKey getJobRunnerKey() {
    return jobRunnerKey;
  }

  @Override
  @Nonnull
  public final RunMode getRunMode() {
    return runMode;
  }

  @Override
  @Nonnull
  public Schedule getSchedule() {
    return schedule;
  }

  @Override
  @Nullable
  public Date getNextRunTime() {
    return copy(nextRunTime);
  }

  /**
   * Returns the raw bytes from the job's parameters.
   * <p>
   * This is not part of the public API. It is intended for the persistence layer
   * to use for accessing the raw
   * parameters and/or a troubleshooting tool for a management interface to use.
   * For example, it might try to
   * deserialize the map with the application's class loader or read the raw
   * serialization data to show the
   * administrator whatever information can be pulled out of it.
   * </p>
   *
   * @return a copy of the raw bytes that encode the job's parameters, or
   *         {@code null} if the parameter map was empty.
   */
  @Nullable
  public final byte[] getRawParameters() {
    return copy(rawParameters);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder(128).append(getClass().getSimpleName())
        .append("[jobId=")
        .append(jobId)
        .append(",jobRunnerKey=")
        .append(jobRunnerKey)
        .append(",runMode=")
        .append(runMode)
        .append(",schedule=")
        .append(schedule)
        .append(",nextRunTime=")
        .append(nextRunTime)
        .append(",rawParameters=(");
    if (rawParameters == null) {
      sb.append("null)");
    } else {
      sb.append(rawParameters.length).append(" bytes)");
    }
    appendToStringDetails(sb);
    return sb.append(']').toString();
  }

  protected abstract void appendToStringDetails(final StringBuilder sb);
}
