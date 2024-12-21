package com.pmi.tpd.core.server;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.lifecycle.config.ApplicationStartedEvent;

public class DefaultApplicationStatusService implements IApplicationStatusService {

    /** */
    private final IEventAdvisorService<?> eventAdvisorService;

    /** */
    private final IApplicationProperties propertiesService;
    /** */
    // private final Map<String, Boolean> resources;
    /** */

    // private final InternalThrottleService throttleService;
    /** */
    private long serverBusyMessageTimeout;

    /** */
    private long serverBusyQueueTime;

    /** */
    private volatile ApplicationState state;

    @Inject
    public DefaultApplicationStatusService(final IApplicationProperties propertiesService,
            final IEventAdvisorService<?> eventAdvisorService) {
        this.propertiesService = propertiesService;
        // this.throttleService = throttleService;

        this.eventAdvisorService = eventAdvisorService;
        // resources = ImmutableMap.of();
        state = ApplicationState.STARTING;
    }

    @EventListener
    public void onStart(final ApplicationStartedEvent event) {
        // check whether the application has been evented
        state = ApplicationState.RUNNING;
    }

    /**
     * Sets how long a warning banner is displayed in the UI after a request is rejected due to excessive load. This
     * value is in <b>minutes</b>. Using 0, or a negative value, disables displaying the banner.
     *
     * @param resourceBusyMessageTimeout
     *            timeout (minutes) banner duration.
     */
    public void setServerBusyMessageTimeout(final long resourceBusyMessageTimeout) {
        this.serverBusyMessageTimeout = TimeUnit.MINUTES.toMillis(resourceBusyMessageTimeout);
    }

    /**
     * Sets how long requests need to be queued before they cause a warning banner to appear. This value is in
     * <b>seconds</b>. Using 0, or a negative value, disables displaying the banner.
     *
     * @param serverBusyQueueTime
     *            time (seconds) before warning banner.
     */
    public void setServerBusyQueueTime(final long serverBusyQueueTime) {
        setServerBusyQueueTime(serverBusyQueueTime, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        state = ApplicationState.STOPPING;
    }

    @VisibleForTesting
    void setServerBusyQueueTime(final long serverBusyQueueTime, final TimeUnit timeUnit) {
        this.serverBusyQueueTime = timeUnit.toMillis(serverBusyQueueTime);
    }

    @Override
    @Nonnull
    public ApplicationState getState() {
        final ApplicationState current = state;
        if (current == ApplicationState.STOPPING) {
            return current;
        }

        final String highestLevel = eventAdvisorService.findHighestEventLevel();
        if (highestLevel != null) {
            if (eventAdvisorService.isLevelAtLeast(highestLevel, EventLevel.ERROR)) {
                return ApplicationState.ERROR;
            }
            if (eventAdvisorService.isLevelAtMost(highestLevel, IEventAdvisorService.LEVEL_MAINTENANCE)) {
                return ApplicationState.MAINTENANCE;
            }
            // Ignore EventLevel.WARNING for now. It signifies a degraded state, but should be recoverable
        }

        if (current == ApplicationState.RUNNING && !propertiesService.isSetup() && !propertiesService.isAutoSetup()) {
            return ApplicationState.FIRST_RUN;
        }

        return current;
    }

    @Override
    public boolean hasRecentlyRejectedRequests() {
        if (serverBusyMessageTimeout < 1) {
            return false;
        }

        // long timeSinceLastRejection = 0;
        // for (final String resource : resources.keySet()) { // Display banners for rejecting _any_ resource
        // final long resourceTime = throttleService.getTimeSinceLastRejectedTicketRequest(resource);
        // if (resourceTime > timeSinceLastRejection) {
        // timeSinceLastRejection = resourceTime;
        // }
        // }
        // // is time since last rejection within the timeout
        // return timeSinceLastRejection != 0 && timeSinceLastRejection < serverBusyMessageTimeout;
        return false;
    }

    @Override
    public boolean isQueueingRequests() {
        if (serverBusyQueueTime < 1) {
            return false;
        }

        // long longestQueuingTime = 0;
        // for (final Map.Entry<String, Boolean> resource : resources.entrySet()) {
        // if (resource.getValue()) { // Only display a warning banner for specific resources (scm-hosting)
        // final long resourceTime = throttleService
        // .getLongestQueueingTimeForCurrentTicketRequests(resource.getKey());
        // if (resourceTime > longestQueuingTime) {
        // longestQueuingTime = resourceTime;
        // }
        // }
        // }
        // return longestQueuingTime != 0 && longestQueuingTime > serverBusyQueueTime;
        return false;
    }

}
