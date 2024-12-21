package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.Permission;

/**
 * A base class to simplify constructing permitted entities which automatically translates from a weight, provided to
 * the constructor, to a {@link Permission}.
 */
abstract class PermittedEntity {

    /** */
    protected final Permission permission;

    protected PermittedEntity(final int permissionWeight) {
        this.permission = Permission.fromWeight(permissionWeight);
        if (this.permission == null) {
            throw new IllegalArgumentException("No permission has weight [" + permissionWeight
                    + "]. This is an indication the database and enumeration have mismatched weights.");
        }
    }

    @Nonnull
    public Permission getPermission() {
        return permission;
    }
}
