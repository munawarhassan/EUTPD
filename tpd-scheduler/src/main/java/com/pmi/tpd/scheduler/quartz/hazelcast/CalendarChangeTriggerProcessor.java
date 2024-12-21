package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Map;

import org.quartz.Calendar;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;

import com.hazelcast.map.EntryProcessor;

/**
 * @since 1.1
 */
public class CalendarChangeTriggerProcessor implements EntryProcessor<TriggerKey, AbstractTriggerConfig, Void> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final long misfireThreshold;

    private final Calendar newCalendar;

    public CalendarChangeTriggerProcessor(final long misfireThreshold, final Calendar newCalendar) {
        this.misfireThreshold = misfireThreshold;
        this.newCalendar = newCalendar;
    }

    @Override
    public Void process(final Map.Entry<TriggerKey, AbstractTriggerConfig> entry) {
        final AbstractTriggerConfig oldConfig = entry.getValue();

        final OperableTrigger trigger = oldConfig.toTrigger(entry.getKey());
        trigger.updateWithNewCalendar(newCalendar, misfireThreshold);
        final AbstractTriggerConfig newConfig = AbstractTriggerConfig.fromTrigger(trigger, oldConfig.getState());

        entry.setValue(newConfig);

        return null;
    }

}
