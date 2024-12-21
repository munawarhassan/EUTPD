package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Map;

import org.quartz.Calendar;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;

import com.google.common.base.Objects;
import com.hazelcast.map.EntryProcessor;

/**
 * @since 1.1
 */
public class TriggerFiredProcessor implements EntryProcessor<TriggerKey, AbstractTriggerConfig, Boolean> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Calendar calendar;

    private final String calendarName;

    public TriggerFiredProcessor(final Calendar calendar, final String calendarName) {
        this.calendar = calendar;
        this.calendarName = calendarName;
    }

    @Override
    public Boolean process(final Map.Entry<TriggerKey, AbstractTriggerConfig> entry) {
        final AbstractTriggerConfig config = entry.getValue();

        // If the state of calendar has changed in the mean time, don't update the trigger
        if (config == null || config.getState() != Trigger.TriggerState.NORMAL
                || !Objects.equal(calendarName, config.getCalendarName())) {
            return false;
        }

        final OperableTrigger trigger = config.toTrigger(entry.getKey());
        trigger.triggered(calendar);
        entry.setValue(AbstractTriggerConfig.fromTrigger(trigger, config.getState()));

        return true;
    }
}
