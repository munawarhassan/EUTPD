package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.event.advisor.event.AddEvent;
import com.pmi.tpd.api.event.advisor.event.RemoveEvent;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.core.maintenance.event.MaintenanceEndedEvent;
import com.pmi.tpd.core.maintenance.event.MaintenanceStartedEvent;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultMaintenanceModeHelper implements IMaintenanceModeHelper {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMaintenanceModeHelper.class);

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final ILifecycleAwareSchedulerService schedulerService;

    /**
     * @param eventPublisher
     * @param schedulerService
     */
    public DefaultMaintenanceModeHelper(final IEventPublisher eventPublisher,
            final ILifecycleAwareSchedulerService schedulerService) {
        this.eventPublisher = eventPublisher;
        this.schedulerService = schedulerService;
    }

    @Override
    public void lock(@Nonnull final MaintenanceApplicationEvent event) {
        pauseScheduler();

        eventPublisher.publish(new MaintenanceStartedEvent(this));
        eventPublisher.publish(new AddEvent(this, event));
    }

    @Override
    public void unlock(@Nonnull final MaintenanceApplicationEvent event) {
        eventPublisher.publish(new RemoveEvent(this, event));
        eventPublisher.publish(new MaintenanceEndedEvent(this));

        resumeScheduler();
    }

    private void pauseScheduler() {
        try {
            // Put the scheduler into standby mode. When the scheduler is restarted, the triggers which should
            // have fired while the scheduler was in standby mode will be fired based on a misfire threshold
            schedulerService.standby();

            // Note: After the scheduler is put into standby, we do _not_ attempt to drain it.
        } catch (final SchedulerServiceException e) {
            LOGGER.warn("The scheduler threw an exception when it was paused. This usually means the scheduler has "
                    + "already been shutdown.",
                e);
        }
    }

    private void resumeScheduler() {
        try {
            // Because the scheduler has been in standby mode while the delegate MaintenanceTask ran, it's possible that
            // some jobs
            // may have missed their scheduled start times. Those will trigger a SchedulerException on restart.
            schedulerService.start();
        } catch (final SchedulerServiceException e) {
            // Unfortunately for any such jobs, there is very little we can try to do about it. It doesn't make sense to
            // mark the entire migration as failed (because, actually, by this point, we pretty much know it succeeded),
            // so instead we log the exception and move forward.
            LOGGER.warn("The scheduler threw an exception when it was resumed. This usually means some jobs missed "
                    + "their configured start time while the scheduler was paused.",
                e);
        }
    }
}
