package com.pmi.tpd.cluster.event;

import java.util.Set;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.cluster.IClusterNode;

/**
 * Raised when an {@link #getRemovedNode() existing node} leaves the cluster.
 * <p>
 * Due to the non-deterministic nature of event processing, it is possible the
 * other nodes may have joined or left the
 * cluster. As a result, using the
 * {@link com.pmi.tpd.cluster.IClusterInformation#getNodes() ClusterInformation}
 * to
 * determine the cluster nodes may be inconsistent. {@link #getCurrentNodes()}
 * is provided to simplify deterministic
 * processing in listeners.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@AsynchronousPreferred
public class ClusterNodeRemovedEvent extends ClusterMembershipEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param source
   * @param node
   * @param currentNodes
   */
  public ClusterNodeRemovedEvent(@Nonnull final Object source, @Nonnull final IClusterNode node,
      @Nonnull final Set<IClusterNode> currentNodes) {
    super(source, node, currentNodes);
  }

  /**
   * @return the removed node, which <i>will not</i> be in the
   *         {@link #getCurrentNodes() current nodes} set
   */
  @Nonnull
  public IClusterNode getRemovedNode() {
    return getNode();
  }
}
