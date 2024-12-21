package com.pmi.tpd.core.database;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.InfrastructureProxy;
import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.IExecutorService;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.exec.IDrainable;
import com.pmi.tpd.api.exec.IForcedDrainable;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.cluster.latch.LatchState;
import com.pmi.tpd.cluster.spring.TransactionAwareLatchedInvocationHandler;
import com.pmi.tpd.core.cluster.AbstractClusterableLatch;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.core.migration.MigrationValidationException;
import com.pmi.tpd.core.util.ProxyUtils;
import com.pmi.tpd.database.DatabaseValidationException;
import com.pmi.tpd.database.DbType;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IMutableDataSourceConfiguration;
import com.pmi.tpd.database.ISwappableDataSource;
import com.pmi.tpd.database.jpa.ISwappableEntityManagerFactory;
import com.pmi.tpd.database.liquibase.ISchemaCreator;
import com.pmi.tpd.database.spi.DefaultDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.database.spi.IDatabaseValidator;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultDatabaseManager implements IDatabaseManager, BeanNameAware, Ordered {

    /** */
    public static final String PROTOTYPE_DATA_SOURCE = "prototypeDataSource";

    /** */
    public static final String ENTITY_MANAGER_FACTORY = "prototypeEntityManagerFactory";

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabaseManager.class);

    /** */
    private final ApplicationContext applicationContext;

    /** */
    private final Cluster cluster;

    /** */
    private final IDatabaseValidator databaseValidator;

    /** */
    private final IMutableDataSourceConfiguration dataSourceConfiguration;

    /** */
    private final IExecutorService executorService;

    /** */
    private final I18nService i18nService;

    /** */
    private final ISchemaCreator schemaCreator;

    /** */
    private final Object lock;

    /** */
    private final ISwappableDataSource swappableDataSource;

    /** */
    private final ISwappableEntityManagerFactory swappableEntityManagerFactory;

    /** */
    private String beanName;

    /** */
    private Duration connectTimeout;

    /** */
    private DataSource realDataSource;

    /** */
    private EntityManagerFactory realEntityManagerFactory;

    /** */
    private IDataSourceConfiguration realDataSourceConfiguration;

    /** */
    private final IEventAdvisorService<?> eventAdvisorService;

    /** */
    private volatile DelegatingDatabaseLatch latch;

    @Autowired
    public DefaultDatabaseManager(final ApplicationContext applicationContext, final Cluster cluster,
            final IDatabaseValidator databaseValidator, final IMutableDataSourceConfiguration dataSourceConfiguration,
            final IExecutorService executorService, final IEventAdvisorService<?> eventAdvisorService,
            final I18nService i18nService, final ISchemaCreator schemaCreator,
            final ISwappableDataSource swappableDataSource,
            final ISwappableEntityManagerFactory swappableEntityManagerFactory) {
        this.applicationContext = applicationContext;
        this.cluster = cluster;
        this.databaseValidator = databaseValidator;
        this.dataSourceConfiguration = dataSourceConfiguration;
        this.executorService = executorService;
        this.eventAdvisorService = eventAdvisorService;
        this.i18nService = i18nService;
        this.schemaCreator = schemaCreator;
        this.swappableDataSource = swappableDataSource;
        this.swappableEntityManagerFactory = swappableEntityManagerFactory;

        this.lock = new Object();
    }

    @Nonnull
    @Override
    public IDatabaseLatch acquireLatch(@Nonnull final LatchMode latchMode) {
        return acquireLatch(latchMode, null);
    }

    @Nonnull
    @Override
    public IDatabaseLatch acquireLatch(@Nonnull final LatchMode latchMode, final @Nullable String latchId) {
        synchronized (lock) {
            if (isLatched()) {
                throw new IllegalStateException("The database has already been latched");
            }

            try {
                // set the latch variable immediately to allow nodes joining the cluster to also
                // be latched. See
                // onNodeAdded
                latch = new DelegatingDatabaseLatch(latchMode, new CountDownLatch(1), eventAdvisorService);
                latch.acquire(latchId);
            } finally {
                // acquire throws an exception when acquiring the latch fails. If that happens,
                // we need to clean up and
                // ensure that the latch is cleared.
                if (!latch.isAcquired()) {
                    latch = null;
                }
            }

            return latch;
        }
    }

    @Override
    public IDatabaseLatch getCurrentLatch() {
        // There is a harmless race condition here where latch is cleared just after
        // isLatched is called. This is
        // harmless because latch is guaranteed to be either null or an acquired latch.
        return isLatched() ? latch : null;
    }

    @Nonnull
    @Override
    public IDatabaseHandle getHandle() {
        return new DefaultDatabaseHandle(dataSourceConfiguration.copy(), swappableDataSource.getWrappedObject(),
                swappableEntityManagerFactory.getWrappedObject(), new DefaultDatabaseTables());
    }

    @Override
    public int getOrder() {
        // The database should be the first thing latched (and, therefore, the last
        // thing unlatched)
        return 0;
    }

    @Nonnull
    @Override
    public LatchState getState() {
        final IDatabaseLatch dbLatch = getCurrentLatch();
        if (dbLatch == null) {
            return LatchState.AVAILABLE;
        }
        if (dbLatch.drain(0, TimeUnit.NANOSECONDS)) {
            return LatchState.DRAINED;
        }
        return LatchState.LATCHED;
    }

    @Override
    public boolean isLatched() {
        final DelegatingDatabaseLatch current = latch;
        return current != null && current.isAcquired();
    }

    @EventListener
    public void onNodeAdded(final ClusterNodeAddedEvent event) {
        final AbstractClusterableLatch current = latch;
        if (current != null) {
            // this can happen because of a race condition between a node joining the
            // cluster and a maintenance task
            // just starting. If this happens, the new node might also need to be latched
            current.onNodeJoined(event.getAddedNode());
        }
    }

    @Nonnull
    @Override
    public IDatabaseHandle prepareDatabase(@Nonnull final IDataSourceConfiguration targetConfiguration) {
        // Open a connection to the database prior to attempting to apply the schema.
        // This ensures the message that is
        // shown if the configuration is wrong doesn't say the schema couldn't be
        // created
        LOGGER.debug("Validating the configuration of the DataSource for the new database");
        validateConfiguration(targetConfiguration);

        // Connect to new database
        LOGGER.debug("Creating a DataSource connected to the target database");
        final DataSource newDataSource = createDataSource(targetConfiguration);

        createSchema(newDataSource);

        // Create EntityManagerFactory for the new database
        LOGGER.debug("Creating Hibernate EntityManagerFactory on the target database and validating the schema");
        final EntityManagerFactory newEntityManager = createEntityManagerFactory(newDataSource, targetConfiguration);

        LOGGER.debug("The new database has been prepared.");

        return new DefaultDatabaseHandle(targetConfiguration, newDataSource, newEntityManager,
                new DefaultDatabaseTables());
    }

    @Override
    public void setBeanName(final String name) {
        beanName = name;
    }

    @Override
    public void validateConfiguration(@Nonnull final IDataSourceConfiguration configuration) {
        checkNotNull(configuration, "configuration");

        LOGGER.debug("Creating a DataSource to test the provided configuration");
        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setDriverClassName(configuration.getDriverClassName());
        dataSource.setPassword(configuration.getPassword());
        dataSource.setUrl(configuration.getUrl());
        dataSource.setUsername(configuration.getUser());

        try {
            DbType.forDriver(configuration.getDriverClassName())
                    .ifPresent(dbType -> dbType.applyTimeout(dataSource, connectTimeout));

            LOGGER.debug("Validating connection and target database for: {}", configuration.getUrl());
            databaseValidator.validate(dataSource);
        } catch (final CannotGetJdbcConnectionException e) {
            LOGGER.warn("A connection could not be opened with the DataSource", e);
            throw new MigrationException(i18nService.createKeyedMessage("app.migration.test.connectfailed"), e);
        } catch (final DataRetrievalFailureException e) {
            LOGGER.warn("Support for the target database could not be verified", e);
            throw new MigrationException(
                    i18nService.createKeyedMessage("app.migration.test.supportunverified", Product.getName()), e);
        } catch (final DataAccessException e) {
            LOGGER.warn("An unexpected exception prevented validating the target database", e);
            throw new MigrationException(
                    i18nService.createKeyedMessage("app.migration.test.unexpectedfailure", Product.getName()), e);
        } catch (final DatabaseValidationException e) {
            throw new MigrationValidationException(e.getKeyedMessage());
        } finally {
            LOGGER.debug("Destroying the test DataSource");
            dataSource.destroy();

        }
    }

    /**
     * Closes the provided {@code DataSource}.
     * <p>
     * If the provided {@code DataSource} implements {@code Closeable}, or has a public {@code close()} method without
     * implementing the interface, it will be invoked here.
     * <p>
     * Note: <i>No exceptions should ever be thrown by this method.</i> Cleaning up should be performed best-effort due
     * to when this method is invoked:
     * <ul>
     * <li>Migration has completed successfully: The new database has been connected and had schema and data from the
     * old database applied to it. Hibernate has been switched to reference it. The configuration has been updated to
     * connect to it on subsequent starts. When cleanup is invoked in this path, all of the work has been done and we're
     * cleaning up the old database. If it fails, the system will still run correctly.</li>
     * <li>Migration has failed: The system has been reverted to the old database and Hibernate has not been switched.
     * The configuration has not been changed. When cleanup is invoked in this path, there is already an exception
     * indicating why the migration failed and all we're doing is cleaning up the new database, which cannot be used. If
     * it fails, the system will still run correctly.</li>
     * </ul>
     *
     * @param dataSource
     *                   the data source to close
     * @throws NullPointerException
     *                              if the provided {@code dataSource} is {@code null}
     */
    void closeDataSource(@Nonnull final DataSource dataSource) {
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

    /**
     * Create a {@code DataSource} which will use the provided {@link DataSourceConfiguration configuration} to connect
     * to the database.
     * <p>
     * Creating the data source <i>may not</i> actually connect to the database. The implementation used at the time
     * this comment was written (BoneCP) does not establish any connections until the first connection is requested. As
     * a result, it is possible that calling this method will not validate the connection. It is assumed that the
     * configuration has already been {@link #validateConfiguration(DataSourceConfiguration) validated} before the
     * migration is started.
     * <p>
     * This method should <i>always</i> return a <i>new</i> {@code DataSource}; it should never reuse an existing
     * instance.
     *
     * @param configuration
     *                      the configuration to use when connecting to the database
     * @return a new data source
     * @throws MigrationException
     *                              if the "dataSourcePrototype" cannot be found or has been wired incorrectly
     * @throws NullPointerException
     *                              if the provided {@code configuration} is {@code null}
     */
    @Nonnull
    DataSource createDataSource(@Nonnull final IDataSourceConfiguration configuration) {
        checkNotNull(configuration, "configuration");

        try {
            return (DataSource) applicationContext.getBean(PROTOTYPE_DATA_SOURCE, configuration);
        } catch (final Throwable t) {
            LOGGER.error("Failed to obtain data source", t);

            // This case should be impossible, because the connection pool _does not_
            // connect on creation. It connects
            // when the first connection is requested. As a result,
            throw new MigrationException(i18nService.createKeyedMessage("app.migration.create.datasource.failed"),
                    Throwables.getRootCause(t));
        }
    }

    /**
     * Creates the schema in the database targeted by the provided {@code DataSource}. application tables as well as
     * their foreign keys, indexes, etc., but will not populate them. When this method returns, the database should
     * contain an empty schema ready for {@link #createSessionFactory(DataSource, DataSourceConfiguration) Hibernate}
     * validation.
     * <p>
     * Because the {@code DataSource} moy not establish any database connections when it is first
     * {@link #createDataSource(DataSourceConfiguration) created}, if the settings have not been verified prior to
     * starting migration, this step may fail if the settings are invalid.
     *
     * @param dataSource
     *                   the data source connected to the database to create the schema in
     * @throws MigrationException
     *                              if the Liquibase changesets which create the schema cannot be applied. This may
     *                              happen because of an issue with the changesets themselves, or because the settings
     *                              provided when the {@code DataSource} was created were invalid and a connection to
     *                              the database cannot be opened
     * @throws NullPointerException
     *                              if the provided {@code dataSource} is {@code null}
     */
    void createSchema(@Nonnull final DataSource dataSource) {
        checkNotNull(dataSource, "dataSource");

        try {
            schemaCreator.createSchema(dataSource);
        } catch (final Throwable t) {
            LOGGER.error("Failed to create schema in target database.", t);
            throw new MigrationException(i18nService.createKeyedMessage("app.migration.create.schema.failed"), t);
        }
    }

    /**
     * Create a Hibernate {@code EntityManagerFactory} which will use the provided {@code DataSource}.
     * <p>
     * Creating the entity manager factory will (lightly) validate the schema. The schema should have already been
     * {@link #createSchema(DataSource) created} before calling this method. This method should <i>always</i> return a
     * <i>new</i> session factory; it should never reuse an existing instance.
     *
     * @param dataSource
     *                            the data source from which the session factory should draw connections
     * @param targetConfiguration
     *                            the data source configuration required to create the new session factory
     * @return a new {@code SessionFactoryImplementor} which will use the provided data source
     * @throws MigrationException
     *                              if the {@code SessionFactoryImplementor} cannot be created. This usually indicates
     *                              the schema is not valid. The most likely cause is that Liquibase and Hibernate have
     *                              a disagreement about how a Java data type should be represented in the database.
     * @throws NullPointerException
     *                              if the provided {@code dataSource} is {@code null}
     */
    @Nonnull
    EntityManagerFactory createEntityManagerFactory(@Nonnull final DataSource dataSource,
        @Nonnull final IDataSourceConfiguration targetConfiguration) {
        checkNotNull(dataSource, "dataSource");
        checkNotNull(targetConfiguration, "targetConfiguration");

        try {
            final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) applicationContext
                    .getBean(ENTITY_MANAGER_FACTORY, dataSource, targetConfiguration, true);
            return entityManagerFactory;
        } catch (final Throwable t) {
            LOGGER.error("Failed to obtain entity manager factory", t);
            throw new MigrationException(
                    i18nService.createKeyedMessage("app.migration.create.entitymanagerfactory.failed"),
                    Throwables.getRootCause(t));
        }
    }

    public void setConnectTimeout(final long connectTimeout) {
        this.connectTimeout = Duration.standardSeconds(connectTimeout);
    }

    private InvocationHandler createLatchingInvocationHandler(final InfrastructureProxy swappable,
        final CountDownLatch countDownLatch) {
        return new TransactionAwareLatchedInvocationHandler(swappable, countDownLatch, swappable.getWrappedObject());
    }

    /**
     * @author Christophe Friederich
     */
    private class DelegatingDatabaseLatch extends AbstractClusterableLatch implements IDatabaseLatch {

        /** */
        private final CountDownLatch latch;

        /** */
        private volatile boolean drained;

        /**
         * @param mode
         * @param latch
         */
        DelegatingDatabaseLatch(final LatchMode mode, final CountDownLatch latch,
                final IEventAdvisorService<?> eventAdvisorService) {
            super(mode, DefaultDatabaseManager.this.cluster, executorService, eventAdvisorService, beanName);
            this.latch = latch;
        }

        @Override
        protected void acquireLocally() {
            realDataSourceConfiguration = dataSourceConfiguration.copy();
            realDataSource = swappableDataSource.swap(ProxyUtils.createProxy(DataSource.class,
                createLatchingInvocationHandler(swappableDataSource, latch),
                InfrastructureProxy.class));
            realEntityManagerFactory = swappableEntityManagerFactory
                    .swap(ProxyUtils.createProxy(EntityManagerFactory.class,
                        createLatchingInvocationHandler(swappableEntityManagerFactory, latch),
                        InfrastructureProxy.class));
        }

        @Override
        protected boolean drainLocally(final long timeout, @Nonnull final TimeUnit unit, final boolean force) {
            isTrue(timeout >= 0, "timeout must be non-negative");
            checkNotNull(unit, "unit");
            ensureInitiator();

            if (drained) {
                return true;
            }

            if (realDataSource instanceof IDrainable) {
                final boolean didDrain = force && realDataSource instanceof IForcedDrainable
                        ? ((IForcedDrainable) realDataSource).forceDrain(timeout, unit)
                        : ((IDrainable) realDataSource).drain(timeout, unit);

                if (!didDrain) {
                    LOGGER.debug("The DataSource could not be drained; some database connections are still open.");
                    return false;
                }

                LOGGER.debug("The DataSource has been drained");
            } else {
                LOGGER.warn("The DataSource for the current database does not implement ForcedDrainable. Existing "
                        + "connections will not be closed.");
            }
            drained = true;
            return drained;
        }

        @Override
        protected void unlatchLocally() {
            ensureInitiator();

            synchronized (lock) {
                // Update the swappables to reference the chosen components. Any new callers
                // beyond this point will no
                // longer block and will instead have their processing performed against the
                // specified target.
                dataSourceConfiguration.update(realDataSourceConfiguration);
                swappableDataSource.swap(realDataSource);
                swappableEntityManagerFactory.swap(realEntityManagerFactory);

                // Null out the latch and render the calling latch useless for future
                // invocations
                DefaultDatabaseManager.this.latch = null;
            }

            // Release the latch. This will allow any callers that were blocked before to
            // continue. The delegate for the
            // invocation handler will be one of the swappables updated above and the call
            // will use the specified
            // target.
            latch.countDown();
        }

        @Override
        public void unlatchTo(@Nonnull final IDatabaseHandle databaseHandle) {
            ensureInitiator();

            synchronized (lock) {
                realDataSource = checkNotNull(databaseHandle.getDataSource(), "newDataSource");
                realDataSourceConfiguration = checkNotNull(databaseHandle.getConfiguration(), "newConfiguration");
                realEntityManagerFactory = checkNotNull(databaseHandle.geEntityManagerFactory(),
                    "newEntityManagerFactory");

                unlatch();
            }
        }

        private void ensureInitiator() {
            if (DefaultDatabaseManager.this.latch != this) {
                throw new IllegalStateException("This latch is no longer active");
            }
        }
    }
}
