package com.pmi.tpd.database;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.database.config.RemoveSetupConfigurationRequest;

/**
 * An interface for classes that modify the configuration file.
 */
public interface IDatabaseConfigurationService {

    /**
     * @return the data source configuration as defined in the app-config properties
     */
    @Nonnull
    IDataSourceConfiguration loadDataSourceConfiguration() throws IOException;

    /**
     * A short-cut for @{code saveDataSourceConfiguration(dataSourceConfig, Option.<String>none())}
     */
    void saveDataSourceConfiguration(@Nonnull IDataSourceConfiguration dataSourceConfig);

    /**
     * Saves the given data source configuration, marking the amendment with the given message, if any.
     *
     * @param dataSourceConfig
     *                         the data source configuration to be saved
     * @param message
     *                         an optional message describing the purpose or context of the amendments
     */
    void saveDataSourceConfiguration(@Nonnull IDataSourceConfiguration dataSourceConfig,
        @Nonnull Optional<String> message);

    /**
     * Removes setup related properties, leaving comments describing what properties were removed and when.
     */
    void removeSetupProperties(@Nonnull RemoveSetupConfigurationRequest request);
}
