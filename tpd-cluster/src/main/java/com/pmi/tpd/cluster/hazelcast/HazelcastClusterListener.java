package com.pmi.tpd.cluster.hazelcast;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.cluster.HazelcastClusterNode;
import com.pmi.tpd.cluster.HazelcastClusterService;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.cluster.event.ClusterNodeRejoinedEvent;
import com.pmi.tpd.cluster.event.ClusterNodeRemovedEvent;

/**
 * Registers a {@code MembershipListener} with the Hazelcast cluster, propagating add and remove events. The listener is
 * registered immediately after construction and is unregistered before the Spring context shuts down.
 * <p>
 * This is not part of {@link HazelcastClusterService HazelcastClusterService} to keep the orthogonal concerns of
 * retrieving cluster details and translating/propagating events separate.
 * <p>
 * Note that this MembershipListener keeps track of which nodes it has already seen. If a node connects that has
 * previously connected, a {@link ClusterNodeRejoinedEvent} is raised instead of a {@link ClusterNodeAddedEvent}. A
 * custom {@link Member#getStringAttribute(String) attribute} is uniquely identify nodes. The standard member
 * {@link Member#getUuid() UUID} is not suitable because it is reset when two clusters merge after a split brain.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastClusterListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastClusterListener.class);

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final HazelcastInstance hazelcast;

    /** */
    private volatile UUID listenerId;

    @Inject
    public HazelcastClusterListener(final IEventPublisher eventPublisher, final HazelcastInstance hazelcast) {
        this.eventPublisher = eventPublisher;
        this.hazelcast = hazelcast;
    }

    @PostConstruct
    public void register() {
        listenerId = hazelcast.getCluster().addMembershipListener(new MembershipListener() {

            private final ConcurrentMap<String, Long> lastModifiedByMember = new ConcurrentHashMap<>();

            @Override
            public void memberAdded(final MembershipEvent membershipEvent) {
                final long now = System.currentTimeMillis();
                final Member member = membershipEvent.getMember();
                final String nodeId = member.getAttribute(HazelcastConstants.ATT_NODE_VM_ID);
                Long lastChange = null;
                if (nodeId != null) {
                    lastChange = lastModifiedByMember.put(nodeId, now);
                } else {
                    LOGGER.warn("No '{}' attribute available for new member {}",
                        HazelcastConstants.ATT_NODE_VM_ID,
                        member.getSocketAddress(EndpointQualifier.MEMBER));
                }
                if (lastChange == null) {
                    // First time the new member is seen
                    eventPublisher.publish(
                        new ClusterNodeAddedEvent(HazelcastClusterListener.this, new HazelcastClusterNode(member),
                                HazelcastClusterNode.transform(membershipEvent.getMembers())));
                } else {
                    // We've seen this member before. This has to be a split brain that is resolved. A restart of a node
                    // would result in a different UUID.
                    eventPublisher.publish(
                        new ClusterNodeRejoinedEvent(HazelcastClusterListener.this, new HazelcastClusterNode(member),
                                HazelcastClusterNode.transform(membershipEvent.getMembers()), lastChange, now));
                }
            }

            @Override
            public void memberRemoved(final MembershipEvent membershipEvent) {
                final Member member = membershipEvent.getMember();
                eventPublisher.publish(
                    new ClusterNodeRemovedEvent(HazelcastClusterListener.this, new HazelcastClusterNode(member),
                            HazelcastClusterNode.transform(membershipEvent.getMembers())));

                final String nodeId = member.getAttribute(HazelcastConstants.ATT_NODE_VM_ID);
                if (nodeId != null) {
                    lastModifiedByMember.put(nodeId, System.currentTimeMillis());
                }
            }

        });
    }

    @PreDestroy
    public boolean unregister() { // Returns a boolean to allow for better verification during testing
        final UUID id = listenerId; // Because paranoia...

        return id != null && hazelcast.getCluster().removeMembershipListener(id);
    }
}
