package com.pmi.tpd.cluster.latch;

/**
 * Used to determine if we want cluster wide or local latch.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum LatchMode {
    /** */
    CLUSTER,
    /** */
    LOCAL
}
