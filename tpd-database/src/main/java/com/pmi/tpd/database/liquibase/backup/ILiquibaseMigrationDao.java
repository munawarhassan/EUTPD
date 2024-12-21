package com.pmi.tpd.database.liquibase.backup;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.pmi.tpd.api.lifecycle.ICancelState;

/**
 * Backups and restores a database instance using Liquibase.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseMigrationDao {

    /**
     * Backups a database as a Liquibase changelog.
     *
     * @param stream
     *            stream that will receive the XML-formatted Liquibase change log
     * @param author
     *            author that will assigned to the created changesets
     * @param monitor
     *            callback to be notified of the current backup status
     * @throws LiquibaseDataAccessException
     *             if an error occurs within Liquibase or if the stream can not be written to
     */
    void backup(ILiquibaseAccessor dao,
        OutputStream stream,
        String author,
        ILiquibaseBackupMonitor monitor,
        ICancelState cancelState) throws LiquibaseDataAccessException;

    /**
     * Restores a database from a Liquibase changelog.
     *
     * @param stream
     *            stream that is expected to be a XML-formatted Liquibase change log
     * @param tempDir
     *            directory for the intermediate file(s) (for security reasons, this directory should only be visible to
     *            application)
     * @param monitor
     *            callback to be notified of the current restore status
     * @throws LiquibaseDataAccessException
     *             if an error occurs within Liquibase, if the intermediate file(s) can not be created within the
     *             {@code tempDir} or if the stream can not be read
     */
    void restore(ILiquibaseAccessor dao,
        InputStream stream,
        File tempDir,
        ILiquibaseRestoreMonitor monitor,
        ICancelState cancelState) throws LiquibaseDataAccessException;

}
