package com.pmi.tpd.core.event.permission;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * This event is fired when a user or group are granted a global permission.
 * <p>
 * This event is internally audited with a HIGH priority.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@AsynchronousPreferred
public class GlobalPermissionGrantedEvent extends GlobalPermissionEvent implements IPermissionGrantedEvent {

    /**
     *
     */
    private static final long serialVersionUID = 5056345442131305506L;

    /**
     * @param source
     * @param permission
     * @param affectedGroup
     * @param affectedUser
     */
    public GlobalPermissionGrantedEvent(final Object source, final Permission permission, final String affectedGroup,
            final IUser affectedUser) {
        super(source, permission, affectedGroup, affectedUser);
    }
}
