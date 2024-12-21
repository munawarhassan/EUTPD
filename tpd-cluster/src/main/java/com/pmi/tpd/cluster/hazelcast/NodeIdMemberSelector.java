package com.pmi.tpd.cluster.hazelcast;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MemberSelector;

/**
 * Matches one or more cluster member by ID.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class NodeIdMemberSelector implements MemberSelector {

    /** */
    private final Set<UUID> nodeIds;

    /**
     * @param nodeId
     */
    public NodeIdMemberSelector(final UUID nodeId) {
        this(Collections.singleton(nodeId));
    }

    /**
     * @param nodeIds
     */
    public NodeIdMemberSelector(final Iterable<UUID> nodeIds) {
        this.nodeIds = ImmutableSet.copyOf(nodeIds);
    }

    @Override
    public boolean select(final Member member) {
        return nodeIds.contains(member.getUuid());
    }
}
