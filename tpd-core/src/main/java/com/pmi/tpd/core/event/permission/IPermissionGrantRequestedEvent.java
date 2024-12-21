package com.pmi.tpd.core.event.permission;

import com.pmi.tpd.api.event.ICancelableEvent;

/**
 * This event is raised before a user or group are granted a permission. This event is synchronous, allowing listeners
 * to perform operations in the same database transaction where the permission is granted.
 * <p>
 * This event is {@link ICancelableEvent cancelable}. A listener may prevent the permission from being granted by
 * {@link #cancel(com.pmi.tpd.api.i18n.KeyedMessage) canceling} this event. Throwing an exception <i>will not</i>
 * prevent the permission from being granted; the exception will be logged and ignored.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IPermissionGrantRequestedEvent extends IPermissionRequestedEvent, ICancelableEvent {
}
