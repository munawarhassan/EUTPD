package com.pmi.tpd.cluster.concurrent;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * Tracks a version against a key. If application is clustered then this can be
 * used to help track changes to entities
 * across the cluster.
 *
 * @param <K>
 *            the type of the key
 */
public interface IVersionTracker<K extends Serializable> {

  /**
   * @return retrieves the current version for the supplied key
   */
  int get(@Nonnull K key);

  /**
   * Increments the version for the supplied key.
   */
  void increment(@Nonnull K key);

  /**
   * @return the incremented version for the supplied key
   */
  int incrementAndGet(@Nonnull K key);

  /**
   * Increments all versions for all known keys. When application is clustered
   * this is potentially an expensive
   * operation.
   */
  void incrementAll();
}
