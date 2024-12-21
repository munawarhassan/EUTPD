package com.pmi.tpd.database;

import javax.annotation.Nonnull;

/**
 * Allows the {@code DataSource} connectivity settings to be updated at runtime.
 * <p>
 * The primary use case for the existence of this interface is database migration. When migrating to a new database, all
 * aspects of the system which reference the JDBC settings (such as ActiveObjects and the application properties) need
 * to be updated. This mutable configuration interface allows that information to be changed after migration is
 * completed.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMutableDataSourceConfiguration extends IDataSourceConfiguration {

    /**
     * Create a no mutable copy of {@link IMutableDataSourceConfiguration}.
     *
     * @return Returns a no mutable copy of this instance.
     */
    @Nonnull
    IDataSourceConfiguration copy();

    /**
     * Update the this instance with new {@code configuration} data source configuration.
     *
     * @param configuration
     *                      the new configuration to use.
     * @return Returns a no mutable instance of this data source configuration before update.
     */
    @Nonnull
    IDataSourceConfiguration update(IDataSourceConfiguration configuration);
}
