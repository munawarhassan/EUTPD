package com.pmi.tpd.core.user.permission;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;

/**
 * Criteria to determine whether a user has a effective permission.
 *
 * @see EffectivePermissionDao
 */
public final class UserPermissionCriteria extends PermissionCriteria {

    /** */
    private final long userId;

    private UserPermissionCriteria(final long userId, final Permission permission) {
        super(permission);
        this.userId = userId;
    }

    /**
     * Gets the user associated with this criteria.
     *
     * @return the user id
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder extends AbstractBuilder<Builder> {

        /** */
        private final long userId;

        /**
         * @param userId
         */
        public Builder(final long userId) {
            this.userId = userId;
        }

        /**
         * @param user
         */
        public Builder(final IUser user) {
            this.userId = user.getId();
        }

        /**
         * @return
         */
        public UserPermissionCriteria build() {
            return new UserPermissionCriteria(userId, permission);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Builder self() {
            return this;
        }

    }

}
