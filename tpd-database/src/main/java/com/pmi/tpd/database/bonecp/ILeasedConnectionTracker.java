package com.pmi.tpd.database.bonecp;

import java.util.Set;

import javax.annotation.Nonnull;

import com.jolbox.bonecp.ConnectionHandle;

/**
 * Tracks the current set of leased {@link ConnectionHandle connections}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILeasedConnectionTracker {

  @Nonnull
  Set<ConnectionHandle> getLeased();
}
