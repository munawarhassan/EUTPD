package com.pmi.tpd.cluster;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.hazelcast.core.HazelcastInstance;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastClusterService implements IClusterService {

    /** */
    private final IClusterInformation clusterInformation;

    /** */
    private final HazelcastInstance hazelcast;

    /**
     * @param hazelcast
     */
    @Inject
    public HazelcastClusterService(final HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;

        clusterInformation = new HazelcastClusterInformation(hazelcast);
    }

    @Nonnull
    @Override
    public IClusterInformation getInformation() {
        return clusterInformation;
    }

    @Nonnull
    @Override
    public UUID getNodeId() {
        return clusterInformation.getLocalNode().getId();
    }

    @Override
    public boolean isAvailable() {
        return hazelcast.getLifecycleService().isRunning();
    }

    @Override
    public boolean isClustered() {
        return isAvailable() && hazelcast.getCluster().getMembers().size() > 1;
    }
}
