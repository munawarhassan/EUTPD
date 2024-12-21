package com.pmi.tpd.cluster;

/**
 * The current node was passivated during startup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class NodePassivationException extends IllegalStateException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public NodePassivationException() {
        super("The current node has been passivated");
    }
}
