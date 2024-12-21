package com.pmi.tpd.api.context;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * A clock that constructs a date-time instance set to the current system
 * millisecond time using ISOChronology in the
 * default time zone.
 */
public class SystemClock implements IClock {

  public static final SystemClock INSTANCE = new SystemClock();

  @Override
  public long nanoTime() {
    return System.nanoTime();
  }

  @Override
  @Nonnull
  public DateTime now() {
    return new DateTime();
  }
}
