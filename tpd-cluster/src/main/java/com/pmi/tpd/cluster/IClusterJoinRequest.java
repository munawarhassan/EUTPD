package com.pmi.tpd.cluster;

import javax.annotation.Nonnull;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

/**
 * A request for a cluster join check.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterJoinRequest {

  /**
   * @return the Hazelcast instance
   */
  @Nonnull
  HazelcastInstance getHazelcast();

  /**
   * @return {@link ClusterJoinMode#CONNECT} if this node initiated the connection
   *         to another node or
   *         {@link ClusterJoinMode#ACCEPT} if the other node initiated the
   *         connection
   */
  @Nonnull
  ClusterJoinMode getJoinMode();

  /**
   * @return input to be read from the remote node
   */
  @Nonnull
  ObjectDataInput in();

  /**
   * @return output to be written to the remote node
   */
  @Nonnull
  ObjectDataOutput out();
}
