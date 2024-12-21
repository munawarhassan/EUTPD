package com.pmi.tpd.scheduler.quartz;

import static com.pmi.tpd.scheduler.quartz.QuartzSchedulerFacade.QUARTZ_PARAMETERS_KEY;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.scheduler.status.AbstractJobDetailsFactory;

/**
 * Creates {@code JobDetails} objects out of Quartz 2.x {@code Trigger}s.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class QuartzJobDetailsFactory extends AbstractJobDetailsFactory<Trigger> {

  QuartzJobDetailsFactory(final QuartzSchedulerService schedulerService) {
    super(schedulerService);
  }

  @Nonnull
  @Override
  protected JobRunnerKey getJobRunnerKey(final Trigger trigger) {
    return JobRunnerKey.of(trigger.getJobKey().getName());
  }

  @Nonnull
  @Override
  protected Schedule getSchedule(final Trigger trigger) {
    if (trigger instanceof CronTrigger) {
      final CronTrigger cron = (CronTrigger) trigger;
      return Schedule.forCronExpression(cron.getCronExpression(), cron.getTimeZone());
    }
    if (trigger instanceof SimpleTrigger) {
      final SimpleTrigger simple = (SimpleTrigger) trigger;
      return Schedule.forInterval(simple.getRepeatInterval(), simple.getStartTime());
    }
    throw new SchedulerRuntimeException("The job with jobId '" + trigger.getKey().getName()
        + "' has an unsupported trigger class: " + trigger.getClass().getName());
  }

  @Nullable
  @Override
  protected byte[] getSerializedParameters(final Trigger trigger) {
    return (byte[]) trigger.getJobDataMap().get(QUARTZ_PARAMETERS_KEY);
  }

  @Nullable
  @Override
  protected Date getNextRunTime(final Trigger trigger) {
    return trigger.getNextFireTime();
  }
}
