package com.pmi.tpd.database;

import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides access to the driver class name, credentials and URL which are used to connect the {@code DataSource} to the
 * database.
 *
 * @see com.pmi.tpd.database.IMutableDataSourceConfiguration
 * @since 1.3
 * @author Christophe Friederich
 */
public interface IDataSourceConfiguration {

    /**
     * Gets the driver class name.
     *
     * @return Returns a {@link String} representing the driver class name.
     */
    @Nonnull
    String getDriverClassName();

    /**
     * Gets the password used for jdbc connection.
     *
     * @return returns a {@link String} representing the password.
     */
    @Nonnull
    String getPassword();

    /**
     * Gets the set of jdbc database properties.
     *
     * @return returns a instance of {@link Properties} containing a set of jdbc database properties.
     */
    @Nullable
    Properties getProperties();

    /**
     * Gets the jdbc url.
     *
     * @return Returns a {@link String} representing the jdbc url.
     */
    @Nonnull
    String getUrl();

    /**
     * Gets the username used for jdbc connection.
     *
     * @return returns a {@link String} representing the username.
     */
    @Nonnull
    String getUser();

    /**
     * Gets the indicating whether the password is set.
     *
     * @return Returns {@code true} whether the password is set, otherwise {@code false}.
     */
    boolean isPasswordSet();
}
