package com.pmi.tpd.scheduler.spi;

import java.util.TimeZone;

import javax.annotation.Nullable;

/**
 * Allows the host application to supply configuration parameters to the
 * scheduler services.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ISchedulerServiceConfiguration {

  /**
   * Returns the default {@code TimeZone} to use when scheduling a job to run
   * according to a
   * {@link Schedule#forCronExpression(String) cron expression} when no specific
   * time zone is provided.
   * <p>
   * The value is checked when the job is scheduled and treated as if it were
   * given explicitly. As a result, if the
   * host application is configured to use a new default time zone, then this will
   * be used for jobs that are scheduled
   * after the change, but existing jobs will continue to use the original
   * setting.
   * </p>
   *
   * @return the default time zone to use for cron expression schedules; if the
   *         configuration object returns
   *         {@code null}, then {@link TimeZone#getDefault()} is assumed
   */
  @Nullable
  TimeZone getDefaultTimeZone();
}
