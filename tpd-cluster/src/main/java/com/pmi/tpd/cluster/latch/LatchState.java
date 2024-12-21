package com.pmi.tpd.cluster.latch;

/**
 * State of a latchable resource.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum LatchState {
    /** */
    AVAILABLE,
    /** */
    LATCHED,
    /** */
    DRAINED
}
