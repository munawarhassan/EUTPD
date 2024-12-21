package com.pmi.tpd.core.event.permission;

import com.pmi.tpd.api.event.ICancelableEvent;
import com.pmi.tpd.security.permission.Permission;

/**
 * This event is raised before a user or group's permission is revoked. This event is synchronous, allowing listeners to
 * perform operations in the same database transaction where the permission is revoked.
 * <p>
 * This event is {@link ICancelableEvent cancelable}. A listener may prevent the permission from being revoked by
 * {@link #cancel(com.pmi.tpd.api.i18n.KeyedMessage) canceling} this event. Throwing an exception <i>will not</i>
 * prevent the permission from being revoked; the exception will be logged and ignored.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IPermissionRevocationRequestedEvent extends IPermissionRequestedEvent, ICancelableEvent {

    Permission getPermission();

}
