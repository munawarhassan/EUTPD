package com.pmi.tpd.api.scheduler.config;

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;
import static java.util.Arrays.asList;

import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Predicates;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.status.RunOutcome;

/**
 * Represents a schedule used to run a job at particular times. Instances of
 * this class are not created directly. Use
 * one of the factory methods &mdash; such as {@link #forInterval(long, Date)}
 * or
 * {@link #forCronExpression(String,TimeZone)} &mdash; to construct them. To
 * recover the information, use
 * {@link #getType()} to identify the type of schedule information and call the
 * appropriate getter (
 * {@link #getIntervalScheduleInfo()} for {@link Type#INTERVAL};
 * {@link #getCronScheduleInfo()} for
 * {@link Type#CRON_EXPRESSION}).
 */
@Immutable
public final class Schedule {

  /**
   * Creates a new schedule for the given cron expression. The cron expression is
   * not verified immediately, but
   * invalid expressions will cause
   * {@link ISchedulerService#scheduleJobWithGeneratedId(JobConfig)} to fail. The
   * system's {@link TimeZone#getDefault() default time zone} is assumed.
   *
   * @param cronExpression
   *                       the cron expression to use for the schedule
   * @return a schedule for running jobs when the given cron expression is
   *         satisfied
   */
  public static Schedule forCronExpression(final String cronExpression) {
    return forCronExpression(cronExpression, null);
  }

  /**
   * Creates a new schedule for the given cron expression. The cron expression is
   * not verified immediately, but
   * invalid expressions will cause
   * {@link ISchedulerService#scheduleJobWithGeneratedId(JobConfig)} to fail.
   *
   * @param cronExpression
   *                       the cron expression to use for the schedule
   * @param timeZone
   *                       the time zone within which to apply the rules of the
   *                       cron expression; may be {@code null} to use a
   *                       default time zone that is appropriate for the
   *                       application
   * @return a schedule for running jobs when the given cron expression is
   *         satisfied
   */
  public static Schedule forCronExpression(final String cronExpression, @Nullable final TimeZone timeZone) {
    return new Schedule(Type.CRON_EXPRESSION, null, new CronScheduleInfo(cronExpression, timeZone));
  }

  /**
   * Creates a new schedule that will run once at the specified time. Jobs that
   * are scheduled to run once are not
   * guaranteed to remain in the system after they are attempted, regardless of
   * the {@link RunOutcome}. Jobs scheduled
   * as {@code runOnce} will be purged automatically after they have run; that is,
   * at some point after they have run
   * (possibly immediately), {@link ISchedulerService#getJobDetails(JobId)} will
   * no longer return the job's
   * information.
   *
   * @param runTime
   *                when the job should run; may be {@code null} to indicate that
   *                the job should run as soon as possible
   * @return a schedule for running once at the given time
   */
  public static Schedule runOnce(@Nullable final Date runTime) {
    return forInterval(0L, runTime);
  }

  /**
   * Creates a new schedule that will run periodically with the given interval.
   * <p>
   * <strong>WARNING</strong>: Implementations are not required to honour the time
   * interval at millisecond precision.
   * The actual resolution that is permitted is implementation-defined.
   * Implementations should round the requested
   * interval up and persist the rounded value so that the interval reported by
   * later calls to
   * {@link Schedule#getIntervalScheduleInfo()} will be consistent with the
   * behaviour that the scheduler actually
   * provides.
   * </p>
   *
   * @param intervalInMillis
   *                         the minimum time interval (in milliseconds) between
   *                         runs. If the value {@code 0L} is specified, then
   *                         the job will not repeat (this is equivalent to using
   *                         {@link #runOnce(Date) runOnce(firstRunTime)}).
   *                         Negative values are not permitted.
   * @param firstRunTime
   *                         when the job should run for the first time; may be
   *                         {@code null} to indicate that the job should run as
   *                         soon as possible
   * @return a schedule for running once at the given time
   */
  public static Schedule forInterval(final long intervalInMillis, @Nullable final Date firstRunTime) {
    return new Schedule(Type.INTERVAL, new IntervalScheduleInfo(firstRunTime, intervalInMillis), null);
  }

  /** */
  private final Type type;

  /** */
  private final IntervalScheduleInfo intervalScheduleInfo;

  /** */
  private final CronScheduleInfo cronScheduleInfo;

  private Schedule(@Nonnull final Type type, @Nullable final IntervalScheduleInfo intervalScheduleInfo,
      @Nullable final CronScheduleInfo cronScheduleInfo) {
    this.type = checkNotNull(type, "type");
    this.intervalScheduleInfo = intervalScheduleInfo;
    this.cronScheduleInfo = cronScheduleInfo;

    isTrue(countNulls(intervalScheduleInfo, cronScheduleInfo) == 1,
        "Exactly one of the schedule formats must be non-null");
  }

  private static int countNulls(final Object... schedules) {
    return size(filter(asList(schedules), Predicates.notNull()));
  }

  /**
   * Returns a representation of the simple settings that were used to create this
   * schedule.
   *
   * @return the interval schedule, or {@code null} if that is not this schedule's
   *         {@link #getType() type}.
   */
  public IntervalScheduleInfo getIntervalScheduleInfo() {
    return intervalScheduleInfo;
  }

  /**
   * Returns a representation of the cron settings that were used to create this
   * schedule.
   *
   * @return the cron schedule, or {@code null} if that is not this schedule's
   *         {@link #getType() type}.
   */
  public CronScheduleInfo getCronScheduleInfo() {
    return cronScheduleInfo;
  }

  /**
   * Returns the {@link Type} of this schedule.
   *
   * @return the {@link Type} of this schedule.
   */
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Schedule other = (Schedule) o;
    return type == other.type && equal(intervalScheduleInfo, other.intervalScheduleInfo)
        && equal(cronScheduleInfo, other.cronScheduleInfo);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (intervalScheduleInfo != null ? intervalScheduleInfo.hashCode() : 0);
    result = 31 * result + (cronScheduleInfo != null ? cronScheduleInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128).append("Schedule[type=").append(type);
    switch (type) {
      case CRON_EXPRESSION:
        sb.append(",cronScheduleInfo=").append(cronScheduleInfo);
        break;
      case INTERVAL:
        sb.append(",intervalScheduleInfo=").append(intervalScheduleInfo);
        break;
    }
    return sb.append(']').toString();
  }

  public static enum Type {
    /**
     * A schedule that uses a cron expression to control when the job runs.
     *
     * @see CronScheduleInfo
     * @see #getCronScheduleInfo()
     */
    CRON_EXPRESSION,

    /**
     * A schedule that uses a interval to control when the job runs.
     *
     * @see IntervalScheduleInfo
     * @see #getIntervalScheduleInfo()
     */
    INTERVAL
  }
}
