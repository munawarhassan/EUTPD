package com.pmi.tpd.cluster.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.cluster.IClusterNode;

/**
 * A base class for events related to cluster membership, raised when nodes
 * {@link ClusterNodeAddedEvent join} and
 * {@link ClusterNodeRemovedEvent leave} the cluster.
 * <p>
 * This class exists primarily to simplify writing event types. Plugins
 * generally should not listen for this base class;
 * they should listen for specific subclasses.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class ClusterMembershipEvent extends ClusterNodeEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final Set<IClusterNode> currentNodes;

  ClusterMembershipEvent(@Nonnull final Object source, @Nonnull final IClusterNode node,
      @Nonnull final Set<IClusterNode> currentNodes) {
    super(source, node);

    this.currentNodes = ImmutableSet.copyOf(checkNotNull(currentNodes, "currentNodes"));
  }

  /**
   * Retrieves the <i>current</i> set of {@link IClusterNode cluster nodes} at the
   * time the event was raised. If a new
   * node has joined, it will be in the returned set. If an existing node has
   * departed, it will not be.
   *
   * @return the <i>current</i> cluster nodes
   */
  @Nonnull
  public Set<IClusterNode> getCurrentNodes() {
    return currentNodes;
  }
}
