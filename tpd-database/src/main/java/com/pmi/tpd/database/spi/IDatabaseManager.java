package com.pmi.tpd.database.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * Provides information about the currently configured database, gives access to the database resources and handles
 * latching and unlatching of the database to support backup/restore and database migration.
 */
public interface IDatabaseManager extends ILatchableService<IDatabaseLatch> {

    /**
     * @return the handle on database, giving access to the database configuration and resources.
     */
    @Nonnull
    IDatabaseHandle getHandle();

    /**
     * Validates the provided {@code configuration} and attempts to open a database connection using the provided
     * {@code configuration}. Once opened, the database is verified and the schema is created in the target database.
     * Finally, a {@link org.hibernate.engine.spi.SessionFactoryImplementor sessionFactory} is initialized on the
     * initialized database.
     *
     * @param configuration
     *                      the configuration for the database that needs to be prepared for installation/migration
     * @return a {@link IDatabaseHandle handle} to the resources for the database that has been initialized.
     * @throws MigrationValidationException
     *                                      if the provided configuration does not meet the requirements of a migration
     */
    @Nonnull
    IDatabaseHandle prepareDatabase(@Nonnull IDataSourceConfiguration configuration);

    /**
     * Attempts to open a database connection using the provided {@link IDataSourceConfiguration configuration}, and,
     * once opened, uses the connection to perform light verification of the connected database.
     *
     * @param configuration
     *                      the configuration to test
     * @throws MigrationValidationException
     *                                      if the provided configuration does not meet the requirements of a migration
     * @throws NullPointerException
     *                                      if the provided {@code configuration} is {@code null}
     */
    void validateConfiguration(@Nonnull IDataSourceConfiguration configuration);
}
