package com.pmi.tpd.core.user.permission;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.Permission;

/**
 * PermissionGraph that composes two other graphs. The composite graph grants a permission iff one or both of the
 * wrapped graphs grants the permission.
 */
public class CompositePermissionGraph implements IPermissionGraph {

    /** */
    private static final IPermissionGraph EMPTY = (final Permission permission,
        @Nullable final Object resource) -> false;

    /** */
    private final IPermissionGraph graph1;

    /** */
    private final IPermissionGraph graph2;

    /**
     * @param graph1
     * @param graph2
     */
    public CompositePermissionGraph(@Nonnull final IPermissionGraph graph1, @Nonnull final IPermissionGraph graph2) {
        this.graph1 = checkNotNull(graph1, "graph1");
        this.graph2 = checkNotNull(graph2, "graph2");
    }

    @Nonnull
    public static IPermissionGraph maybeCompose(@Nullable final IPermissionGraph graph1,
        @Nullable final IPermissionGraph graph2) {
        if (graph1 == null && graph2 == null) {
            return EMPTY;
        }
        if (graph1 == null) {
            return graph2;
        }
        if (graph2 == null) {
            return graph1;
        }
        return new CompositePermissionGraph(graph1, graph2);
    }

    @Override
    public boolean isGranted(@Nonnull final Permission permission, @Nullable final Object resource) {
        return graph1.isGranted(permission, resource) || graph2.isGranted(permission, resource);
    }

    @Override
    public String toString() {
        return "{CompositePermissionGraph graph1:" + graph1 + ", graph2:" + graph2 + "}";
    }
}
