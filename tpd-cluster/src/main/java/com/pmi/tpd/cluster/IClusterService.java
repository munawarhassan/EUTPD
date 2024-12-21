package com.pmi.tpd.cluster;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Provides access to cluster details for the instance.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterService {

    /**
     * @return details about the cluster and its {@link IClusterNode nodes}
     */
    @Nonnull
    IClusterInformation getInformation();

    /**
     * Convenience method for obtaining the ID for the local node. Calling the method is equivalent to calling
     * {@code getInformation().getLocalNode().getId()}
     *
     * @return the ID of the local node
     */
    @Nonnull
    UUID getNodeId();

    /**
     * @return {@code true} if clustering is available; otherwise, {@code false}
     */
    boolean isAvailable();

    /**
     * @return {@code true} if clustering is {@link #isAvailable() available} <i>and</i> at least two nodes have joined
     *         the cluster
     */
    boolean isClustered();
}
