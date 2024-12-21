package com.pmi.tpd.database.spi;

import java.io.Closeable;

import javax.annotation.Nonnull;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * Handle for all resources and configuration relating to a database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDatabaseHandle extends Closeable {

    /**
     * Closes all database connections and releases any other resources associated with them.
     * <p>
     * This method is overridden to suppress the IOException declared by Closeable.
     */
    @Override
    void close();

    @Nonnull
    IDataSourceConfiguration getConfiguration();

    @Nonnull
    DataSource getDataSource();

    @Nonnull
    EntityManagerFactory geEntityManagerFactory();

    @Nonnull
    IDatabaseTables getDatabaseTables();
}
