package com.pmi.tpd.core.event.permission;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.annotation.TransactionAware;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * This event is raised before a user or group's global permission is revoked. This event is synchronous, allowing
 * listeners to perform operations in the same database transaction where the permission is revoked.
 * <p>
 * This event is {@link com.pmi.tpd.api.event.ICancelableEvent cancelable}. A listener may prevent the permission from
 * being revoked by {@link #cancel(KeyedMessage) canceling} this event. Throwing an exception <i>will not</i> prevent
 * the permission from being revoked; the exception will be logged and ignored.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@TransactionAware(value = TransactionAware.When.IMMEDIATE) // Override PermissionEvent
public class GlobalPermissionRevocationRequestedEvent extends GlobalPermissionEvent
        implements IPermissionRevocationRequestedEvent {

    /**
     *
     */
    private static final long serialVersionUID = -5178923013092028277L;

    /** */
    private final ICancelState cancelState;

    /**
     * @param source
     * @param permission
     * @param affectedGroup
     * @param affectedUser
     * @param cancelState
     */
    public GlobalPermissionRevocationRequestedEvent(@Nonnull final Object source, @Nonnull final Permission permission,
            @Nullable final String affectedGroup, @Nullable final IUser affectedUser,
            @Nonnull final ICancelState cancelState) {
        super(source, permission, affectedGroup, affectedUser);
        this.cancelState = checkNotNull(cancelState, "cancelState");
    }

    @Override
    public void cancel(@Nonnull final KeyedMessage message) {
        cancelState.cancel(message);
    }

    @Override
    public boolean isCanceled() {
        return cancelState.isCanceled();
    }
}
