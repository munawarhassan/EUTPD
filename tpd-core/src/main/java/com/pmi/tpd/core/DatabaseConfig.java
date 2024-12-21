package com.pmi.tpd.core;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.IExecutorService;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.context.annotation.NotActiveForIntegrationTest;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.core.cache.CacheConfiguration;
import com.pmi.tpd.core.database.DatabaseConnectionConfiguration;
import com.pmi.tpd.core.database.DatabaseValidator;
import com.pmi.tpd.core.database.DefaultDatabaseManager;
import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.database.DatabaseConstants;
import com.pmi.tpd.database.DelegatingSwappableDataSource;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.database.IMutableDataSourceConfiguration;
import com.pmi.tpd.database.ISwappableDataSource;
import com.pmi.tpd.database.bonecp.CleanupConnectionHook;
import com.pmi.tpd.database.bonecp.SpringBoneCPDataSource;
import com.pmi.tpd.database.config.DefaultDataSourceConfiguration;
import com.pmi.tpd.database.jpa.ISwappableEntityManagerFactory;
import com.pmi.tpd.database.liquibase.DefaultSchemaLiquibase;
import com.pmi.tpd.database.liquibase.ISchemaCreator;
import com.pmi.tpd.database.spi.DefaultDatabaseSupplier;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.database.spi.IDatabaseSupplier;
import com.pmi.tpd.database.spi.IDatabaseTables;
import com.pmi.tpd.database.spi.IDatabaseValidator;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.spring.transaction.ITransactionSynchronizer;

