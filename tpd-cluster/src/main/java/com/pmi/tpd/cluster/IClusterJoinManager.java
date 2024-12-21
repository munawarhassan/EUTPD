package com.pmi.tpd.cluster;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Manages the initial establishment of connections between nodes in the
 * cluster.
 *
 * @see com.pmi.tpd.cluster.hazelcast.ClusterJoinSocketInterceptor
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterJoinManager {

  /**
   * Validate a joining node for the cluster the current node is a part of.
   *
   * @param request
   *                the join request
   * @throws IOException
   *                     if the join negotiation failed, or a communication
   *                     breakdown occurs
   */
  void accept(@Nonnull IClusterJoinRequest request) throws IOException;

  /**
   * Attempt to join an existing cluster.
   *
   * @param request
   *                the join request
   * @throws IOException
   *                     if the join negotiation failed, or a communication
   *                     breakdown occurs
   */
  void connect(@Nonnull IClusterJoinRequest request) throws IOException;
}
