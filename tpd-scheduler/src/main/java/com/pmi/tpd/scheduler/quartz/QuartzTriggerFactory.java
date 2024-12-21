package com.pmi.tpd.scheduler.quartz;

import static com.pmi.tpd.scheduler.quartz.QuartzSchedulerFacade.QUARTZ_PARAMETERS_KEY;
import static com.pmi.tpd.scheduler.quartz.QuartzSchedulerFacade.QUARTZ_TRIGGER_GROUP;
import static com.pmi.tpd.scheduler.util.CronExpressionQuantizer.quantizeSecondsField;
import static com.pmi.tpd.scheduler.util.QuartzParseExceptionMapper.mapException;
import static com.pmi.tpd.scheduler.util.TimeIntervalQuantizer.quantizeToMinutes;
import static org.quartz.CronScheduleBuilder.cronScheduleNonvalidatedExpression;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerKey.triggerKey;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;

import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.CronScheduleInfo;
import com.pmi.tpd.api.scheduler.config.IntervalScheduleInfo;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.util.ParameterMapSerializer;
import com.pmi.tpd.scheduler.cron.CronSyntaxException;
import com.pmi.tpd.scheduler.spi.ISchedulerServiceConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
class QuartzTriggerFactory {

  private final ISchedulerServiceConfiguration config;

  private final ParameterMapSerializer parameterMapSerializer;

  QuartzTriggerFactory(final ISchedulerServiceConfiguration config,
      final ParameterMapSerializer parameterMapSerializer) {
    this.config = config;
    this.parameterMapSerializer = parameterMapSerializer;
  }

  public TriggerBuilder<?> buildTrigger(final JobId jobId, final JobConfig jobConfig)
      throws SchedulerServiceException {
    final byte[] parameters = parameterMapSerializer.serializeParameters(jobConfig.getParameters());
    final JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(QUARTZ_PARAMETERS_KEY, parameters);
    return buildTrigger(jobConfig.getSchedule()).withIdentity(triggerKey(jobId.toString(), QUARTZ_TRIGGER_GROUP))
        .usingJobData(jobDataMap);
  }

  public TriggerBuilder<?> buildTrigger(final Schedule schedule) throws SchedulerServiceException {
    switch (schedule.getType()) {
      case INTERVAL:
        return getSimpleTrigger(schedule.getIntervalScheduleInfo());
      case CRON_EXPRESSION:
        return getCronTrigger(schedule.getCronScheduleInfo());
    }
    throw new IllegalStateException("type=" + schedule.getType());
  }

  private static TriggerBuilder<SimpleTrigger> getSimpleTrigger(final IntervalScheduleInfo info) {
    final Date startTime = info.getFirstRunTime() != null ? info.getFirstRunTime() : new Date();
    return TriggerBuilder.newTrigger().withSchedule(interval(info.getIntervalInMillis())).startAt(startTime);
  }

  private static SimpleScheduleBuilder interval(final long intervalInMillis) {
    if (intervalInMillis == 0L) {
      return simpleSchedule().withRepeatCount(0);
    }

    return simpleSchedule().withIntervalInMilliseconds(intervalInMillis).repeatForever();
  }

  @SuppressWarnings("unused")
  private static SimpleScheduleBuilder intervalMinute(final long intervalInMillis) {
    if (intervalInMillis == 0L) {
      return simpleSchedule().withRepeatCount(0);
    }
    return simpleSchedule().withIntervalInMilliseconds(quantizeToMinutes(intervalInMillis)).repeatForever();
  }

  private TriggerBuilder<CronTrigger> getCronTrigger(final CronScheduleInfo info) throws CronSyntaxException {
    try {
      // Force validation to happen first
      final String cronExpression = new CronExpression(info.getCronExpression()).getCronExpression();
      final CronScheduleBuilder schedule = cronScheduleNonvalidatedExpression(
          quantizeSecondsField(cronExpression)).inTimeZone(getTimeZone(info));
      return TriggerBuilder.newTrigger().withSchedule(schedule);
    } catch (final ParseException pe) {
      throw mapException(info.getCronExpression().toUpperCase(Locale.US), pe);
    }
  }

  @Nonnull
  private TimeZone getTimeZone(final CronScheduleInfo info) {
    TimeZone timeZone = info.getTimeZone();
    if (timeZone == null) {
      timeZone = config.getDefaultTimeZone();
      if (timeZone == null) {
        timeZone = TimeZone.getDefault();
      }
    }
    return timeZone;
  }
}
