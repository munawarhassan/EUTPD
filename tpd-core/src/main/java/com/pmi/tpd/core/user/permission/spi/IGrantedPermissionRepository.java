package com.pmi.tpd.core.user.permission.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.database.jpa.IDslAccessor;

/**
 * A repository for managing {@link GrantedPermission granted permission}.
 * 
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 * @param <E>
 */
public interface IGrantedPermissionRepository<E extends GrantedPermission> extends IDslAccessor<E, Long> {

    /**
     * Checks for existence of the given permission.
     *
     * @param grantedPermission
     *            the permission to check.
     * @return true if the permission is currently granted, explicitly.
     */
    boolean hasPermissionEntry(@Nonnull E grantedPermission);

    /**
     * Revokes all the permissions of type {@code <E>} explicitly associated with an user.
     *
     * @param userId
     *            id identifying the user
     * @return count of the number of rows deleted
     */
    long revokeAll(Long userId);

    /**
     * Revokes all the permissions of type {@code <E>} explicitly associated with a group.
     *
     * @param groupName
     *            name of the group
     * @return count of the number of rows deleted
     */
    long revokeAll(@Nonnull String groupName);

    /**
     * Revokes the matching explicit permission(s) of type {@code <E>}.
     *
     * @param grantedPermission
     *            the granted permission
     * @return count of the number of rows deleted
     */
    long revoke(@Nonnull E grantedPermission);

}
