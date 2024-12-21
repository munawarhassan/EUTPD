package com.pmi.tpd.core.backup;

import java.io.Closeable;

/**
 * Wraps a database connection and provides methods for interrogating the underlying database's configuration. This is
 * particularly useful for validating preconditions hold true in the target database prior to performing a migration.
 *
 * @see com.pmi.tpd.database.spi.IDatabaseValidator
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMigrationTarget extends Closeable {

    /**
     * Verifies that the database contains no tables with names clashing with the schema created by your application.
     *
     * @return {@code true} if the database is clean, {@code false} otherwise.
     */
    boolean hasNoClashingTables();

    /**
     * Verifies that the configured user for the database connection has the CREATE TABLE_NAME, DROP TABLE_NAME, INSERT
     * and DELETE permissions for the target database.
     *
     * @return {@code true} if the configured user for the database connection has all required schema permissions,
     *         {@code false} otherwise.
     */
    boolean hasRequiredSchemaPermissions();

    /**
     * Verifies that the configured user for the database connection has the CREATE TEMPORARY TABLES (for MySQL
     * databases only).
     *
     * @return {@code true} if the configured user for the database connection has the required permission.
     *         {@code false} otherwise.
     */
    boolean hasRequiredTemporaryTablePermission();

    /**
     * Verifies that database is case-sensitive.
     *
     * @return {@code true} if the database is case-sensitive, {@code false} otherwise.
     */
    boolean isCaseSensitive();

    /**
     * Verifies that the database is configured for storing character data as UTF-8.
     *
     * @return {@code true} if the database is UTF-8, {@code false} otherwise.
     */
    boolean isUtf8();
}
