package com.pmi.tpd.cluster.hazelcast;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import javax.annotation.Nonnull;

import com.pmi.tpd.cluster.concurrent.IClusterLock;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
class HazelcastClusterLock implements IClusterLock {

  /** */
  private final HazelcastMappedLock lockMap;

  /** */
  private final String lockName;

  public HazelcastClusterLock(final HazelcastMappedLock lockMap, final String lockName) {
    if (lockName == null || lockName.trim().isEmpty()) {
      throw new IllegalArgumentException("lockName cannot be empty: " + lockName);
    }

    this.lockMap = lockMap;
    this.lockName = lockName;
  }

  @Override
  public boolean isHeldByCurrentThread() {
    return this.lockMap.isHeldByCurrentThread(this.lockName);
  }

  @Override
  public void lock() {
    this.lockMap.lock(this.lockName);
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    this.lockMap.lockInterruptibly(this.lockName);
  }

  @Override
  public boolean tryLock() {
    return this.lockMap.tryLock(this.lockName);
  }

  @Override
  public boolean tryLock(final long time, @Nonnull final TimeUnit unit) throws InterruptedException {
    return this.lockMap.tryLock(this.lockName, time, unit);
  }

  @Override
  public void unlock() {
    this.lockMap.unlock(this.lockName);
  }

  @Override
  @Nonnull
  public Condition newCondition() {
    throw new UnsupportedOperationException("newCondition() not supported in ClusterLock");
  }
}
