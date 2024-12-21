package com.pmi.tpd.cluster;

import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.pmi.tpd.api.context.ITimeZoneHelper;

/**
 * A {@link IClusterJoinRequirement} which requires that the time zone on both
 * servers match.
 * <p>
 * This is so that users always get a consistent time zone date/times returned
 * from the cluster. It also ensures that
 * cron jobs are scheduled consistently within the scheduler.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class TimeZoneClusterJoinRequirement implements IClusterJoinRequirement<TimeZone> {

  /** */
  private final ITimeZoneHelper timeZoneHelper;

  /**
   * @param timeZoneHelper
   */
  @Inject
  public TimeZoneClusterJoinRequirement(final ITimeZoneHelper timeZoneHelper) {
    this.timeZoneHelper = timeZoneHelper;
  }

  @Nonnull
  @Override
  public String getName() {
    return "timeZone";
  }

  @Override
  public TimeZone getValue() {
    return timeZoneHelper.getTimeZone();
  }
}
