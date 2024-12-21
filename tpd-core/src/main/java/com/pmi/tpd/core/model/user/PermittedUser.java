package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.security.permission.IPermittedUser;

/**
 * Associates a {@link com.pmi.tpd.api.security.permission.Permission permission} with a {@link IUser user}.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class PermittedUser extends PermittedEntity implements IPermittedUser {

    /** */
    private final IUser user;

    /**
     * @param user
     * @param permissionWeight
     */
    public PermittedUser(final IUser user, final int permissionWeight) {
        super(permissionWeight);

        this.user = Assert.checkNotNull(user, "user");
    }

    @Nonnull
    @Override
    public IUser getUser() {
        return user;
    }
}
