package com.pmi.tpd.core.event.permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * An event which is fired when a user or group has their permission modified.
 *
 * @see GlobalPermissionModifiedEvent
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IPermissionModifiedEvent {

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
     * @return the new permission which replaced the previous value
     */
    @Nonnull
    Permission getNewValue();

    /**
     * @return the old permission which was replaced
     */
    @Nonnull
    Permission getOldValue();

}