/**
 * <p>
 * DatabaseConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@NotActiveForIntegrationTest
@Configuration
@Import({ CacheConfiguration.class })
@DependsOn({ CacheConfiguration.CACHE_MANAGER_BEAN_NAME })
public class DatabaseConfig implements EnvironmentAware {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    /** */
    private Environment environment;

    /** */
    private RelaxedPropertyResolver dbPropertyResolver;

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        this.dbPropertyResolver = new RelaxedPropertyResolver(environment, "database.");
    }

    /**
     * @param configuration
     * @return
     * @throws ClassNotFoundException
     */

    @Bean(name = "prototypeDataSource", autowire = Autowire.NO, destroyMethod = "")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @Lazy
    public DataSource boneCPDataSource(final IDataSourceConfiguration configuration) {
        final CleanupConnectionHook connectionHook = new CleanupConnectionHook();
        final SpringBoneCPDataSource datasource = new SpringBoneCPDataSource(configuration);
        datasource.setAcquireIncrement(dbPropertyResolver.getProperty("pool.acquireIncrement", Integer.class, 2));
        datasource.setConnectionHook(connectionHook);
        datasource.setLeasedConnectionTracker(connectionHook);
        datasource.setConnectionTimeoutInSeconds(
            dbPropertyResolver.getProperty("pool.connection.timeout", Integer.class, 15));
        // We must set the isolation level on the data source as well as on the entity manager factory.
        // Spring transactions allow us to specify an isolation level, which by default matches the datasource's
        // isolation level. When inside a spring transaction the transaction's isolation level clobbers the session's
        // transaction level. For most DBs the default is 2/READ-COMMITTED but for MySQL 5.5 it is 4/REPEATABLE_READ.
        datasource.setDefaultTransactionIsolation("READ_COMMITTED");
        datasource.setDisableConnectionTracking(true);
        datasource.setIdleConnectionTestPeriodInMinutes(
            dbPropertyResolver.getProperty("pool.idle.testInterval", Integer.class, 10));
        datasource.setIdleMaxAgeInMinutes(dbPropertyResolver.getProperty("pool.idle.maxAge", Integer.class, 30));
        datasource.setMaxConnectionsPerPartition(
            dbPropertyResolver.getProperty("pool.partition.connection.maximum", Integer.class, 20));
        datasource.setMinConnectionsPerPartition(
            dbPropertyResolver.getProperty("pool.partition.connection.minimum", Integer.class, 3));
        datasource.setPartitionCount(dbPropertyResolver.getProperty("pool.partition.count", Integer.class, 4));
        datasource.setPoolAvailabilityThreshold(
            dbPropertyResolver.getProperty("pool.partition.connection.threshold", Integer.class, 10));
        datasource.setStatementsCacheSize(dbPropertyResolver.getProperty("pool.cache.statements", Integer.class, 10));
        // datasource.setReleaseHelperThreads(
        // dbPropertyResolver.getProperty("pool.threads", Integer.class, 2));
        datasource.setStatisticsEnabled(environment.getProperty("jmx.enabled", Boolean.class, false));
        // datasource.setLazyInit(true);
        return datasource;
    }

    /**
     * Produces new {@link org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory} to be managed by the
     * Spring container.
     *
     * @return Returns {@link org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory} to be managed by the
     *         Spring container.
     * @throws java.lang.ClassNotFoundException
     *                                          if Derby is not on the classpath.
     */
    @Bean(name = "dataSource")
    public ISwappableDataSource datasource(@Named("prototypeDataSource") final DataSource dataSource)
            throws ClassNotFoundException {
        return new DelegatingSwappableDataSource(dataSource);
    }

    /**
     * Produces new {@link liquibase.integration.spring.SpringLiquibase} to be managed by the Spring container.
     *
     * @param dataSource
     *                   The DataSource that liquibase will use to perform the migration.
     * @return Returns {@link liquibase.integration.spring.SpringLiquibase} to be managed by the Spring container.
     */
    @Bean(ApplicationConstants.Liquibase.LIQUIBASE_BEAN_NAME)
    public DefaultSchemaLiquibase liquibase(@Named("dataSource") final DataSource dataSource,
        final IApplicationProperties applicationProperties) {
        String liquibaseContexts = dbPropertyResolver.getProperty("liquibase.contexts");
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            liquibaseContexts = "test,dev,production";
        }
        final DefaultSchemaLiquibase liquibase = new DefaultSchemaLiquibase(dataSource);
        liquibase.setShouldRun(true);
        liquibase.setChangeLog(ApplicationConstants.Liquibase.CHANGE_LOG_LOCATION);
        liquibase.setContexts(liquibaseContexts);
        return liquibase;
    }

    @Bean
    public IDatabaseConfigurationService databaseConnectionConfiguration(
        @Nonnull final IApplicationConfiguration settings,
        final IAuthenticationContext authenticationContext,
        final IClock clock,
        final I18nService i18nService,
        final ITransactionSynchronizer synchronizer) {
        return new DatabaseConnectionConfiguration(settings, authenticationContext, clock, i18nService, synchronizer);
    }

    @Bean
    public IDataSourceConfiguration dataSourceConfiguration(
        final IDatabaseConfigurationService databaseConfigurationService) {
        try {
            return databaseConfigurationService.loadDataSourceConfiguration();
        } catch (final IOException ex) {
            LOGGER.warn(ex.getMessage());
        }
        // use full property name, i.e already prefixed with 'database.'
        final String driverClassName = environment.getProperty(DatabaseConstants.PROP_JDBC_DRIVER);
        final String user = environment.getProperty(DatabaseConstants.PROP_JDBC_USER);
        final String password = environment.getProperty(DatabaseConstants.PROP_JDBC_PASSWORD);
        final String url = environment.getProperty(DatabaseConstants.PROP_JDBC_URL);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Database Configuration-> driverClassName: {}, username: {}, password: {}, url: {}",
                driverClassName,
                user,
                "**********",
                url);
        }
        final DefaultDataSourceConfiguration configuration = new DefaultDataSourceConfiguration(driverClassName, user,
                password, url);
        configuration.setConnectTimeout(dbPropertyResolver.getProperty("pool.connection.timeout", Integer.class, 15));
        return configuration;
    }

    /**
     * @param dataSource
     * @return
     */
    @Bean(name = "databaseSupplier")
    public IDatabaseSupplier databaseSupplier(@Named("dataSource") final DataSource dataSource) {
        final DefaultDatabaseSupplier databaseSupplier = new DefaultDatabaseSupplier(dataSource);
        databaseSupplier.setIgnoreUnsupported(dbPropertyResolver.getProperty("ignoreunsupported", Boolean.class, true));
        return databaseSupplier;
    }

    @Bean
    public IDatabaseTables getDatabaseTables() {
        return new DefaultDatabaseTables();
    }

    /**
     * @param clusterService
     * @param databaseSupplier
     * @param i18nService
     * @return
     */
    @Bean
    public IDatabaseValidator databaseValidator(@Nullable final IClusterService clusterService,
        @Nonnull final IDatabaseSupplier databaseSupplier,
        @Nonnull final I18nService i18nService,
        final IDatabaseTables databaseTables) {
        return new DatabaseValidator(clusterService, databaseSupplier, i18nService, databaseTables);
    }

    /**
     * @param applicationContext
     * @param cluster
     * @param databaseValidator
     * @param dataSourceConfiguration
     * @param executorService
     * @param i18nService
     * @param liquibaseAccessor
     * @param swappableDataSource
     * @param swappableEntityManagerFactory
     * @return
     */
    @Bean
    public IDatabaseManager dataManager(final ApplicationContext applicationContext,
        final Cluster cluster,
        final IDatabaseValidator databaseValidator,
        final IMutableDataSourceConfiguration dataSourceConfiguration,
        final IExecutorService executorService,
        final IEventAdvisorService<?> eventAdvisorService,
        final I18nService i18nService,
        final ISchemaCreator schemaCreator,
        final ISwappableDataSource swappableDataSource,
        final ISwappableEntityManagerFactory swappableEntityManagerFactory) {
        final DefaultDatabaseManager databaseManager = new DefaultDatabaseManager(applicationContext, cluster,
                databaseValidator, dataSourceConfiguration, executorService, eventAdvisorService, i18nService,
                schemaCreator, swappableDataSource, swappableEntityManagerFactory);
        databaseManager
                .setConnectTimeout(dbPropertyResolver.getProperty("migration.test.connect.timeout", Long.class, 6l));
        return databaseManager;
    }

}
