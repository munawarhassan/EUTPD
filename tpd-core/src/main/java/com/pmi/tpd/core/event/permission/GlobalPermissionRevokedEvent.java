package com.pmi.tpd.core.event.permission;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * This event is fired when a user or group has their global permission revoked.
 * <p>
 * This event is internally audited with a HIGH priority.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@AsynchronousPreferred
public class GlobalPermissionRevokedEvent extends GlobalPermissionEvent implements IPermissionRevokedEvent {

    /**
     *
     */
    private static final long serialVersionUID = -726588273171206521L;

    /**
     * @param source
     * @param permission
     * @param affectedGroup
     * @param affectedUser
     */
    public GlobalPermissionRevokedEvent(final Object source, final Permission permission, final String affectedGroup,
            final IUser affectedUser) {
        super(source, permission, affectedGroup, affectedUser);
    }
}
