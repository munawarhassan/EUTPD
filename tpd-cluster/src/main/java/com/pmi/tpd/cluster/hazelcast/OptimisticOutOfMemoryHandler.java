package com.pmi.tpd.cluster.hazelcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OutOfMemoryHandler;
import com.hazelcast.instance.impl.OutOfMemoryErrorDispatcher;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class OptimisticOutOfMemoryHandler extends OutOfMemoryHandler {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(OptimisticOutOfMemoryHandler.class);

    @Override
    public void onOutOfMemory(final OutOfMemoryError oom, final HazelcastInstance[] hazelcastInstances) {
        LOGGER.warn("OutOfMemoryError occurred attempting to continue operating as normal", oom);
        LOGGER.debug("Attempting to re-register {} hazelcast instances", hazelcastInstances.length);
        for (final HazelcastInstance hazelcastInstance : hazelcastInstances) {
            OutOfMemoryErrorDispatcher.registerServer(hazelcastInstance);
            LOGGER.trace("Re-registered {}", hazelcastInstance);
        }
    }
}
