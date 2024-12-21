package com.pmi.tpd.api.scheduler.config;

import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.util.Safe.copy;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;

/**
 * Configuration options available when scheduling a job to be run. This is
 * similar to
 * {@link com.pmi.tpd.core.scheduler.status.IJobDetails}, but provides only the
 * information that is relevant to
 * configuring the job or for the job to reference when it is actually running.
 *
 * @see com.pmi.tpd.core.scheduler.status.IJobDetails
 * @author Christophe Friederich
 * @since 1.3
 */
@Immutable
public final class JobConfig {

  /** */
  static final Map<String, Serializable> NO_PARAMETERS = ImmutableMap.of();

  // CHECKSTYLE:OFF
  /**
   * Creates a new job configuration for the specified job runner key.
   * <p>
   * By default, the job configuration will assume:
   * </p>
   * <ul>
   * <li><code>{@link #withRunMode(RunMode) withRunMode}({@link RunMode#RUN_ONCE_PER_CLUSTER})</code></li>
   * <li>
   * <code>{@link #withSchedule(Schedule) withSchedule}({@link Schedule#runOnce(java.util.Date) Schedule.runOnce}(new Date()))</code>
   * </li>
   * <li><code>{@link #withParameters(Map) withParameters}(null)</code></li>
   * </ul>
   * <p>
   * Any of which may be overridden by calling the appropriate method. Note that
   * chaining the methods is recommended,
   * as these methods return an altered copy rather than modifiying the original
   * {@code JobConfig} in place. For
   * example, use:
   * </p>
   * <code><pre>
   *     JobConfig config = JobConfig.forJobRunnerKey("myJobToDoImportantThings")
   *              .withSchedule(Schedule.forInterval(Date, long))
   *              .withRunMode(RunMode.RUN_LOCALLY);
   * </pre></code>
   *
   * @param jobRunnerKey
   *                     the unique identifier used to
   *                     {@link com.pmi.tpd.core.scheduler.ISchedulerService#registerJobRunner(JobRunnerKey, com.pmi.tpd.core.scheduler.JobRunner)
   *                     register} the
   *                     {@link com.pmi.tpd.core.scheduler.JobRunner}
   * @return a job configuration for the specified job runner key that will use
   *         the default settings
   */
  // CHECKSTYLE:ON
  public static JobConfig forJobRunnerKey(final JobRunnerKey jobRunnerKey) {
    checkNotNull(jobRunnerKey, "jobRunnerKey");
    return new JobConfig(jobRunnerKey, RUN_ONCE_PER_CLUSTER, Schedule.runOnce(null), NO_PARAMETERS);
  }

  /** */
  private final JobRunnerKey jobRunnerKey;

  /** */
  private final RunMode runMode;

  /** */
  private final Schedule schedule;

  /** */
  private final Map<String, Serializable> parameters;

  private JobConfig(final JobRunnerKey jobRunnerKey, @Nullable final RunMode runMode,
      @Nullable final Schedule schedule, final Map<String, Serializable> parameters) {
    this.jobRunnerKey = jobRunnerKey;
    this.runMode = runMode != null ? runMode : RUN_ONCE_PER_CLUSTER;
    this.schedule = schedule != null ? schedule : Schedule.runOnce(null);

    // handled by withParameters and would be redundant elsewhere
    // noinspection AssignmentToCollectionOrArrayFieldFromParameter
    this.parameters = parameters;
  }

  /**
   * @return
   */
  @Nonnull
  public JobRunnerKey getJobRunnerKey() {
    return jobRunnerKey;
  }

  /**
   * @return
   */
  @Nonnull
  public RunMode getRunMode() {
    return runMode;
  }

  /**
   * @return
   */
  @Nonnull
  public Schedule getSchedule() {
    return schedule;
  }

  /**
   * @return
   */
  @Nonnull
  public Map<String, Serializable> getParameters() {
    return parameters;
  }

  /**
   * Returns a copy of this job config that will use the specified run mode
   * instead of what it currently uses.
   *
   * @param runMode
   *                the new run mode; may be {@code null}, in which case the
   *                default {@link RunMode#RUN_ONCE_PER_CLUSTER}
   *                is used
   * @return the new job configuration; the original is left unchanged
   */
  public JobConfig withRunMode(final RunMode runMode) {
    return new JobConfig(jobRunnerKey, runMode, schedule, parameters);
  }

  /**
   * Returns a copy of this job config that will use the specified schedule
   * instead of what it currently uses.
   *
   * @param schedule
   *                 the new schedule; may be {@code null}, in which case the
   *                 default
   *                 {@link Schedule#runOnce(java.util.Date) runOnce(new Date())}
   *                 is used
   * @return the new job configuration; the original is left unchanged
   */
  public JobConfig withSchedule(final Schedule schedule) {
    return new JobConfig(jobRunnerKey, runMode, schedule, parameters);
  }

  /**
   * Returns a copy of this job config that will use the specified parameters
   * instead of what it currently uses.
   * <p>
   * <strong>WARNING</strong>: The parameters map must be serializable, so all of
   * its contents must be as well. Using
   * objects that are not serializable, even as data members of objects that are
   * themselves serializable, will usually
   * fail. Developers are encouraged to limit the information stored here to a few
   * simple keys for accessing the
   * runtime data that is needed instead of storing large objects, injectable
   * components, etc. in the
   * {@code parameters} map.
   * </p>
   * <p>
   * <strong>WARNING</strong>: The scheduler service assumes that the objects
   * within the {@code parameters} map are
   * immutable. Modifying objects after they have been added to the parameters map
   * may have unpredictable results.
   * </p>
   *
   * @param parameters
   *                   the new parameters; may be {@code null}, in which case the
   *                   {@link com.pmi.tpd.core.scheduler.JobRunner} is provided
   *                   with an empty map at run time. The map should
   *                   contain only immutable, serializable data
   * @return the new job configuration; the original is left unchanged
   */
  public JobConfig withParameters(@Nullable final Map<String, Serializable> parameters) {
    return new JobConfig(jobRunnerKey, runMode, schedule, copy(parameters));
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final JobConfig other = (JobConfig) o;
    return jobRunnerKey.equals(other.jobRunnerKey) && runMode == other.runMode && schedule.equals(other.schedule)
        && parameters.equals(other.parameters);
  }

  @Override
  public int hashCode() {
    int result = jobRunnerKey.hashCode();
    result = 31 * result + runMode.hashCode();
    result = 31 * result + schedule.hashCode();
    result = 31 * result + parameters.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "JobConfig[jobRunnerKey=" + jobRunnerKey + ",runMode=" + runMode + ",schedule=" + schedule
        + ",parameters=" + parameters + ']';
  }
}
