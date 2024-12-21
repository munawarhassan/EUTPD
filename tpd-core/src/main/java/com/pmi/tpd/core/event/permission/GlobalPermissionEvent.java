package com.pmi.tpd.core.event.permission;

import static com.pmi.tpd.api.util.Assert.state;

import com.google.common.base.Strings;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.user.permission.PermissionEvent;

/**
 * Event signalling global permission changes.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public abstract class GlobalPermissionEvent extends PermissionEvent {

    /**
     *
     */
    private static final long serialVersionUID = -6450536076710117940L;

    /**
     * @param source
     * @param permission
     * @param affectedGroup
     * @param affectedUser
     */
    protected GlobalPermissionEvent(final Object source, final Permission permission, final String affectedGroup,
            final IUser affectedUser) {
        super(source, permission, affectedGroup, affectedUser);

        state(Strings.isNullOrEmpty(affectedGroup) ^ affectedUser == null,
            "Either a user or group can be affected, not both or neither. user=%s, group=%s",
            affectedUser,
            affectedGroup);
        state(permission.isGlobal(), "Require a global permission");
    }
}
