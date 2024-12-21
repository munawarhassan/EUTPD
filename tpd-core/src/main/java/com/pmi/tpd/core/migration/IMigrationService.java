package com.pmi.tpd.core.migration;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * Allows migrating the system from one database ({@code DataSource} and {@code SessionFactoryImplementor}) to another
 * at runtime.
 * <p>
 * Migration has two types:
 * <ol>
 * <li>{@link #migrate(DataSourceConfiguration) Full}: Used on a fully configured system to migrate all existing data to
 * a new database</li>
 * <li>{@link #setup(DataSourceConfiguration) Setup}: Used on a system which has been bootstrapped but has not had the
 * setup wizard completed to replace the bootstrapped internal database with an external database</li>
 * </ol>
 * Further documentation about these migration types is available on their respective methods.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMigrationService {

    /**
     * Gets the current {@link IMaintenanceService} used in.
     * 
     * @return Returns the current {@link IMaintenanceService} (can <b>not</b> be {@code null}).
     */
    @Nonnull
    public IMaintenanceService getMaintenanceService();

    /**
     * Begins the process for migrating to the specified database, if another migration is not already in progress.
     * <p>
     * Starting a migration performs some initial work before it returns to the caller. That work includes:
     * <ul>
     * <li>Creating a {@code DataSource} using the provided {@link DataSourceConfiguration configuration}</li>
     * <li>Creating the database schema using a connection from the new {@code DataSource}</li>
     * <li>Creating a Hibernate {@code SessionFactoryImplementor} which will draw connections from the new
     * {@code Datasource}, which has the corollary effect of validating the created schema</li>
     * </ul>
     * If any of these initial tasks fail, a {@link MigrationException} is thrown. If those tasks complete successfully,
     * a {@code MaintenanceTaskMonitor} is returned. <i>Further exceptions may be thrown during migration processing,
     * but will only be available on callbacks registered via
     * {@link ITaskMaintenanceMonitor#registerCallback(com.pmi.tpd.core.exec.ICompletionCallback) .</i>
     *
     * @param configuration
     *            the configuration for connecting to the new database
     * @return a {@code MaintenanceTaskMonitor task handle} which can be used to cancel the migration or listen for its
     *         completion
     * @throws MigrationException
     *             if the provided configuration does not allow a successful database connection, if the schema cannot
     *             be created in the specified database or Hibernate fails to validate it, or if another migration is
     *             already in progress
     */
    @Nonnull
    ITaskMaintenanceMonitor migrate(@Nonnull IDataSourceConfiguration configuration);

    /**
     * Begins the process for migrating to the specified database, but performs only a subset of the processing done by
     * a {@link #migrate(DataSourceConfiguration) full migration}.
     * <p>
     * As with a full migration, this method performs some initial work before it returns to the caller:
     * <ul>
     * <li>Creating a {@code DataSource} using the provided {@link DataSourceConfiguration configuration}</li>
     * <li>Creating the database schema using a connection from the new {@code DataSource}</li>
     * <li>Creating a Hibernate {@code SessionFactoryImplementor} which will draw connections from the new
     * {@code Datasource}, which has the corollary effect of validating the created schema</li>
     * </ul>
     * If any of these initial tasks fail, a {@link MigrationException} is thrown. If those tasks complete successfully,
     * a {@code MaintenanceTaskMonitor} is returned. <i>Further exceptions may be thrown during migration processing,
     * but will only be available on callbacks registered via
     * {@link ITaskMaintenanceMonitor#registerCallback(com.pmi.tpd.core.exec.ICompletionCallback) registerCallback}
     * .</i>
     * <p>
     * The migration performed by this method omits some steps that are performed during a full migration and is
     * tailored for use during initial setup. <i>This form of migration should <u>never</u> be applied to a fully setup
     * instance.</i>
     *
     * @param configuration
     *            the configuration for connecting to the new database
     * @return a {@code MaintenanceTaskMonitor task handle} which can be used to cancel the migration and listen for its
     *         completion
     * @throws MigrationException
     *             if the provided configuration does not allow a successful database connection, if the schema cannot
     *             be created in the specified database or Hibernate fails to validate it, or if another migration is
     *             already in progress
     */
    @Nonnull
    ITaskMaintenanceMonitor setup(@Nonnull IDataSourceConfiguration configuration);

    /**
     * Attempts to open a database connection using the provided {@link DataSourceConfiguration configuration}, and,
     * once opened, uses the connection to perform light verification of the connected database.
     *
     * @param configuration
     *            the configuration to test
     * @throws MigrationValidationException
     *             if the provided configuration does not meet the requirements of a migration
     */
    void validateConfiguration(@Nonnull IDataSourceConfiguration configuration);
}
