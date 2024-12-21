package com.pmi.tpd.cluster.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;

public interface IClusterLock extends Lock {

  boolean isHeldByCurrentThread();

  @Override
  @Nonnull
  Condition newCondition();
}
