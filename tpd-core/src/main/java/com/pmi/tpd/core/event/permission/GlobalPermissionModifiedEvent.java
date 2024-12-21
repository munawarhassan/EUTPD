package com.pmi.tpd.core.event.permission;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * This event is fired when a user or group has their global permission modified.
 * <p>
 * This event is internally audited with a HIGH priority.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@AsynchronousPreferred
public class GlobalPermissionModifiedEvent extends AbstractGlobalPermissionModificationEvent
        implements IPermissionModifiedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 8812564047161985360L;

    /**
     * @param source
     * @param oldValue
     * @param newValue
     * @param affectedGroup
     * @param affectedUser
     */
    public GlobalPermissionModifiedEvent(final Object source, final Permission oldValue, final Permission newValue,
            final String affectedGroup, final IUser affectedUser) {
        super(source, oldValue, newValue, affectedGroup, affectedUser);
    }

}
