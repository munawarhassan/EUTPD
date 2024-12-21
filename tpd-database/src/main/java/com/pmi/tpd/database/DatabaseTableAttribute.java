package com.pmi.tpd.database;

/**
 * Attributes that can be used to decorate and construct queries for {@link DatabaseTable}s.
 *
 * @see {@link DatabaseTable#getTableNames(DatabaseTableAttribute, DatabaseTableAttribute...)}.
 * @author Christophe Friederich
 * @since 1.3
 */
public enum DatabaseTableAttribute {
    /**
     * PREPOPULATED tables are pre-populated with data when the initial schema is created by liquibase. We clear these
     * out before restoring data during a migration.
     */
    PREPOPULATED
}
