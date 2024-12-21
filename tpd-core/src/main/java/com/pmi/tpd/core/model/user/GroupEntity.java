package com.pmi.tpd.core.model.user;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.model.AbstractEntityBuilder;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.database.support.Identifiable;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.user.IGroup;

/**
 * <p>
 * GroupEntity class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "Group")
@Table(name = GroupEntity.TABLE_NAME,
        uniqueConstraints = { @UniqueConstraint(name = "uc_t_groupname_col", columnNames = { "name" }) })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GroupEntity extends Identifiable<Long> implements IGroup, IInitializable {

    /** The name of the primary key generator to use. */
    private static final String ID_GEN = "groupIdGenerator";

    /** table name associate to this entity. */
    public static final String TABLE_NAME = "t_group";

    /** */
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME,
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME,
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME, pkColumnValue = "group_id",
            allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
    // @Column(name = "id", nullable = false, unique = true)
    private Long id;

    /** */
    @Nonnull
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    /** */
    @Column(name = "description", length = 255)
    private String description;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_directory", length = 50, nullable = true)
    private UserDirectory directory;

    /** */
    @Column(name = "deleted_timestamp", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    /** */
    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY, //
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<UserEntity> users;

    /**
     * Default constructor.
     */
    public GroupEntity() {

    }

    /**
     * Create new instance filled with {@code builder} parameter data.
     *
     * @param builder
     *            a builder to use.
     */
    public GroupEntity(@Nonnull final Builder builder) {
        this.id = builder.id();
        name = builder.name;
        description = builder.description;
        this.deletedDate = builder.deletedDate;
        this.directory = builder.directory;
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {

    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the user directory associated to.
     */
    public UserDirectory getDirectory() {
        return directory;
    }

    /**
     * @return Returns {@code true} whether the group is active, otherwise {@code false}.
     * @since 2.0
     */
    public boolean isActive() {
        return deletedDate == null;
    }

    /**
     * @return the timestamp when the user was deleted (from external user directory), or {@code null}. Note that this
     *         is only set after deletion and before the user has been cleaned up. In case the user gets "undeleted",
     *         it's cleared again.
     * @since 2.0
     */
    public Date getDeletedDate() {
        return deletedDate;
    }

    /**
     * Create new Builder and fill it with data of the current instance.
     *
     * @return Returns new instance {@link com.pmi.tpd.core.model.user.GroupEntity.Builder} filled this data of this
     *         instance.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * @return Returns new instance of {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class Builder extends AbstractEntityBuilder<Long, GroupEntity, Builder> {

        /** */
        private String name;

        /** */
        private String description;

        /** */
        private Date deletedDate;

        /** */
        private UserDirectory directory;

        /**
         *
         */
        protected Builder() {
        }

        /**
         * @param group
         *            group to use.
         */
        public Builder(@Nonnull final GroupEntity group) {
            super(group);
            this.name = Assert.checkHasText(group.name, "group.name");
            this.description = group.description;
            this.deletedDate = group.deletedDate;
            this.directory = group.directory;
        }

        /**
         * @param name
         *            the name of group
         * @return Returns fluent {@link Builder}.
         */
        public Builder name(@Nonnull final String name) {
            this.name = Assert.checkHasText(name, "name");
            return self();
        }

        /**
         * @param description
         *            the description
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder description(@Nullable final String description) {
            this.description = description;
            return self();
        }

        /**
         * @param directory
         *            the associated user directory.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder directory(@Nullable final UserDirectory directory) {
            this.directory = directory;
            return self();
        }

        /**
         * @param value
         *            the deleted date.
         * @return Returns fluent {@link Builder}.
         * @since 2.0
         */
        @Nonnull
        public Builder deletedDate(@Nullable final Date value) {
            this.deletedDate = value;
            return self();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public GroupEntity build() {
            return new GroupEntity(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected Builder self() {
            return this;
        }

    }

}
