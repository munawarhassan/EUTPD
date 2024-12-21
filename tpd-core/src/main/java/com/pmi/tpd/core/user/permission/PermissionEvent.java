package com.pmi.tpd.core.user.permission;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.event.annotation.TransactionAware;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * Base event signalling permission changes.
 */
@TransactionAware
public abstract class PermissionEvent extends BaseEvent {

    /** */
    private static final long serialVersionUID = -2695582053517755311L;

    /** */
    private final Permission permission;

    /** */
    private final String affectedGroup;

    /** */
    private final IUser affectedUser;

    /**
     * @param source
     * @param permission
     * @param affectedGroup
     * @param affectedUser
     */
    protected PermissionEvent(final Object source, final Permission permission, final String affectedGroup,
            final IUser affectedUser) {
        super(source);
        checkArgument(checkNotNull(permission, "permission").isGrantable(), "Permission must be grantable");
        this.permission = permission;
        this.affectedGroup = affectedGroup;
        this.affectedUser = affectedUser;
    }

    /**
     * @return
     */
    @Nonnull
    public Permission getPermission() {
        return permission;
    }

    /**
     * @return
     */
    @Nullable
    public String getAffectedGroup() {
        return affectedGroup;
    }

    /**
     * @return
     */
    @Nullable
    public IUser getAffectedUser() {
        return affectedUser;
    }
}
