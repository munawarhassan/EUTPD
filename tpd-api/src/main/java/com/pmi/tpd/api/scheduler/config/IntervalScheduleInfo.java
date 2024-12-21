package com.pmi.tpd.api.scheduler.config;

import static com.google.common.base.Objects.equal;
import static com.pmi.tpd.api.scheduler.util.Safe.copy;

import java.util.Date;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.pmi.tpd.api.util.Assert;

/**
 * The description of an {@link Schedule#forInterval(long, Date)} interval
 * schedule.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Immutable
public final class IntervalScheduleInfo {

  /** */
  private final Date firstRunTime;

  /** */
  private final long intervalInMillis;

  IntervalScheduleInfo(@Nullable final Date firstRunTime, final long intervalInMillis) {
    this.firstRunTime = copy(firstRunTime);
    this.intervalInMillis = intervalInMillis;

    Assert.isTrue(intervalInMillis >= 0L, "intervalInMillis must not be negative");
  }

  /**
   * @return
   */
  @Nullable
  public Date getFirstRunTime() {
    return copy(firstRunTime);
  }

  /**
   * @return
   */
  public long getIntervalInMillis() {
    return intervalInMillis;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final IntervalScheduleInfo other = (IntervalScheduleInfo) o;
    return intervalInMillis == other.intervalInMillis && equal(firstRunTime, other.firstRunTime);
  }

  @Override
  public int hashCode() {
    int result = firstRunTime != null ? firstRunTime.hashCode() : 0;
    result = 31 * result + (int) (intervalInMillis ^ intervalInMillis >>> 32);
    return result;
  }

  @Override
  public String toString() {
    return "IntervalScheduleInfo[firstRunTime=" + firstRunTime + ",intervalInMillis=" + intervalInMillis + ']';
  }
}
