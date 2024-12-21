package com.pmi.tpd.cluster.concurrent;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterLockService {

  /**
   * @param paramString
   * @return
   */
  IClusterLock getLockForName(@Nonnull String paramString);
}
