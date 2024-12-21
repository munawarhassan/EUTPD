package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Date;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.OperableTrigger;

/**
 * @since 1.1
 */
public class SimpleTriggerConfig extends AbstractTriggerConfig {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final long endTime;

    private final int repeatCount;

    private final long repeatInterval;

    private final long startTime;

    private final int timesTriggered;

    private SimpleTriggerConfig(final Builder builder) {
        super(builder);

        endTime = builder.endTime;
        repeatCount = builder.repeatCount;
        repeatInterval = builder.repeatInterval;
        startTime = builder.startTime;
        timesTriggered = builder.timesTriggered;

    }

    @Override
    public Builder copy() {
        return new Builder(this);
    }

    public long getEndTime() {
        return endTime;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getTimesTriggered() {
        return timesTriggered;
    }

    @Override
    public OperableTrigger toTrigger(final TriggerKey triggerKey) {
        final SimpleTriggerImpl trigger = (SimpleTriggerImpl) super.toTrigger(triggerKey);

        // Unfortunately, these can't be set using the SimpleSchedulerBuilder
        trigger.setEndTime(endTime == NO_FIRE_TIME ? null : new Date(endTime));
        trigger.setStartTime(startTime == NO_FIRE_TIME ? null : new Date(startTime));
        trigger.setTimesTriggered(timesTriggered);

        return trigger;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SimpleScheduleBuilder newScheduleBuilder() {
        return SimpleScheduleBuilder.simpleSchedule()
                .withRepeatCount(repeatCount)
                .withIntervalInMilliseconds(repeatInterval);
    }

    public static class Builder extends AbstractBuilder<Builder, SimpleTriggerConfig> {

        private long endTime;

        private int repeatCount;

        private long repeatInterval;

        private long startTime;

        private int timesTriggered;

        public Builder() {
            endTime = NO_FIRE_TIME;
            startTime = NO_FIRE_TIME;
        }

        public Builder(final SimpleTrigger trigger) {
            super(trigger);

            endTime(trigger.getEndTime());
            repeatCount(trigger.getRepeatCount());
            repeatInterval(trigger.getRepeatInterval());
            startTime(trigger.getStartTime());
            timesTriggered(trigger.getTimesTriggered());
        }

        public Builder(final SimpleTriggerConfig trigger) {
            super(trigger);

            endTime(trigger.getEndTime());
            repeatCount(trigger.getRepeatCount());
            repeatInterval(trigger.getRepeatInterval());
            startTime(trigger.getStartTime());
            timesTriggered(trigger.getTimesTriggered());
        }

        @Override
        public SimpleTriggerConfig build() {
            return new SimpleTriggerConfig(this);
        }

        public Builder endTime(final Date value) {
            endTime = value == null ? NO_FIRE_TIME : value.getTime();

            return self();
        }

        public Builder endTime(final long value) {
            endTime = value;

            return self();
        }

        public Builder repeatCount(final int value) {
            repeatCount = value;

            return self();
        }

        public Builder repeatInterval(final long value) {
            repeatInterval = value;

            return self();
        }

        public Builder startTime(final Date value) {
            startTime = value == null ? NO_FIRE_TIME : value.getTime();

            return self();
        }

        public Builder startTime(final long value) {
            startTime = value;

            return self();
        }

        public Builder timesTriggered(final int value) {
            timesTriggered = value;

            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
