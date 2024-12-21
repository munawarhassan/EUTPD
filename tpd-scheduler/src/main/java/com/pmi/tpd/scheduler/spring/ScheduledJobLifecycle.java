package com.pmi.tpd.scheduler.spring;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.OrderComparator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;

/**
 * @author Christophe Friederich
 * @since 1.0
 */

public class ScheduledJobLifecycle extends AbstractSmartLifecycle implements IStartable {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledJobLifecycle.class);

    /** */
    private final ISchedulerService schedulerService;

    /** */
    private final ImmutableList<IScheduledJobSource> sources;

    /**
     * Default constructor.
     * 
     * @param schedulerService
     *            schedule service.
     * @param sources
     *            the list of Scheduled job source.
     */
    @Inject
    public ScheduledJobLifecycle(final ISchedulerService schedulerService, List<IScheduledJobSource> sources) {
        sources = Lists.newArrayList(sources);
        OrderComparator.sort(sources);

        this.schedulerService = schedulerService;
        this.sources = ImmutableList.copyOf(sources);
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public int getPhase() {
        return ApplicationConstants.LifeCycle.LIFECYCLE_PHASE_SCHEDULED_JOBS;
    }

    @Override
    public void start() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Applying {} job sources", sources.size());
        }
        for (final IScheduledJobSource source : sources) {
            final String name = getName(source);
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Scheduling jobs for {}", name);
                }
                source.schedule(schedulerService);
            } catch (final SchedulerServiceException e) {
                LOGGER.error("Failed to schedule jobs for {}", name, e);
            }
        }

        super.start();
    }

    @Override
    public void stop() {
        for (final IScheduledJobSource source : sources.reverse()) {
            final String name = getName(source);
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unscheduling jobs for {}", name);
                }
                source.unschedule(schedulerService);
            } catch (final SchedulerServiceException e) {
                LOGGER.error("Failed to unschedule jobs for {}", name, e);
            }
        }

        super.stop();
    }

    private static String getName(final IScheduledJobSource source) {
        return AopUtils.getTargetClass(source).getSimpleName();
    }

}
