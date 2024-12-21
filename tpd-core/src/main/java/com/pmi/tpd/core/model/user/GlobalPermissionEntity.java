package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.pmi.tpd.security.permission.Permission;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
@Entity(name = "GlobalPermission")
@Table(name = GlobalPermissionEntity.TABLE_NAME,
        indexes = { @Index(name = "idx_global_permission_user", columnList = "user_id"),
                @Index(name = "idx_global_permission_group", columnList = "group_name") })
public class GlobalPermissionEntity extends GrantedPermission {

    /** */
    public static final String TABLE_NAME = "t_global_permission";

    /**
     * Default Constructor.
     */
    // for hibernate
    protected GlobalPermissionEntity() {
        super();
    }

    private GlobalPermissionEntity(final Long id, final Permission permission, final String group,
            final UserEntity user) {
        super(id, permission, group, user);
    }

    @Override
    public void accept(@Nonnull final IGrantedPermissionVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Create new Builder.
     *
     * @return Returns new instance of {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create new Builder copy of instance.
     *
     * @return Returns new instance of {@link Builder} copy of instance.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * @author devacfr<christophefriederich@mac.com>
     */
    public static final class Builder extends GrantedPermission.AbstractBuilder<Builder> {

        private Builder() {
            super();
        }

        private Builder(final GlobalPermissionEntity source) {
            super(source);
        }

        /**
         * @return Create new instance of {@link GlobalPermissionEntity}.
         */
        public GlobalPermissionEntity build() {
            return new GlobalPermissionEntity(id, permission, group, user);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
