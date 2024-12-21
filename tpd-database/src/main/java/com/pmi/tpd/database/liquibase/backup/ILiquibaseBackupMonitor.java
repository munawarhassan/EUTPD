package com.pmi.tpd.database.liquibase.backup;

/**
 * Indicates the status of the backup process.
 *
 * @see ILiquibaseMigrationDao
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseBackupMonitor {

    /**
     * The backup of the database has started and {@code totalRows} rows are expected to be processed.
     */
    void started(long totalRows);

    /**
     * The backup has exported one row out of the {@code totalRows} rows to export.
     */
    void rowWritten();

}
