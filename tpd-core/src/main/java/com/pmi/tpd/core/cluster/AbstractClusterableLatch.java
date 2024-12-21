package com.pmi.tpd.core.cluster;

import java.util.concurrent.TimeUnit;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.IExecutorService;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.cluster.BaseClusterableLatch;
import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.cluster.spi.IUnlatchFailedEventTask;

/**
 * Base implementation of {@link ILatch latches} that support both local and cluster-wide latching and draining.
 * Subclasses need only implement {@link #acquireLocally()}, {@link #drainLocally(long, TimeUnit, boolean)} and
 * {@link #unlatchLocally()}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractClusterableLatch extends BaseClusterableLatch {

    protected AbstractClusterableLatch(final LatchMode mode, final Cluster cluster, final IExecutorService executor,
            final IEventAdvisorService<?> eventAdvisorService, final String latchServiceBeanName) {
        super(mode, cluster, executor, eventAdvisorService, latchServiceBeanName);
    }

    @Override
    protected IUnlatchFailedEventTask createUnlatchFailedEventTask() {
        return new UnlatchFailedEventTask(eventAdvisorService);
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static final class UnlatchFailedEventTask implements IUnlatchFailedEventTask {

        /** */
        private static final long serialVersionUID = 1L;

        /** */
        private static final String UNLATCH_FAILED_EVENT_TYPE = "unlatch-failed";

        /** */
        private static final String UNLATCH_FAILED_EVENT_LEVEL = "error";

        private final IEventAdvisorService<?> eventAdvisorService;

        private UnlatchFailedEventTask(final IEventAdvisorService<?> eventAdvisorService) {
            this.eventAdvisorService = eventAdvisorService;
        }

        @Override
        public void run() {
            final Event event = new Event(eventAdvisorService.getEventType(UNLATCH_FAILED_EVENT_TYPE).orElse(null),
                    "Failed to unlatch this cluster node",
                    eventAdvisorService.getEventLevel(UNLATCH_FAILED_EVENT_LEVEL).orElse(null));
            eventAdvisorService.publishEvent(event);
        }
    }
}
