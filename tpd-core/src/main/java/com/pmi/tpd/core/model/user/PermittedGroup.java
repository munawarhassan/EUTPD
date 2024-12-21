package com.pmi.tpd.core.model.user;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.IPermittedGroup;

/**
 * Associates a {@link com.pmi.tpd.api.security.permission.Permission permission} with a group.
 */

public class PermittedGroup extends PermittedEntity implements IPermittedGroup {

    /** */
    private final String group;

    /**
     * @param group
     * @param permissionWeight
     */
    public PermittedGroup(final String group, final int permissionWeight) {
        super(permissionWeight);

        this.group = checkNotNull(group, "A group is required");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return PermittedGroup.class.getSimpleName() + "[" + group + ", " + getPermission() + "]";
    }
}
