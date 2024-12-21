package com.pmi.tpd.core.event.permission;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.ICancelableEvent;
import com.pmi.tpd.security.permission.Permission;

/**
 * This event is raised before a user or group's permission is modified. This event is synchronous, allowing listeners
 * to perform operations in the same database transaction where the permission is modified.
 * <p>
 * This event is {@link ICancelableEvent cancelable}. A listener may prevent the permission from being modified by
 * {@link #cancel(com.pmi.tpd.api.i18n.KeyedMessage) canceling} this event. Throwing an exception <i>will not</i>
 * prevent the permission from being modified; the exception will be logged and ignored.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IPermissionModificationRequestedEvent extends IPermissionRequestedEvent, ICancelableEvent {

    /**
     * @return the new permission replacing the previous value
     */
    @Nonnull
    Permission getNewValue();

    /**
     * @return the old permission being replaced
     */
    @Nonnull
    Permission getOldValue();
}
