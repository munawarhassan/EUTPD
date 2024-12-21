package com.pmi.tpd.api.scheduler.config;

import static com.pmi.tpd.api.scheduler.config.Schedule.forCronExpression;
import static com.pmi.tpd.api.scheduler.config.Schedule.forInterval;
import static com.pmi.tpd.api.scheduler.config.Schedule.runOnce;
import static com.pmi.tpd.api.scheduler.config.Schedule.Type.CRON_EXPRESSION;
import static com.pmi.tpd.api.scheduler.config.Schedule.Type.INTERVAL;
import static java.util.TimeZone.getTimeZone;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;
import java.util.TimeZone;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class ScheduleTest extends TestCase {

  private static final String AT_2AM = "0 0 2 * * *";

  private static final String AT_4AM = "0 0 4 * * *";

  private static final String BAD = "These aren't the cron expressions we are looking for.";

  @Test
  public void testRunOnceWithNull() throws Exception {
    assertIntervalSchedule(runOnce(null), null, 0L);
  }

  @Test
  public void testRunOnceWithDate() throws Exception {
    final Date original = new Date();
    final long originalTime = original.getTime();
    final Schedule sched = runOnce(original);
    original.setTime(42L);

    final IntervalScheduleInfo info = simple(sched);
    Date copy = info.getFirstRunTime();
    assertNotNull(copy);
    assertThat("Modifying the original should not pollute the stored copy", copy.getTime(), is(originalTime));
    copy.setTime(42L);

    copy = info.getFirstRunTime();
    assertNotNull(copy);
    assertThat("Modifying returned value should not pollute the original", copy.getTime(), is(originalTime));
  }

  @Test
  public void testInterval() throws Exception {
    final Date now = new Date();
    assertIntervalSchedule(forInterval(0L, null), null, 0L);
    assertIntervalSchedule(forInterval(42L, null), null, 42L);
    assertIntervalSchedule(forInterval(0L, now), now, 0L);
    assertIntervalSchedule(forInterval(42L, now), now, 42L);
  }

  @Test
  public void testIntervalWithNegativeValue1() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      forInterval(-42L, null);
    });
  }

  @Test
  public void testIntervalWithNegativeValue2() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      forInterval(-42L, new Date());
    });
  }

  @Test
  public void testCronScheduleDefaultTimeZone() {
    assertCronSchedule(forCronExpression(AT_2AM), AT_2AM, null);
    assertCronSchedule(forCronExpression(AT_4AM, null), AT_4AM, null);
  }

  // Note: Cron expressions are not validated at this level, so these are accepted
  @Test
  public void testCronScheduleMalformedCronExpression() {
    assertCronSchedule(forCronExpression(BAD), BAD, null);
    assertCronSchedule(forCronExpression(BAD, null), BAD, null);
  }

  @Test
  public void testCronScheduleSpecificTimeZone() {
    TimeZone zone = getTimeZone("America/Chicago");
    if (zone.equals(TimeZone.getDefault())) {
      zone = getTimeZone("Australia/Sydney");
    }
    assertCronSchedule(forCronExpression(AT_2AM, zone), AT_2AM, zone);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testCronScheduleNull1() {
    assertThrows(IllegalArgumentException.class, () -> {
      forCronExpression(null);
    });
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testCronScheduleNull2() {
    assertThrows(IllegalArgumentException.class, () -> {
      forCronExpression(null, TimeZone.getDefault());
    });
  }

  private static void assertIntervalSchedule(final Schedule sched,
      @Nullable final Date firstRunTime,
      final long intervalInMillis) {
    assertSimpleSchedule(simple(sched), firstRunTime, intervalInMillis);
  }

  private static void assertSimpleSchedule(final IntervalScheduleInfo info,
      @Nullable final Date firstRunTime,
      final long intervalInMillis) {
    assertThat(info.getFirstRunTime(), is(firstRunTime));
    assertThat(info.getIntervalInMillis(), is(intervalInMillis));
  }

  private static IntervalScheduleInfo simple(final Schedule sched) {
    assertThat(sched.getType(), is(INTERVAL));
    assertThat(sched.getIntervalScheduleInfo(), notNullValue());
    assertThat(sched.getCronScheduleInfo(), nullValue());
    return sched.getIntervalScheduleInfo();
  }

  private static void assertCronSchedule(final Schedule sched,
      final String cronExpression,
      @Nullable final TimeZone timeZone) {
    assertCronSchedule(cron(sched), cronExpression, timeZone);
  }

  private static void assertCronSchedule(final CronScheduleInfo info,
      final String cronExpression,
      @Nullable final TimeZone timeZone) {
    assertThat(info.getCronExpression(), is(cronExpression));
    assertThat(info.getTimeZone(), is(timeZone));
  }

  private static CronScheduleInfo cron(final Schedule sched) {
    assertThat(sched.getType(), is(CRON_EXPRESSION));
    assertThat(sched.getIntervalScheduleInfo(), nullValue());
    assertThat(sched.getCronScheduleInfo(), notNullValue());
    return sched.getCronScheduleInfo();
  }
}
