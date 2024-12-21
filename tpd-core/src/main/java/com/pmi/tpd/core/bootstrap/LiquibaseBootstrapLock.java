package com.pmi.tpd.core.bootstrap;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.IOperation;
import com.pmi.tpd.api.util.IUncheckedOperation;
import com.pmi.tpd.cluster.concurrent.LockException;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.lockservice.LockServiceFactory;

/**
 * A reusable component for locking the bootstrap while an {@link Operation operation} is performed.
 * <p>
 * Separate instances of this interface <i>are related</i>. Locking the bootstrap in one instance locks it in all of
 * them, since Liquibase marks this in a separate transaction.
 * <p>
 * <b>Note</b>: This is not available to plugin developers.
 *
 * @see IBootstrapLock
 * @see IUncheckedOperation
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseBootstrapLock implements IBootstrapLock {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseBootstrapLock.class);

    /** */
    private final DataSource dataSource;

    /** */
    private final I18nService i18nService;

    /**
     * @param dataSource
     * @param i18nService
     */
    public LiquibaseBootstrapLock(final DataSource dataSource, final I18nService i18nService) {
        this.dataSource = dataSource;
        this.i18nService = i18nService;
    }

    @Override
    public <T, E extends Throwable> T withLock(@Nonnull final IOperation<T, E> operation) throws E {
        checkNotNull(operation);

        final Database database = findDatabase(dataSource);
        if (database instanceof DerbyDatabase) {
            ((DerbyDatabase) database).setShutdownEmbeddedDerby(false);
        }
        final liquibase.lockservice.LockService service = getLockService(database);

        LOGGER.debug("Acquiring database lock");
        try {
            service.waitForLock();
            LOGGER.debug("Database lock acquired");
        } catch (final Exception e) {
            throw new LockException(i18nService.createKeyedMessage("app.service.lock.bootstrap.acquirefailed"), e);
        }

        try {
            LOGGER.debug("Performing operation");
            return operation.perform();
        } finally {
            try {
                LOGGER.debug("Releasing database lock");
                service.releaseLock();
                LOGGER.debug("Database lock released");
            } catch (final liquibase.exception.LockException e) {
                throw new LockException(i18nService.createKeyedMessage("app.service.lock.bootstrap.releasefailed"), e);
            } finally {
                closeDatabase(database);
            }
        }
    }

    @Nonnull
    @VisibleForTesting
    Database findDatabase(@Nonnull final DataSource dataSource) {
        return LiquibaseUtils.findDatabase(dataSource);
    }

    @Nonnull
    @VisibleForTesting
    liquibase.lockservice.LockService getLockService(@Nonnull final Database database) {
        return LockServiceFactory.getInstance().getLockService(database);
    }

    private void closeDatabase(final Database database) {
        try {
            database.close();
        } catch (final DatabaseException e) {
            LOGGER.error("Failed to close database", e);
        }
    }
}
