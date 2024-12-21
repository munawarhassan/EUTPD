package com.pmi.tpd.scheduler.quartz.hazelcast;

import org.quartz.Trigger;

/**
 * @since 1.1
 */
public class StateChangeTriggerProcessor extends AbstractStateChangeTriggerProcessor<Boolean> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public StateChangeTriggerProcessor(final Trigger.TriggerState newState) {
        super(newState);
    }

    @Override
    protected Boolean triggerMissing() {
        return Boolean.FALSE;
    }

    @Override
    protected Boolean triggerUpdated(final AbstractTriggerConfig newConfig) {
        return Boolean.TRUE;
    }

}
