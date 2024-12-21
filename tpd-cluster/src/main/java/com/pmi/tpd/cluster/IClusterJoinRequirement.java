package com.pmi.tpd.cluster;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A requirement of a node joining a application cluster.
 *
 * @param <T>
 *            the type of the requirement
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterJoinRequirement<T extends Serializable> {

  /**
   * @return a unique name for the requirement
   */
  @Nonnull
  String getName();

  /**
   * @return the value for the current node which will be compared to a remote
   *         node
   */
  @Nullable
  T getValue();
}
