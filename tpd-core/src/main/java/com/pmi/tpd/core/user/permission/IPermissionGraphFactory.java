package com.pmi.tpd.core.user.permission;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.model.user.IIterablePermissionGraph;

/**
 * Factory for {@link PermissionGraph permission graphs}.
 * 
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IPermissionGraphFactory {

    /**
     * Creates a {@link PermissionGraph permission graph} for the provided user.
     *
     * @param user
     *            the user
     * @return the permission graph for the provided user
     */
    @Nonnull
    IIterablePermissionGraph createGraph(@Nonnull IUser user);
}
