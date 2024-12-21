package com.pmi.tpd.scheduler.quartz.hazelcast;

import org.quartz.JobKey;
import org.quartz.Trigger;

/**
 * @since 1.1
 */
public class JobStateChangeTriggerProcessor extends AbstractStateChangeTriggerProcessor<JobKey> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public JobStateChangeTriggerProcessor(final Trigger.TriggerState newState) {
        super(newState);
    }

    @Override
    protected JobKey triggerMissing() {
        return null;
    }

    @Override
    protected JobKey triggerUpdated(final AbstractTriggerConfig newConfig) {
        return newConfig.getJob();
    }

}
