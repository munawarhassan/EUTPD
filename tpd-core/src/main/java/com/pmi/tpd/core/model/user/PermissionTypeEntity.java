package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.pmi.tpd.database.support.Identifiable;
import com.pmi.tpd.core.model.Converters.PermissionConverter;
import com.pmi.tpd.security.permission.Permission;

/**
 * Model class for mapping a permission to a weight.
 * <p>
 * <b>This model class is not intended to be used outside of Hibernate.</b> {@link Permission} already manages its own
 * {@link Permission#getWeight() weight}, and all code should rely on those values. However, in order to allow certain
 * calculations, such as determining the "highest" permission for a user or group, to be performed in the database more
 * efficiently, the database needs a concept of weight as well. That means Hibernate needs a model class, allowing the
 * construction of queries which join to that data.
 * <p>
 * This entity class has been marked as {@code Immutable}, indicating that Hibernate should not allow instances of it to
 * be updated. Also, no DAO has been implemented for it. All updates to the weights should be done using Liquibase
 * changesets. See {@code liquibase/r2_0/permission-schema.xml} for creation/population of the associated table.
 * <p>
 * Because this class is intended to be a database representation of {@link Permission}, its ID is not generated. It
 * must be set when the instance is constructed, and it is set from the {@link Permission#getId() permission ID}.
 */
@Cacheable
@Entity(name = "PermissionType")
@Table(name = PermissionTypeEntity.TABLE_NAME,
        uniqueConstraints = { @UniqueConstraint(name = "uc_t_permission_typepermission_weight_col",
                columnNames = { "permission_weight" }) })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Immutable
public class PermissionTypeEntity extends Identifiable<Permission> {

    /** */
    public static final String TABLE_NAME = "t_permission_type";

    /** */
    @Id // Not generated
    @Column(name = "permission_id")
    @Convert(converter = PermissionConverter.class)
    private final Permission id;

    /** */
    @Column(name = "permission_weight", nullable = false)
    private final int weight;

    /**
     * Default constructor.
     */
    public PermissionTypeEntity() {
        id = null;
        weight = -1;
    }

    /**
     * Create a new permission type.
     *
     * @param permission
     *            a permission to use
     */
    public PermissionTypeEntity(@Nonnull final Permission permission) {
        id = permission;
        weight = permission.getWeight();
    }

    @Override
    public Permission getId() {
        return id;
    }

    /**
     * @return Returns the weight of permission.
     */
    public int getWeight() {
        return weight;
    }

}
