package com.pmi.tpd.scheduler.quartz.hazelcast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;

import com.google.common.collect.Maps;

/**
 * @since 1.1
 */
public abstract class AbstractTriggerConfig implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // Note, all of these attribute names must match the associated fields below
    public static final String ATTR_CALENDAR = "calendar";

    public static final String ATTR_JOB_GROUP = "jobGroup";

    public static final String ATTR_JOB_NAME = "jobName";

    public static final String ATTR_NEXT_FIRE_TIME = "nextFireTime";

    public static final String ATTR_STATE = "state";

    public static final int NO_FIRE_TIME = -1;

    private final String calendarName;

    private final Map<String, Serializable> data;

    // Separate the job into jobGroup and jobName fields
    // so they can be searched by Hazelcast Predicates
    private final String jobGroup;

    private final String jobName;

    private final long nextFireTime;

    private final long previousFireTime;

    private final Trigger.TriggerState state;

    protected AbstractTriggerConfig(final AbstractBuilder<?, ?> builder) {
        calendarName = builder.calendarName;
        data = builder.data;
        jobGroup = checkNotNull(builder.job, "job").getGroup();
        jobName = builder.job.getName();
        nextFireTime = builder.nextFireTime;
        previousFireTime = builder.previousFireTime;
        state = checkNotNull(builder.state, "state");
    }

    public abstract AbstractBuilder<?, ?> copy();

    public String getCalendarName() {
        return calendarName;
    }

    public Map<String, Serializable> getData() {
        return data == null ? null : Collections.unmodifiableMap(data);
    }

    public JobKey getJob() {
        return new JobKey(jobName, jobGroup);
    }

    public long getNextFireTime() {
        return nextFireTime;
    }

    public long getPreviousFireTime() {
        return previousFireTime;
    }

    public Trigger.TriggerState getState() {
        return state;
    }

    public OperableTrigger toTrigger(final TriggerKey triggerKey) {
        // noinspection unchecked
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final TriggerBuilder<OperableTrigger> builder = (TriggerBuilder) TriggerBuilder.newTrigger();
        builder.withIdentity(triggerKey);

        builder.modifiedByCalendar(calendarName);
        builder.forJob(jobName, jobGroup);

        if (data != null) {
            builder.usingJobData(new JobDataMap(data));
        }

        builder.withSchedule(newScheduleBuilder());

        final OperableTrigger trigger = builder.build();

        trigger.setNextFireTime(getNextFireTime() == NO_FIRE_TIME ? null : new Date(getNextFireTime()));
        trigger.setPreviousFireTime(getPreviousFireTime() == NO_FIRE_TIME ? null : new Date(getPreviousFireTime()));

        return trigger;
    }

    protected abstract <T extends Trigger> ScheduleBuilder<T> newScheduleBuilder();

    public static AbstractTriggerConfig fromTrigger(final Trigger trigger, final Trigger.TriggerState state) {
        AbstractBuilder<?, ?> builder;
        if (trigger instanceof CronTrigger) {
            builder = new CronTriggerConfig.Builder((CronTrigger) trigger);
        } else if (trigger instanceof SimpleTrigger) {
            builder = new SimpleTriggerConfig.Builder((SimpleTrigger) trigger);
        } else {
            return null;
        }

        builder.state(state);

        return builder.build();
    }

    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends AbstractTriggerConfig> {

        private String calendarName;

        private Map<String, Serializable> data;

        private JobKey job;

        private long nextFireTime;

        private long previousFireTime;

        private Trigger.TriggerState state;

        protected AbstractBuilder() {
            nextFireTime = NO_FIRE_TIME;
            previousFireTime = NO_FIRE_TIME;
            state = Trigger.TriggerState.NONE;
        }

        protected AbstractBuilder(final AbstractTriggerConfig trigger) {
            this();

            calendarName(trigger.getCalendarName());
            data(trigger.getData());
            job(trigger.getJob());
            nextFireTime(trigger.getNextFireTime());
            previousFireTime(trigger.getPreviousFireTime());
            state(trigger.getState());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected AbstractBuilder(final Trigger trigger) {
            this();

            calendarName(trigger.getCalendarName());
            data((Map) trigger.getJobDataMap());
            job(trigger.getJobKey());
            nextFireTime(trigger.getNextFireTime());
            previousFireTime(trigger.getPreviousFireTime());
        }

        public abstract T build();

        public B calendarName(final String value) {
            calendarName = value;

            return self();
        }

        public B data(final Map<String, Serializable> value) {
            // We are using a hash map here to preserve any null keys or values
            data = value == null || value.isEmpty() ? null : Maps.newHashMap(value);

            return self();
        }

        public B job(final JobKey value) {
            job = value;

            return self();
        }

        public B nextFireTime(final Date value) {
            nextFireTime = value == null ? NO_FIRE_TIME : value.getTime();

            return self();
        }

        public B nextFireTime(final long value) {
            nextFireTime = value;

            return self();
        }

        public B previousFireTime(final Date value) {
            previousFireTime = value == null ? NO_FIRE_TIME : value.getTime();

            return self();
        }

        public B previousFireTime(final long value) {
            previousFireTime = value;

            return self();
        }

        public B state(final Trigger.TriggerState value) {
            state = value;

            return self();
        }

        protected abstract B self();

    }

}
