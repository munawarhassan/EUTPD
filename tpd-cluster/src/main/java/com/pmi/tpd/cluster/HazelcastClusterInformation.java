package com.pmi.tpd.cluster;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.pmi.tpd.cluster.HazelcastClusterNode.sort;
import static com.pmi.tpd.cluster.HazelcastClusterNode.transform;

import java.util.Set;

import javax.annotation.Nonnull;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastClusterInformation implements IClusterInformation {

    /** */
    private final Cluster cluster;

    /** */
    private final HazelcastInstance hazelcast;

    /** */
    public HazelcastClusterInformation(final HazelcastInstance hazelcast) {
        this.hazelcast = checkNotNull(hazelcast, "hazelcast");

        cluster = checkNotNull(hazelcast.getCluster(), "hazelcast.cluster");
    }

    @Nonnull
    @Override
    public IClusterNode getLocalNode() {
        return new HazelcastClusterNode(cluster.getLocalMember());
    }

    @Nonnull
    @Override
    public Set<IClusterNode> getNodes() {
        return sort(transform(cluster.getMembers()));
    }

    @Override
    public boolean isRunning() {
        return hazelcast.getLifecycleService().isRunning();
    }
}
