package com.pmi.tpd.cluster;

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Describes a cluster, providing details about each node.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterInformation {

  /**
   * @return information about the local node
   */
  @Nonnull
  IClusterNode getLocalNode();

  /**
   * @return a set describing all of the nodes in the cluster, which will always
   *         contain at least one node
   */
  @Nonnull
  Set<IClusterNode> getNodes();

  /**
   * @return {@code true} if the clustering services are running, indicating nodes
   *         can join the cluster; otherwise,
   *         {@code false} if clustering services are not running
   */
  boolean isRunning();
}
