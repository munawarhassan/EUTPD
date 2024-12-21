package com.pmi.tpd.database.liquibase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.resource.ResourceAccessor;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@Singleton
public class DefaultSchemaLiquibase extends SpringLiquibase implements ISchemaCreator {

    private long lockMaxWait = 30;

    private long lockPollInterval = 5;
    //
    // <property name="lockMaxWait" value="${db.schema.lock.maxWait}"/>
    // <property name="lockPollInterval" value="${db.schema.lock.pollInterval}"/>

    // ########################################################################################################################
    // # Database Schema
    // ########################################################################################################################
    //
    // # These properties control aspects of how the database schema is managed.
    //
    // # Defines the maximum amount of time the system can wait to acquire the schema lock. Shorter values will prevent
    // long
    // # delays on server startup when the lock is held by another instance or, more likely, when the lock was not
    // released
    // # properly because a previous start was interrupted while holding the lock. This can happen when the system is
    // killed
    // # while it is attempting to update its schema.
    // #
    // # This value is in **seconds**.
    // db.schema.lock.maxWait=30
    // # Defines the amount of time to wait between attempts to acquire the schema lock. Slower polling produces less
    // load,
    // # but may delay acquiring the lock.
    // #
    // # This value is in **seconds**.
    // db.schema.lock.pollInterval=5
    //
    public DefaultSchemaLiquibase(final DataSource dataSource) {
        setDataSource(dataSource);
    }

    public void setLockMaxWait(final long lockMaxWait) {
        this.lockMaxWait = TimeUnit.SECONDS.toMillis(Math.max(1L, lockMaxWait));
    }

    public void setLockPollInterval(final long lockPollInterval) {
        this.lockPollInterval = TimeUnit.SECONDS.toMillis(Math.max(1L, lockPollInterval));
    }

    @Override
    protected Database createDatabase(final Connection c, final ResourceAccessor resourceAccessor)
            throws DatabaseException {
        final Database database = super.createDatabase(c, resourceAccessor);

        if (database instanceof DerbyDatabase) {
            ((DerbyDatabase) database).setShutdownEmbeddedDerby(false);
        }

        // Adjust the lock timeouts. The defaults applied by Liquibase are ridiculously long. This LockService is
        // cached by Liquibase for later reuse, so configuring it here will be picked up everywhere where locking
        // is performed for this Database instance
        final LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.setChangeLogLockRecheckTime(lockPollInterval);
        lockService.setChangeLogLockWaitTime(lockMaxWait);

        return database;
    }

    @Override
    public void createSchema(final DataSource dataSource) throws LiquibaseException {
        setDataSource(dataSource);
        Connection c = null;
        Liquibase liquibase = null;
        try {
            c = getDataSource().getConnection();
            liquibase = createLiquibase(c);
            performUpdate(liquibase);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            if (liquibase != null) {
                liquibase.close();
            }
        }
    }

}
