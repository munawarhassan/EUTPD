package com.pmi.tpd.scheduler.support;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Objects;
import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@ThreadSafe
public final class RunningJobImpl implements IRunningJob {

  /** */
  private final long startTime;

  /** */
  private final JobId jobId;

  /** */
  private final JobConfig jobConfig;

  /** */
  private volatile boolean cancelled;

  /**
   * @param startTime
   * @param jobId
   * @param jobConfig
   */
  public RunningJobImpl(@Nonnull final Date startTime, @Nonnull final JobId jobId,
      @Nonnull final JobConfig jobConfig) {
    this.startTime = checkNotNull(startTime, "startTime").getTime();
    this.jobId = checkNotNull(jobId, "jobId");
    this.jobConfig = checkNotNull(jobConfig, "jobConfig");
  }

  @Nonnull
  @Override
  public Date getStartTime() {
    return new Date(startTime);
  }

  @Nonnull
  @Override
  public JobId getJobId() {
    return jobId;
  }

  @Nonnull
  @Override
  public JobConfig getJobConfig() {
    return jobConfig;
  }

  @Override
  public boolean isCancellationRequested() {
    return cancelled;
  }

  @Override
  public void cancel() {
    cancelled = true;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final RunningJobImpl other = (RunningJobImpl) o;
    return startTime == other.startTime && jobId.equals(other.jobId) && jobConfig.equals(other.jobConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(startTime, jobId, jobConfig);
  }

  @Override
  public String toString() {
    return "RunningJobImpl[startTime=" + startTime + ",jobId=" + jobId + ",jobConfig=" + jobConfig + ",cancelled="
        + cancelled + ']';
  }
}
