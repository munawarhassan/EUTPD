package com.pmi.tpd.cluster;

/**
 * The action to take based on the executed {@link IClusterJoinCheck}.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public enum ClusterJoinCheckAction {
    /**
     * The check passed, allow the nodes to connect.
     */
    CONNECT(0, false),
    /**
     * The check failed but no node needs to be passivated.
     */
    DISCONNECT(1, false),
    /**
     * The check failed and one node needs to be passivated but based on the join check it's unclear which node needs
     * to. be shutdown
     */
    PASSIVATE_ANY_NODE(2, true),
    /**
     * The check failed and the other node needs to be passivated.
     */
    PASSIVATE_OTHER_NODE(3, true),
    /**
     * The check failed and this node needs to be passivated.
     */
    PASSIVATE_THIS_NODE(4, true);

    /** */
    private final int id;

    /** */
    private final boolean passivate;

    ClusterJoinCheckAction(final int id, final boolean passivate) {
        this.id = id;
        this.passivate = passivate;
    }

    public int getId() {
        return id;
    }

    public boolean isPassivate() {
        return passivate;
    }

    public static ClusterJoinCheckAction forId(final int id) {
        for (final ClusterJoinCheckAction target : values()) {
            if (id == target.id) {
                return target;
            }
        }

        throw new IllegalArgumentException("Undefined id " + id);
    }
}
