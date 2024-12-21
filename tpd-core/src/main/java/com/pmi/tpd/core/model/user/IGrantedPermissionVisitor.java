package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;

/**
 * Implements the visitor pattern for {@link GrantedPermission granted permissions}.
 *
 * @since 2.0
 */
public interface IGrantedPermissionVisitor {

    /**
     * Visit a granted permission.
     * 
     * @param permission
     *            granted permission to use.
     */
    void visit(@Nonnull GrantedPermission permission);

}
