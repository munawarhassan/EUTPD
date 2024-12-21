package com.pmi.tpd.scheduler.quartz.hazelcast;

import java.util.Map;

import org.quartz.Trigger;
import org.quartz.TriggerKey;

import com.hazelcast.map.EntryProcessor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class AbstractStateChangeTriggerProcessor<T>
        implements EntryProcessor<TriggerKey, AbstractTriggerConfig, T> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Trigger.TriggerState newState;

    protected AbstractStateChangeTriggerProcessor(final Trigger.TriggerState newState) {
        this.newState = newState;
    }

    @Override
    public T process(final Map.Entry<TriggerKey, AbstractTriggerConfig> entry) {

        final AbstractTriggerConfig oldConfig = entry.getValue();

        if (oldConfig == null) {
            return triggerMissing();
        }

        final AbstractTriggerConfig newConfig = oldConfig.copy().state(newState).build();

        entry.setValue(newConfig);

        return triggerUpdated(newConfig);
    }

    protected abstract T triggerMissing();

    protected abstract T triggerUpdated(AbstractTriggerConfig newConfig);

}
