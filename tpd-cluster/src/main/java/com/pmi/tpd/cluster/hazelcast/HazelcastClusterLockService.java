package com.pmi.tpd.cluster.hazelcast;

import javax.annotation.Nonnull;

import com.hazelcast.core.HazelcastInstance;
import com.pmi.tpd.cluster.concurrent.IClusterLock;
import com.pmi.tpd.cluster.concurrent.IClusterLockService;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastClusterLockService implements IClusterLockService {

  /** */
  private static final String LOCK_MAP_NAME = "app.cluster.locks";

  /** */
  private final HazelcastMappedLock mappedLock;

  public HazelcastClusterLockService(final HazelcastInstance hazelcast) {
    this.mappedLock = new HazelcastMappedLock(hazelcast.<String, String>getMap(LOCK_MAP_NAME));
  }

  @Override
  public IClusterLock getLockForName(@Nonnull final String lockName) {
    return new HazelcastClusterLock(this.mappedLock, lockName);
  }
}
