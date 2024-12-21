package com.pmi.tpd.scheduler.spring;

import static com.google.common.collect.Iterables.transform;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;

import com.google.common.base.Function;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;

/**
 * Coordinates the lifecycle of the underlying {@link LifecycleAwareSchedulerService}
 * <p>
 * Uses composition over inheritance to work around the fact that {@code Quartz1SchedulerService} has methods marked as
 * {@code final}, preventing a subclass from implementing {@link SmartLifecycle} as well.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class SchedulerLifecycle extends AbstractSmartLifecycle {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerLifecycle.class);

    /** */
    private static final Function<IRunningJob, JobId> TO_JOB_ID = runningJob -> runningJob.getJobId();

    /** */
    private final ILifecycleAwareSchedulerService schedulerService;

    /** */
    private final int shutdownTimeout;

    /**
     * Default constructor.
     * 
     * @param schedulerService
     *            scheduler service.
     * @param shutdownTimeout
     *            the shutdown timeout.
     */
    @Autowired
    public SchedulerLifecycle(final ILifecycleAwareSchedulerService schedulerService, final int shutdownTimeout) {
        this.schedulerService = schedulerService;
        this.shutdownTimeout = shutdownTimeout;
    }

    @Override
    public int getPhase() {
        return ApplicationConstants.LifeCycle.LIFECYCLE_PHASE_SCHEDULER;
    }

    @Override
    public void start() {
        try {
            schedulerService.start();

            super.start();
        } catch (final SchedulerServiceException e) {
            throw new IllegalStateException("The scheduler could not be started", e);
        }
    }

    @Override
    public void stop() {
        // Assumption: Exactly one Spring context will be created/refreshed/shutdown and its active profiles will
        // never change at runtime. If that assumption is violated, this will cause the scheduler to
        // mishehave, because once shutdown it cannot be restarted
        schedulerService.shutdown();

        try {
            if (!schedulerService.waitUntilIdle(shutdownTimeout, TimeUnit.SECONDS)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Scheduler service is not idle after {} seconds.", shutdownTimeout);
                }
            }
        } catch (final InterruptedException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Interrupted while waiting for running jobs to complete.", e);
            }
        }

        final Collection<IRunningJob> locallyRunningJobs = schedulerService.getLocallyRunningJobs();
        if (!locallyRunningJobs.isEmpty()) {
            LOGGER.warn("The following jobs could not be canceled. They will be killed when the JVM terminates: {}",
                transform(locallyRunningJobs, TO_JOB_ID));
        }

        super.stop();
    }
}
