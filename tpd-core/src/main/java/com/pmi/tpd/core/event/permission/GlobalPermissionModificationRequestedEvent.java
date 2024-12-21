package com.pmi.tpd.core.event.permission;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.annotation.TransactionAware;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * This event is raised before a user or group's global permission is modified. This event is synchronous, allowing
 * listeners to perform operations in the same database transaction where the permission is modified.
 * <p>
 * This event is {@link com.pmi.tpd.api.event.ICancelableEvent cancelable}. A listener may prevent the permission from
 * being modified by {@link #cancel(KeyedMessage) canceling} this event. Throwing an exception <i>will not</i> prevent
 * the permission from being modified; the exception will be logged and ignored.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@TransactionAware(value = TransactionAware.When.IMMEDIATE) // Override PermissionEvent
public class GlobalPermissionModificationRequestedEvent extends AbstractGlobalPermissionModificationEvent
        implements IPermissionModificationRequestedEvent {

    /**
     *
     */
    private static final long serialVersionUID = -9033739856516788993L;

    /** */
    private final ICancelState cancelState;

    /**
     * @param source
     * @param oldValue
     * @param newValue
     * @param affectedGroup
     * @param affectedUser
     * @param cancelState
     */
    public GlobalPermissionModificationRequestedEvent(@Nonnull final Object source, @Nonnull final Permission oldValue,
            @Nonnull final Permission newValue, @Nullable final String affectedGroup,
            @Nullable final IUser affectedUser, @Nonnull final ICancelState cancelState) {
        super(source, oldValue, newValue, affectedGroup, affectedUser);

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
