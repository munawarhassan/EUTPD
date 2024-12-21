package com.pmi.tpd.cluster;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.springframework.core.Ordered;

/**
 * A check that is run when a node connects to another node, either forming or
 * joining a cluster.
 * {@code ClusterJoinCheck}s verify that the nodes meet the minimum requirement
 * for forming a cluster. The check is
 * executed on both sides. On the side that initiates the connection
 * {@link #connect(IClusterJoinRequest)} is called,
 * while {@link #accept(IClusterJoinRequest)} is called on the side accepting
 * the connection.
 * <p>
 * The check must pass on both sides before the connection can be made. Either
 * side can signal that the connection
 * should be refused, in which case the connection is not made. For serious
 * issues that could lead to data corruption, a
 * check can signal that one of the nodes should be passivated.
 */
public interface IClusterJoinCheck extends Ordered {

  /**
   * @return the name of the join check
   */
  @Nonnull
  String getName();

  /**
   * Called when another node tries to connect to this node.
   *
   * @param request
   *                the join request
   * @return the outcome of the check
   * @throws IOException
   *                     when a problem occurs reading from or writing to the
   *                     other node
   */
  @Nonnull
  ClusterJoinCheckResult accept(@Nonnull IClusterJoinRequest request) throws IOException;

  /**
   * Called when this node tries to connect to another node.
   *
   * @param request
   *                the join request
   * @return the outcome of the check
   * @throws IOException
   *                     when a problem occurs reading from or writing to the
   *                     other node
   */
  @Nonnull
  ClusterJoinCheckResult connect(@Nonnull IClusterJoinRequest request) throws IOException;

  /**
   * Called when the node this node is connecting to doesn't have this check
   *
   * @param request
   *                the join request
   * @return the outcome of the check
   */
  @Nonnull
  ClusterJoinCheckResult onUnknown(@Nonnull IClusterJoinRequest request);
}
