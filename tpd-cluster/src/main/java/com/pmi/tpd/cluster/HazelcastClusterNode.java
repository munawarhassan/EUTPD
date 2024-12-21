package com.pmi.tpd.cluster;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.hazelcast.instance.EndpointQualifier.MEMBER;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.hazelcast.cluster.Member;
import com.pmi.tpd.cluster.hazelcast.HazelcastConstants;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
// @CustomSoyDataMapper("ClusterNode")
public class HazelcastClusterNode implements IClusterNode {

    /** */
    private static final Comparator<? super IClusterNode> ORDERING = (left, right) -> left.getId()
            .compareTo(right.getId());

    /** */
    public static final Function<HazelcastClusterNode, Member> TO_MEMBER = HazelcastClusterNode::getMember;

    /** */
    private static final Function<Member, IClusterNode> TRANSFORM = HazelcastClusterNode::new;

    /** */
    private final Member member;

    /** */
    private final String name;

    /**
     * @param member
     */
    public HazelcastClusterNode(final Member member) {
        this.member = checkNotNull(member, "member");
        this.name = member.getAttribute(HazelcastConstants.ATT_NODE_NAME);
    }

    /**
     * @param member
     * @return
     */
    @Nonnull
    public static IClusterNode transform(@Nonnull final Member member) {
        return TRANSFORM.apply(member);
    }

    /**
     * @param members
     * @return
     */
    @Nonnull
    public static Set<IClusterNode> transform(@Nonnull final Set<Member> members) {
        return from(members).transform(TRANSFORM).toSet();
    }

    /**
     * @param members
     * @return
     */
    @Nonnull
    public static Set<IClusterNode> sort(@Nonnull final Set<IClusterNode> members) {
        return from(members).sort(ORDERING).toSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof HazelcastClusterNode) {
            final HazelcastClusterNode n = (HazelcastClusterNode) o;

            return member.equals(n.member);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public InetSocketAddress getAddress() {
        return member.getSocketAddress(MEMBER);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public UUID getId() {
        return member.getUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    @Nonnull
    public Member getMember() {
        return member;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return member.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocal() {
        return member.localMember();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getId() + " listening on " + getAddress();
    }
}
