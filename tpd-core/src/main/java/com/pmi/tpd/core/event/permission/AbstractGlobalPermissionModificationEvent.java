package com.pmi.tpd.core.event.permission;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * @see GlobalPermissionModificationRequestedEvent
 * @see GlobalPermissionModifiedEvent
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public abstract class AbstractGlobalPermissionModificationEvent extends GlobalPermissionEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final Permission oldValue;

    /**
     * @param source
     * @param oldValue
     * @param newValue
     * @param affectedGroup
     * @param affectedUser
     */
    protected AbstractGlobalPermissionModificationEvent(final Object source, final Permission oldValue,
            final Permission newValue, final String affectedGroup, final IUser affectedUser) {
        super(source, newValue, affectedGroup, affectedUser);

        this.oldValue = checkNotNull(oldValue, "oldValue");
        state(oldValue.isGlobal() && oldValue.isGrantable(), "oldValue must be a grantable global permission");
    }

    /**
     * @return
     */
    @Nonnull
    public Permission getNewValue() {
        return getPermission();
    }

    /**
     * @return
     */
    @Nonnull
    public Permission getOldValue() {
        return oldValue;
    }

}
