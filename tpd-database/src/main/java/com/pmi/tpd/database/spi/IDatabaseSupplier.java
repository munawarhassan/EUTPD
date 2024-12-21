package com.pmi.tpd.database.spi;

import java.sql.Connection;

import javax.annotation.Nonnull;

import com.google.common.base.Supplier;

/**
 * Allows callers to retrieve detailed information about the connected database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDatabaseSupplier extends Supplier<IDetailedDatabase> {

    /**
     * Retrieves details for the configured database. These details are drawn from the database itself, not inferred
     * from the JDBC driver in use.
     *
     * @return details for the configured database
     * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
     *                                                                   if a connection cannot be opened to the
     *                                                                   database using the configured driver
     * @throws org.springframework.dao.DataRetrievalFailureException
     *                                                                   if metadata cannot be loaded for the configured
     *                                                                   database
     */
    @Override
    @Nonnull
    IDetailedDatabase get();

    /**
     * Retrieves details for the connected database. These details are drawn from the database itself, using the
     * provided connection, not inferred from the JDBC driver providing the connection.
     *
     * @param connection
     *                   an <i>open</i> connection to the database
     * @return details for the connected database
     * @throws org.springframework.dao.DataRetrievalFailureException
     *                                                               if metadata cannot be loaded for the connected
     *                                                               database, or the provided connection is closed
     */
    @Nonnull
    IDetailedDatabase getForConnection(@Nonnull Connection connection);
}
