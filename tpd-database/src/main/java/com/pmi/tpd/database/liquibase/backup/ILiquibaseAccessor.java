package com.pmi.tpd.database.liquibase.backup;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.database.spi.IDatabaseTables;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.exception.LiquibaseException;

/**
 * An interface for types that interact with Liquibase.
 * <p>
 * This interface does not incorporate methods for explicit commits to underlying database transactions, implementing
 * classes must manage their own schemes for committing database updates.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseAccessor extends Closeable {

    /**
     * Counts the number of rows in the given table.
     *
     * @param tableName
     *                  the name of the table to count
     * @return the count of the number of rows
     * @since 2.6
     */
    long countRows(@Nonnull String tableName);

    /**
     * Provides the names of all tables in the database, lowercased. Due to the lowercasing, this method is
     * innappropriate for use when performing operations that require case-sensitive table names.
     *
     * @return a new iterable of table names. The client can mutate the result without affecting the internal state of
     *         this backup dao. The result is guaranteed not to contain null values.
     */
    @Nonnull
    Iterable<String> getTableNames();

    /**
     * @return
     */
    IDatabaseTables getDatabaseTables();

    /**
     * Provides the types of the database.
     *
     * @return a string identifying the database
     */
    @Nonnull
    String getDatabaseType();

    /**
     * Provides the names of all columns of the given table.
     *
     * @param tableName
     *                  the name of the table to which the columns belong
     * @return a new iterable of column names. The client can mutate the result without affecting the internal state of
     *         this backup dao. The result is guaranteed not to contain null values.
     * @throws java.util.NoSuchElementException
     *                                          if the table is not found in the database
     */
    @Nonnull
    Iterable<String> getColumnNames(@Nonnull String tableName);

    /**
     * Applies the given effect to each row of the given table.
     *
     * @param tableName
     *                    the name of the table containing the rows to which the effect will be applied
     * @param column
     *                    an optional column name that will be used to sort the rows of the table (in ascending order)
     * @param cancelState
     *                    the cancelState to be used to abort early if the caller decides to cancel. When the caller
     *                    cancels, this method will simply return and will not throw an exception.
     * @param effect
     *                    the effect to be applied. The effect will be applied to rows represented by maps from column
     *                    names (non-null strings) to column values (possibly-null objects).
     * @return the number of rows to which the effect was applied
     * @throws org.springframework.dao.DataRetrievalFailureException
     *                                                               if rows cannot be fetched from the underlying
     *                                                               database
     */
    long forEachRow(@Nonnull String tableName,
        @Nullable String column,
        @Nonnull ICancelState cancelState,
        @Nonnull Consumer<Map<String, Object>> effect);

    /**
     * Applies the given effect in the context of a locked Liquibase instance. Before applying the effect, any database
     * resources to which exclusive write access is required are locked; after applying the effect, any database
     * resources that were locked are unlocked.
     *
     * @throws org.springframework.dao.CannotAcquireLockException
     *                                                            if a lock could not be obtained
     */
    void withLock(Consumer<ILiquibaseAccessor> effect);

    /**
     * Releases any resources held by this liquibase dao.
     * <p>
     * If the underlying database it locked, this method unlocks it before releasing other resources.
     *
     * @throws org.springframework.dao.CleanupFailureDataAccessException
     *                                                                   if an error occurred while closing underlying
     *                                                                   resources.
     */
    @Override
    void close();

    /**
     * Indicates the start of a set of {@link #insert(InsertDataChange) insert} or {@link #deleteAllRows delete}
     * operation.
     */
    void beginChangeSet();

    /**
     * Indicates the end of a set of {@link #insert(InsertDataChange) insert} or {@link #deleteAllRows delete}
     * operation.
     */
    void endChangeSet();

    /**
     * Executes the given insert data change against the underlying database.
     *
     * @param change
     *               the insert operation to be performed
     * @throws org.springframework.dao.InvalidDataAccessResourceUsageException
     *                                                                         if the table does not exist, or if the
     *                                                                         row cannot be inserted
     */
    void insert(@Nonnull InsertDataChange change);

    /**
     * Executes the given update data change against the underlying database.
     *
     * @param change
     *               the update operation to be performed
     * @throws org.springframework.dao.InvalidDataAccessResourceUsageException
     *                                                                         if the table does not exist, or if the
     *                                                                         row cannot be updated
     */
    void update(@Nonnull UpdateDataChange change);

    /**
     * @param change
     */
    void delete(@Nonnull DeleteDataChange change);

    /**
     * Deletes the all rows of the given table.
     *
     * @param tableName
     *                  the name of the table from which to remove rows
     * @throws org.springframework.dao.InvalidDataAccessResourceUsageException
     *                                                                         if the table does not exist, or if the
     *                                                                         rows of the table cannot be deleted
     */
    void deleteAllRows(@Nonnull String tableName);

    /**
     * Creates the schema in the database targeted by the provided {@code DataSource}. This will create all of
     * application tables as well as their foreign keys, indexes, etc., but will not populate them.
     *
     * @param dataSource
     *                   the data source connected to the database to create the schema in
     * @throws LiquibaseException
     *                            if the Liquibase changesets which create the schema cannot be applied. This may happen
     *                            because of an issue with the changesets themselves, or because the settings provided
     *                            when the {@code DataSource} was created were invalid and a connection to the database
     *                            cannot be opened
     */
    void createSchema(DataSource dataSource) throws LiquibaseException;

    /**
     * Searches the class path for classes detailing custom changes to the database.
     *
     * @return an {@link Iterable} of classes detailing custom changes
     */
    Set<Class<?>> findCustomChanges();
}
