package com.pmi.tpd.api.scheduler.config;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;

/**
 * The description of a {@link Schedule#forCronExpression(String,TimeZone) cron
 * expression schedule}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Immutable
public final class CronScheduleInfo {

  /** */
  private final String cronExpression;

  /** */
  private final TimeZone timeZone;

  CronScheduleInfo(@Nonnull final String cronExpression, @Nullable final TimeZone timeZone) {
    this.cronExpression = checkNotNull(cronExpression, "cronExpression");
    this.timeZone = timeZone;
  }

  @Nonnull
  public String getCronExpression() {
    return cronExpression;
  }

  @Nullable
  public TimeZone getTimeZone() {
    return timeZone;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CronScheduleInfo other = (CronScheduleInfo) o;
    return cronExpression.equals(other.cronExpression) && Objects.equal(timeZone, other.timeZone);
  }

  @Override
  public int hashCode() {
    int result = cronExpression.hashCode();
    result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final String timeZoneId = timeZone != null ? timeZone.getID() : null;
    return "CronScheduleInfo[cronExpression='" + cronExpression + "',timeZone=" + timeZoneId + ']';
  }
}
