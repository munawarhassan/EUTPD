package com.pmi.tpd.database.spi;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.google.common.io.Closeables;
import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultDatabaseHandle implements IDatabaseHandle {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabaseHandle.class);

    /** */
    private final IDataSourceConfiguration configuration;

    /** */
    private final DataSource dataSource;

    /** */
    private final EntityManagerFactory entityManagerFactory;

    private final IDatabaseTables databaseTables;

    /**
     * @param configuration
     * @param dataSource
     * @param sessionFactory
     */
    public DefaultDatabaseHandle(@Nonnull final IDataSourceConfiguration configuration,
            @Nonnull final DataSource dataSource, @Nonnull final EntityManagerFactory entityManagerFactory,
            @Nonnull final IDatabaseTables databaseTables) {
        this.configuration = configuration;
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
        this.databaseTables = databaseTables;
    }

    @Override
    public void close() {
        closeDataSource(dataSource);

        // Note: Do _not_ close the EntityManagerFactory.
        //
        // Closing the EntityManagerFactory just shuts down caches. However, at this point, we may have two different
        // EntityManager
        // factories that are both using the same caches. That means, if we close either, it shuts down the caching
        // for both. That results in the system being in an invalid state whether migration succeeds or fails.
    }

    @Nonnull
    @Override
    public IDataSourceConfiguration getConfiguration() {
        return configuration;
    }

    @Nonnull
    @Override
    public IDatabaseTables getDatabaseTables() {
        return databaseTables;
    }

    @Nonnull
    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Nonnull
    @Override
    public EntityManagerFactory geEntityManagerFactory() {
        return entityManagerFactory;
    }

    private void closeDataSource(@Nonnull final DataSource dataSource) {
        checkNotNull(dataSource, "dataSource");

        LOGGER.debug("Closing DataSource to release database connections");
        if (dataSource instanceof Closeable) {
            try {
                Closeables.close((Closeable) dataSource, false);
            } catch (final IOException e) {
            }
        } else {
            final Class<?> dataSourceClass = dataSource.getClass();
            LOGGER.debug("DataSource class [{}] does not implement Closeable", dataSourceClass);

            final Method close = ReflectionUtils.findMethod(dataSourceClass, "close");
            if (close == null) {
                LOGGER.warn("DataSource class [{}] does not have a close() method and will not be closed.",
                    dataSourceClass);
            } else {
                LOGGER.debug("Invoking {}.{}() to close the DataSource", close.getDeclaringClass(), close.getName());
                try {
                    ReflectionUtils.invokeMethod(close, dataSource);
                } catch (final Throwable t) {
                    LOGGER.warn(
                        dataSourceClass + "." + close.getName() + "() did not run cleanly. "
                                + "The DataSource may not have been closed",
                        t);
                }
            }
        }
    }
}
