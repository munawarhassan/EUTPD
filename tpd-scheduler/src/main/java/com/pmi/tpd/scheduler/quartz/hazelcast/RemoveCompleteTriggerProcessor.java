package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Map;

import org.quartz.TriggerKey;

import com.hazelcast.map.EntryProcessor;

/**
 * @since 1.1
 */
public class RemoveCompleteTriggerProcessor implements EntryProcessor<TriggerKey, AbstractTriggerConfig, Boolean> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Boolean process(final Map.Entry<TriggerKey, AbstractTriggerConfig> entry) {
        final AbstractTriggerConfig config = entry.getValue();

        // It is possible that the trigger was rescheduled during job execution.
        // As a result we need to check the next fire time before removing
        if (config != null && config.getNextFireTime() == AbstractTriggerConfig.NO_FIRE_TIME) {
            // Setting the value to null indicates to Hazelcast to remove the entry
            entry.setValue(null);
            return true;
        }

        return false;
    }

}
