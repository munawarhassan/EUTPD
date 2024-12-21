package com.pmi.tpd.cluster;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class to resolve the name of a cluster node.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class ClusterNodeNameResolver {

    /** */
    static final String CLUSTER_NODE_NAME = "cluster.node.name";

    private ClusterNodeNameResolver() {
    }

    /**
     * Return the name of the node, currently only looks for system property <i>cluster.node.name</i>.
     *
     * @return the value of the system property or an empty string if the property has not been set.
     */
    public static String getNodeName() {
        return StringUtils.trimToEmpty(System.getProperty(CLUSTER_NODE_NAME));
    }
}
