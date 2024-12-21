package com.pmi.tpd.cluster;

import java.net.InetSocketAddress;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Describes a single node in a cluster.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClusterNode {

    /**
     * @return the complete socket address, comprised of hostname and port, for the node
     */
    @Nonnull
    InetSocketAddress getAddress();

    /**
     * Retrieves a cluster-wide unique identifier for this node.
     * <p>
     * The value is guaranteed to be unique within the cluster. If the cluster is restarted, every node's ID will still
     * be unique but will likely not be the same as the previous value.
     * </p>
     *
     * @return a cluster-wide unique identifier for this node
     */
    @Nonnull
    UUID getId();

    /**
     * @return {@code true} if this is the {@link IClusterInformation#getLocalNode() local node}; otherwise,
     *         {@code false} for remote nodes
     */
    boolean isLocal();

    /**
     * A long living cluster name that is defined by setting the system property {@code cluster.node.name}.
     *
     * @return The value from the system property named above otherwise an empty string if the property is not set.
     * @since 3.8
     */
    @Nonnull
    String getName();
}
