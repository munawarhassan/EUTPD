package com.pmi.tpd.cluster.event;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.cluster.IClusterNode;

/**
 * Specialization of {@link ClusterNodeAddedEvent} that is raised when a node
 * reconnects to the current node. This
 * happens when a network partition is resolved, for instance due to
 * intermittent networking issues.
 * <p>
 * Note: when a network partition in a larger cluster occurs, the node may
 * receive multiple
 * {@code ClusterNodeRejoinedEvent}s when the partitions merge - one for each
 * node-to-node connection that is
 * reestablished.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class ClusterNodeRejoinedEvent extends ClusterNodeAddedEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final long timestampReconnected;

  /** */
  private final long timestampDisconnected;

  public ClusterNodeRejoinedEvent(@Nonnull final Object source, @Nonnull final IClusterNode node,
      @Nonnull final Set<IClusterNode> currentNodes, final long timestampDisconnected,
      final long timestampReconnected) {
    super(source, node, currentNodes);

    this.timestampReconnected = timestampReconnected;
    this.timestampDisconnected = timestampDisconnected;
  }

  /**
   * @return the date the node was disconnected from the current node
   */
  @Nonnull
  public Date getDateDisconnected() {
    return new Date(timestampDisconnected);
  }

  /**
   * @return the date the node reconnected to the current node
   */
  @Nonnull
  public Date getDateReconnected() {
    return new Date(timestampReconnected);
  }
}
