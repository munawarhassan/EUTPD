package com.pmi.tpd.cluster.hazelcast;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.pmi.tpd.api.ApplicationConstants.LifeCycle;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;

/**
 * When the Spring {@code ApplicationContext} is being started, and the plugin framework has been fully initialized
 * update the capabilities in the local cluster member with {@code PARTITION_HOST} and {@code EXECUTOR}. This will allow
 * Hazelcast to send data partitions and tasks to this node. When the Spring {@code ApplicationContext} is being
 * shutdown, attempt to make the {@code HazelcastInstance} safe to shutdown but <i>do not shut it down</i>.
 * <p>
 * "Safe", from the Hazelcast documentation, means all partitions on this node are synchronized with at least one other
 * node in the cluster (potentially more if more than one backup is configured). That ensures, when this node shuts
 * down, clustered data will not be lost.
 * <p>
 * The goal, here, is to balance:
 * <ul>
 * <li>Needing to leave the cluster so that other members do not dispatch work to this node while it's shutting down,
 * when it won't be able to handle that work</li>
 * <li>Needing to remain in the cluster so that components still terminating are able to shutdown gracefully</li>
 * </ul>
 * The hope is that, after all local data has been synchronized elsewhere in the cluster, other nodes will no longer
 * make requests to this node. {@code HazelcastInstance.shutdown()} is handled by {@code ContextCleanupListener} and is
 * only run after the Spring {@code ApplicationContext}, and all servlets and filters, has fully terminated.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastLifecycle extends AbstractSmartLifecycle {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastLifecycle.class);

    /** */
    private final HazelcastInstance hazelcast;

    /** */
    private final int shutdownTimeout;

    @Inject
    public HazelcastLifecycle(final HazelcastInstance hazelcast, final int shutdownTimeout) {
        this.hazelcast = hazelcast;
        this.shutdownTimeout = shutdownTimeout;
    }

    @Override
    public int getPhase() {
        return LifeCycle.LIFECYCLE_PHASE_HAZELCAST;
    }

    @Override
    public void stop() {
        try {
            LOGGER.info("Draining partitions from local member");
            if (hazelcast.getPartitionService().forceLocalMemberToBeSafe(shutdownTimeout, TimeUnit.SECONDS)) {
                LOGGER.info("Drained local partitions");
            } else {
                LOGGER.info("Could not drain local partitions");
            }
        } catch (final HazelcastInstanceNotActiveException e) {
            // do nothing
        }

        super.stop();
    }
}
