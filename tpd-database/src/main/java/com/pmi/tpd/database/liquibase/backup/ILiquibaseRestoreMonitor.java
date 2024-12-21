package com.pmi.tpd.database.liquibase.backup;

/**
 * Indicates the status of the restore process.
 *
 * @see ILiquibaseMigrationDao
 * @since 1.3
 */
public interface ILiquibaseRestoreMonitor {

    /**
     * Applying a Liquibase changeset out of {@code total} changesets to restore.
     */
    void onBeginChangeset(ILiquibaseChangeSet change, int index, int total);

    /**
     * Applied a change in the database changeset.
     */
    void onAppliedChange();

    /**
     * Applied the database changeset.
     */
    void onFinishedChangeset();

}
