package com.pmi.tpd.core.event.permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * An event which is fired when a user or group is granted a permission within.
 *
 * @see GlobalPermissionGrantedEvent
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IPermissionGrantedEvent {

    /**
     * @return the affected group of {@code null} if a user was affected
     */
    @Nullable
    String getAffectedGroup();

    /**
     * @return the affected user of {@code null} if a group was affected
     */
    @Nullable
    IUser getAffectedUser();

    /**
     * @return the permission granted
     */
    @Nonnull
    Permission getPermission();
}
