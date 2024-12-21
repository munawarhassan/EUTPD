package com.pmi.tpd.core.model.user;

import static com.pmi.tpd.database.support.IdentifierUtils.toLowerCase;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

import org.hibernate.Hibernate;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.database.support.Identifiable;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.core.model.Converters.PermissionConverter;

/**
 * Base class for granted permissions as global or specific to source object.
 *
 * @author Christophe Friederich
 * @since 2.0
 * @see GlobalPermissionEntity
 */
@Entity(name = "GrantedPermission")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class GrantedPermission extends Identifiable<Long> {

    /** */
    private static final String ID_GEN = "grantedPermIdGenerator";

    /** */
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME, //
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME, //
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME, pkColumnValue = "permission_id",
            allocationSize = 1)
    @Id
    @GeneratedValue(generator = ID_GEN, strategy = GenerationType.TABLE)
    private final Long id;

    /** */
    @Column(name = "permission_id", nullable = false)
    @Convert(converter = PermissionConverter.class)
    private final Permission permission;

    /** */
    @Column(name = "group_name", length = 255, nullable = true)
    private final String group;

    /** */
    @ManyToOne(fetch = FetchType.LAZY)
    private final UserEntity user;

    /**
     * Default construct.
     */
    protected GrantedPermission() {
        this.id = null;
        this.permission = null;
        this.group = null;
        this.user = null;
    }

    /**
     * @param id
     *            a id.
     * @param permission
     *            a permission.
     * @param group
     *            a group name.
     * @param user
     *            a user.
     */
    protected GrantedPermission(final Long id, final Permission permission, final String group, final UserEntity user) {
        this.id = id;
        this.permission = permission;
        this.group = group == null ? null : toLowerCase(group);
        this.user = user;
    }

    /**
     * Accept visistor.
     *
     * @param visitor
     *            the visitor to accept.
     */
    public abstract void accept(@Nonnull IGrantedPermissionVisitor visitor);

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!getClass().equals(Hibernate.getClass(o))) {
            return false;
        }

        final GrantedPermission that = (GrantedPermission) o;
        return Objects.equals(getPermission(), that.getPermission()) && Objects.equals(getGroup(), that.getGroup())
                && Objects.equals(getId(), that.getId()) && Objects.equals(getUser(), that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPermission(), getGroup(), getId(), getUser());
    }

    /**
     *
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return Returns the group name.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return Returns the permission.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * @return Returns the user.
     */
    public UserEntity getUser() {
        return user;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("group", group)
                .add("permission", permission)
                .add("user", user)
                .toString();
    }

    /**
     * @author devacfr<christophefriederich@mac.com>
     * @param <B>
     */
    protected abstract static class AbstractBuilder<B extends AbstractBuilder<B>> {

        /** */
        protected String group;

        /** */
        protected Long id;

        /** */
        protected Permission permission;

        /** */
        protected UserEntity user;

        /**
         *
         */
        protected AbstractBuilder() {

        }

        /**
         * Create new instance using a specific granted permission.
         *
         * @param source
         *            the granted permission to use
         */
        protected AbstractBuilder(final GrantedPermission source) {
            group = source.getGroup();
            id = source.getId();
            permission = source.getPermission();
            user = source.getUser();
        }

        /**
         * @param group
         *            a group name
         * @return Returns fluent B builder.
         */
        public B group(final String group) {
            this.group = group;
            return self();
        }

        /**
         * @param id
         *            a id of granted permission.
         * @return Returns fluent B builder.
         */
        public B id(final long id) {
            this.id = id;
            return self();
        }

        /**
         * @param permission
         *            a permission
         * @return Returns fluent B builder.
         */
        public B permission(final Permission permission) {
            this.permission = permission;
            return self();
        }

        /**
         * @param user
         *            a user.
         * @return Returns fluent B builder.
         */
        public B user(final UserEntity user) {
            this.user = user;
            return self();
        }

        /**
         * @return Returns the instance of current builder.
         */
        protected abstract B self();

    }

}
