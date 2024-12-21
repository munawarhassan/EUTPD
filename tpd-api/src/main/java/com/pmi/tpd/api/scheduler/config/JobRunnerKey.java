package com.pmi.tpd.api.scheduler.config;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A wrapper to distinguish job runner keys from simple strings and to make it
 * easier to avoid confusing them with Job
 * IDs.
 *
 * @since 1.3
 */
@Immutable
public final class JobRunnerKey implements Serializable, Comparable<JobRunnerKey> {

  /** */
  private static final long serialVersionUID = 1L;

  // CHECKSTYLE:OFF
  /**
   * Wraps the provided string as a {@code JobRunnerKey}.
   * <p>
   * Although it is not necessary for correctness, it will usually make sense to
   * create a single instance of the
   * {@code JobRunnerKey} and reuse it as a constant, as in:
   * </p>
   * <code><pre>
   *     private static final JobRunnerKey <strong>POP3_SERVICE</strong> = JobRunnerKey.of("com.example.plugin.Pop3Service");
   *
   *     // ...
   *
   *     private void registerJobRunner()
   *     {
   *         schedulerService.registerJobRunner(<strong>POP3_SERVICE</strong>, new Pop3JobRunner());
   *     }
   *
   *     private String scheduleJobWithGeneratedId(String cronExpression)
   *     {
   *         JobConfig jobConfig = JobConfig.forJobRunnerKey(<strong>POP3_SERVICE</strong>)
   *                  .withSchedule(Schedule.forCronExpression(cronExpression));
   *         return schedulerService.scheduleJobWithGeneratedId(jobConfig);
   *     }
   * </pre></code>
   *
   * @param key
   *            the job runner key, as a string
   * @return the wrapped job runner key
   */
  // CHECKSTYLE:ON
  public static JobRunnerKey of(final String key) {
    return new JobRunnerKey(key);
  }

  /** */
  private final String key;

  private JobRunnerKey(@Nonnull final String key) {
    this.key = checkNotNull(key, "key");
  }

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    return o != null && o.getClass() == getClass() && ((JobRunnerKey) o).key.equals(key);
  }

  @Override
  public int compareTo(final JobRunnerKey o) {
    return key.compareTo(o.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public String toString() {
    return key;
  }
}
