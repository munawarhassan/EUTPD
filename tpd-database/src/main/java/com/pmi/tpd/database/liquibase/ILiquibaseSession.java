package com.pmi.tpd.database.liquibase;

import org.springframework.dao.CleanupFailureDataAccessException;

import liquibase.database.Database;
import liquibase.snapshot.DatabaseSnapshot;

/**
 * Encapsulates all the state maintained by {@link DefaultLiquibaseAccessor} between invocations of
 * {@link DefaultLiquibaseAccessor#close}. TODO There'd be some argument for making {@link DefaultLiquibaseAccessor} a
 * non-singleton with non-threadlocal state, and constructing one per thread using a factory, but we'll live with this
 * for now.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseSession {

    /**
     * @return The {@link Database} associated with this session. Each session is bound to a single database so
     *         implementations should return the same object on subsequent invocations.
     */
    Database getDatabase();

    /**
     * @return The {@link DatabaseSnapshot} associated with this session. This should be used only for resolving
     *         high-level, unchanging meta-data about the database, therefore for performance reasons implementations
     *         may cache this value and return the same object on subsequent invocations. This means that operations
     *         like schema changes will not always be reflected in the returned {@link DatabaseSnapshot shapshot}.
     */
    DatabaseSnapshot getSnapshot();

    /**
     * Increment the number of changes associated with the current transaction. Should be called every time an
     * additional change is made.
     */
    void incrementChangeCount();

    /**
     * @return the number of changes associated with the current transaction.
     */
    long getChangeCount();

    // TODO to remove
    // @Nonnull
    // TypeConverter getTypeConverter();

    /**
     * Reset the number of changes associated with the current transaction. Should be called every time a transaction is
     * {@link Database#commit() committed} or {@link Database#rollback() rolled-back}.
     */
    void resetChangeCount();

    /**
     * Close the session and release any resources.
     *
     * @throws CleanupFailureDataAccessException
     *             if there was a problem closing the underlying database connection.
     */
    void close();

}
