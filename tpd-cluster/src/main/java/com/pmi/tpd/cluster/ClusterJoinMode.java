package com.pmi.tpd.cluster;

/**
 * Describes whether a node is {@link #CONNECT initiating } the connection to another node or {@link #ACCEPT accepting}
 * a connection from another node.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public enum ClusterJoinMode {
    /** */
    ACCEPT,
    /** */
    CONNECT
}
