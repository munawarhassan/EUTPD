package com.pmi.tpd.database.liquibase;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CleanupFailureDataAccessException;

import com.google.common.base.Function;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultLiquibaseSession implements ILiquibaseSession {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLiquibaseSession.class);

    /**
     * A function that returns the lowercased name of a Liquibase table passed to it.
     */
    public static final Function<Table, String> TO_LOWERCASE_TABLE_NAME = table -> table.getName().toLowerCase();

    /**
     * A function that returns the lowercased name of a Liquibase table column passed to it.
     */
    public static final Function<Column, String> TO_LOWERCASE_COLUMN_NAME = column -> column.getName().toLowerCase();

    /** */
    private final Database database;

    /**
     * A lazily-loaded snapshot of the underlying database structure, used to provide metadata for backup-related
     * operations.
     */
    private DatabaseSnapshot snapshot;

    /**
     * The number of changes executed in the context of the current database transaction.
     */
    private long changeCount;

    /**
     * @param dataSource
     */
    public DefaultLiquibaseSession(final DataSource dataSource) {
        this.database = LiquibaseUtils.findDatabase(dataSource);
    }

    /**
     * @param connection
     */
    public DefaultLiquibaseSession(final Connection connection) {
        this.database = LiquibaseUtils.findDatabaseForConnection(connection);
    }

    public DefaultLiquibaseSession(final Database database) {
        this.database = database;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public DatabaseSnapshot getSnapshot() {
        if (snapshot == null) {
            LOGGER.info("Examining structure of source database");
            final SnapshotGeneratorFactory snapShotFactory = SnapshotGeneratorFactory.getInstance();
            try {
                snapshot = snapShotFactory
                        .createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
            } catch (final DatabaseException | InvalidExampleException e) {
                LOGGER.error("Failed to obtain snapshot", e);
                throw new LiquibaseDataAccessException("Failed to obtain snapshot", database, e);
            }
        }
        return snapshot;
    }

    @Override
    public void incrementChangeCount() {
        changeCount++;
    }

    @Override
    public long getChangeCount() {
        return changeCount;
    }

    @Override
    public void resetChangeCount() {
        changeCount = 0;
    }

    @Override
    public void close() {
        LOGGER.debug("Closing Liquibase");
        try {
            // release resources
            getDatabase().close();
            LOGGER.debug("Liquibase closed");
        } catch (final DatabaseException e) {
            throw new CleanupFailureDataAccessException("Failed to close database", e);
        }
    }
}
