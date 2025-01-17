package com.pmi.tpd.core.database;

import static com.pmi.tpd.database.DatabaseTableAttribute.PREPOPULATED;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.core.model.audit.AuditEventEntity;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.PayloadEntity;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.ProductIdEntity;
import com.pmi.tpd.core.model.euceg.StatusAttachment;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;
import com.pmi.tpd.core.model.upgrade.UpgradeHistory;
import com.pmi.tpd.core.model.upgrade.UpgradeHistoryVersion;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.PermissionTypeEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.database.DatabaseTableAttribute;
import com.pmi.tpd.database.spi.IDatabaseTable;

/**
 * Enumeration of application database tables for the current schema. No foreign key constraints will be violated when
 * the contents of these tables are restored to an empty application schema, provided they are restored in the order
 * shown below. The foreign key relationships of each table are indicated in inline comments below. Some tables are
 * decorated with additional {@link DatabaseTableAttribute attributes} which can be queried for by passing arguments to
 * {@link #getTableNames}.
 * <p>
 * <strong>NOTE (schema changes):</strong> This enum <strong>must</strong> be kept up-to-date with any schema changes
 * that effect table names or foreign keys!
 * <p>
 * <strong>NOTE (self-referencing foreign keys):</strong> If any table is added a self-referencing foreign key, its
 * instance constructor should specify a sorting column (see {@link #APP_USER_GROUP} for an example). If present, that
 * column will be used to order the rows of the tables in the database backups generated by application, to ensure no
 * foreign key violations when restoring the database .
 * <p>
 * <strong>WARNING: As noted above, the order of the values in this enum is critical for performing restores without
 * violating referential integrity constraints. When updating, ensure that all tables declaring a foreign key reference
 * to another table are positioned <em>below</em> the referenced table. Do not reorder the enums without thoroughly
 * testing.</strong>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum DatabaseTable implements IDatabaseTable {

    // Properties tables
    /** */
    APP_PROPERTY(PropertySetItem.TABLE_NAME),

    // Application tables

    /** */
    APP_GROUP(GroupEntity.TABLE_NAME, PREPOPULATED),

    /** */
    APP_USER(UserEntity.TABLE_NAME, PREPOPULATED),

    /** */
    APP_PERMISSION_TYPE(PermissionTypeEntity.TABLE_NAME, PREPOPULATED),

    /** */
    APP_GLOBALPERMISSION(GlobalPermissionEntity.TABLE_NAME, PREPOPULATED), // FK -> t_user

    /** */
    APP_USER_GROUP("t_user_group", PREPOPULATED), // FK -> t_group, t_user

    /** */
    UPGRADE_HISTORY(UpgradeHistory.TABLE_NAME),
    /** */
    UPGRADE_HISTORY_VERSION(UpgradeHistoryVersion.TABLE_NAME),

    /** */
    AUDIT_EVENT(AuditEventEntity.TABLE_NAME),
    /** */
    AUDIT_EVENT_DATA(AuditEventEntity.TABLE_NAME_DATA), // FK -> t_audit_event

    /** */
    ATTACHMENT(AttachmentEntity.TABLE_NAME),
    /** */
    PRODUCT_ID(ProductIdEntity.TABLE_NAME),
    /** */
    PAYLOAD(PayloadEntity.TABLE_NAME), // fk - product, submission
    /** */
    PRODUCT(ProductEntity.TABLE_NAME),
    /** */
    PRODUCT_ATTACHMENT(ProductEntity.TABLE_NAME_ATTACHMENT), // FK -> production
    /** */
    SUBMISSION(SubmissionEntity.TABLE_NAME),
    /** */
    SUBMISSION_ATTACHMENT(SubmissionEntity.TABLE_NAME_EXPORTED_ATTACHMENT), // FK -> submission
    /** */
    TRANSMIT_RECEIPT(TransmitReceiptEntity.TABLE_NAME), // FK -> submission
    /** */
    SUBMITTER(SubmitterEntity.TABLE_NAME),
    /** */
    STATUS_ATTACHMENT(StatusAttachment.TABLE_NAME), // fk -> (attachment, submitter)

    /** */
    HIBERNATE_GENERATED_ID(ApplicationConstants.Jpa.Generator.NAME, PREPOPULATED);

    /** */
    private static final Map<String, DatabaseTable> NAME_TO_TABLE_MAP = buildNameToTableMap();

    private static Map<String, DatabaseTable> buildNameToTableMap() {
        final ImmutableMap.Builder<String, DatabaseTable> builder = ImmutableMap.builder();
        for (final DatabaseTable table : values()) {
            builder.put(table.getTableName(), table);
        }
        return builder.build();
    }

    /**
     * Gets the indicating whether the table is known.
     *
     * @param tableName
     *            the table name to check.
     * @return Returns {@link true} whether the table is known, otherwise {@literal false}.
     */
    public static boolean isKnownTable(final String tableName) {
        return NAME_TO_TABLE_MAP.containsKey(tableName);
    }

    /** */
    private final String tableName;

    /** Ordering column for the Liquibase changeset in a backup. */
    private final String orderingColumn;

    /** */
    private final Set<DatabaseTableAttribute> attributes;

    DatabaseTable(final String tableName, final DatabaseTableAttribute... attributes) {
        this(tableName, (String) null, attributes);
    }

    DatabaseTable(final String tableName, final String orderingColumn, final DatabaseTableAttribute... attributes) {
        this.tableName = tableName.toLowerCase(Locale.US);
        this.orderingColumn = orderingColumn;
        this.attributes = attributes.length > 0 ? EnumSet.copyOf(ImmutableSet.copyOf(attributes))
                : Collections.<DatabaseTableAttribute> emptySet();
    }

    /**
     * @return the lower-cased name of the table in the database
     */
    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * @return an optional column name to order the table's rows in a backup
     */
    @Override
    public String getOrderingColumn() {
        return orderingColumn;
    }

    @Override
    public Set<DatabaseTableAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @return all the application tables
     */
    public static List<IDatabaseTable> getTables() {
        return ImmutableList.copyOf(DatabaseTable.values());
    }

}
