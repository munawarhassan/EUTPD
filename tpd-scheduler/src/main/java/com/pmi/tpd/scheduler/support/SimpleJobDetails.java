package com.pmi.tpd.scheduler.support;

import static com.pmi.tpd.api.scheduler.util.Safe.copy;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.scheduler.status.AbstractJobDetails;

/**
 * A simple, concrete implementation of
 * {@link com.pmi.tpd.api.scheduler.status.IJobDetails} that has the parameter
 * map
 * present.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SimpleJobDetails extends AbstractJobDetails {

  /** */
  private final Map<String, Serializable> parameters;

  /**
   * @param jobId
   * @param jobRunnerKey
   * @param runMode
   * @param schedule
   * @param nextRunTime
   * @param rawParameters
   * @param parameters
   */
  public SimpleJobDetails(final JobId jobId, final JobRunnerKey jobRunnerKey, final RunMode runMode,
      final Schedule schedule, @Nullable final Date nextRunTime, final byte[] rawParameters,
      @Nullable final Map<String, Serializable> parameters) {
    super(jobId, jobRunnerKey, runMode, schedule, nextRunTime, rawParameters);
    this.parameters = copy(parameters);
  }

  @Override
  @Nonnull
  public Map<String, Serializable> getParameters() {
    return parameters;
  }

  @Override
  public boolean isRunnable() {
    return true;
  }

  @Override
  protected void appendToStringDetails(final StringBuilder sb) {
    sb.append(",parameters=").append(parameters);
  }
}
