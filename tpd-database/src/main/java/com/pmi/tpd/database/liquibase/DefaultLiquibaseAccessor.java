package com.pmi.tpd.database.liquibase;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.find;
import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.FluentIterable.from;
import static com.pmi.tpd.database.liquibase.DefaultLiquibaseSession.TO_LOWERCASE_COLUMN_NAME;
import static com.pmi.tpd.database.liquibase.DefaultLiquibaseSession.TO_LOWERCASE_TABLE_NAME;
import static java.util.Optional.empty;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataRetrievalFailureException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseChangeExecutionException;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseTables;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultLiquibaseAccessor implements ILiquibaseAccessor {

    /** */
    public static final DatabaseChangeLog EMPTY_CHANGE_LOG = new DatabaseChangeLog();

    /** */
    public static final List<SqlVisitor> NO_VISITORS = ImmutableList.of();

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLiquibaseAccessor.class);

    /** */
    // Hibernate temp tables should start with ht_, but a bug (HHH-9290) in
    // Oracle8iDialect.generateTemporaryTableName truncates the 'h' when
    // the table name is longer than 30 characters
    // TODO to verify
    // tableName.startsWith("ht_") || tableName.length() == 29 && tableName.startsWith("t_");
    private static final Predicate<String> IS_HIBERNATE_TEMPORARY_TABLE = tableName -> tableName.startsWith("ht_")
            || tableName.length() == 29;

    /**
     * A function that releases the lock on a Liquibase service passed to it. Exceptions are logged as errors, but
     * otherwise silently succeed.
     */
    private static final Consumer<LockService> RELEASE_LOCK = lockService -> {
        try {
            lockService.releaseLock();
        } catch (final LockException e) {
            LOGGER.error("Failed to release Liquibase lock", e);
        }
    };

    /**
     * Encapsulating a connection to the underlying database.
     * <p>
     * It will continue to use the same database connection until its {@link ThreadLocal#remove} method is called. The
     * next {@link ThreadLocal#get} or {@link ILiquibaseSession} method call following a call to {@code remove()} will
     * obtain a new database connection.
     */
    private final ILiquibaseSession databaseSession;

    /**
     * The number of inserts within a change set after which a commit will performed. If the value of commitBlockSize is
     * less than one, then changes will not be committed until the end of the change set.
     */
    private final long commitBlockSize;

    /**
     * A None value indicates that the underlying Liquibase instance is not locked. A Some(x) value is used in the
     * {@link #unlock()} method to unlock the underlying Liquibase instance.
     */
    private Optional<LockService> lockService = empty();

    /**
     * A number of records to get in memory in one go during backup. Note: JDBC setFetchSize() is a hint to the driver
     * and behaviour is implementation specific. Recent PostgreSQL, MySQL and Oracle drivers support it natively. SQL
     * Server reportedly doesn't support fetch size, but jTDS driver for SQL Server does
     */
    private static final int FETCH_SIZE = 1000;

    /** */
    private ISchemaCreator schemaCreator;

    private IDatabaseTables databaseTables;

    /** */
    private String customChangePackageBase;

    @Inject
    public DefaultLiquibaseAccessor(final ISchemaCreator schemaCreator, final IDatabaseTables databaseTables,
            final DataSource dataSource, final long commitBlockSize,
            @Named("customChangePackageBase") final String customChangePackageBase) {
        this.commitBlockSize = commitBlockSize;
        this.databaseSession = new DefaultLiquibaseSession(dataSource);
        this.schemaCreator = schemaCreator;
        this.databaseTables = databaseTables;
        this.customChangePackageBase = checkHasText(customChangePackageBase, "customChangePackageBase");
    }

    /**
     * @param database
     */
    public DefaultLiquibaseAccessor(final Database database, final long commitBlockSize) {
        this.commitBlockSize = commitBlockSize;
        this.databaseSession = new DefaultLiquibaseSession(database);
    }

    public ILiquibaseSession getDatabaseSession() {
        return databaseSession;
    }

    @Override
    public void beginChangeSet() {
        if (databaseSession.getDatabase().supportsDDLInTransaction()) {
            try {
                // use transactions
                databaseSession.getDatabase().setAutoCommit(false);
            } catch (final DatabaseException e) {
                throw new LiquibaseDataAccessException("Failed to set auto-commit off", databaseSession.getDatabase(),
                        e);
            }
        }
    }

    @Override
    public void close() {
        unlock();
        // close the session to release any resources
        databaseSession.close();
    }

    @Override
    public long countRows(@Nonnull final String tableName) {
        Assert.checkNotNull(tableName, "tableName");
        final Database db = databaseSession.getDatabase();
        final String escapedTableName = db.escapeTableName(null, null, tableName);
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            final Connection connection = LiquibaseUtils.getConnection(databaseSession.getDatabase());
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT count(1) FROM " + escapedTableName);
            resultSet.next();
            return resultSet.getLong(1);
        } catch (final SQLException e) {
            throw new DataRetrievalFailureException("Could not count rows for " + tableName, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }
    }

    @Override
    public void createSchema(final DataSource dataSource) throws LiquibaseException {
        if (schemaCreator == null) {
            throw new UnsupportedOperationException();
        }
        schemaCreator.createSchema(dataSource);
    }

    /**
     * This implementation always commits the changes to the underlying database.
     */
    @Override
    public void deleteAllRows(@Nonnull final String tableName) {
        final DeleteDataChange change = new DeleteDataChange();
        change.setTableName(tableName);
        applyChange(change);
        commit();
    }

    @Override
    public void delete(@Nonnull final DeleteDataChange change) {
        applyChange(change);
        commit();
    }

    @Override
    public void endChangeSet() {
        commit();
    }

    @Override
    public Set<Class<?>> findCustomChanges() {
        return LiquibaseUtils.findCustomChanges(customChangePackageBase);
    }

    @Override
    public long forEachRow(@Nonnull final String tableName,
        @Nullable final String orderingColumn,
        @Nonnull final ICancelState cancelState,
        @Nonnull final Consumer<Map<String, Object>> effect) {
        Preconditions.checkNotNull(tableName, "tableName");
        Preconditions.checkArgument(orderingColumn == null || StringUtils.isNotBlank(orderingColumn),
            "blank ordering column");
        Preconditions.checkNotNull(effect, "effect");

        final Table table = findTable(tableName);
        final String escapedTableName = databaseSession.getDatabase().escapeTableName(null, null, tableName);
        long numberOfRows = 0;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            final Connection connection = LiquibaseUtils.getConnection(databaseSession.getDatabase());
            statement = connection.createStatement();
            statement.setFetchSize(FETCH_SIZE);
            resultSet = statement
                    .executeQuery("SELECT * FROM " + escapedTableName + orderByClause(table, orderingColumn));
            while (resultSet.next() && !cancelState.isCanceled()) {
                forCurrentRow(databaseSession.getDatabase(), table, resultSet, effect);
                numberOfRows++;
                if (numberOfRows % 10000 == 0) {
                    LOGGER.trace("{}: {} rows processed", tableName, numberOfRows);
                }
            }
            return numberOfRows;
        } catch (final SQLException | IOException e) {
            throw new DataRetrievalFailureException("Could not fetch rows from " + tableName, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }
    }

    public long forEachRow(@Nonnull final String query,
        @Nonnull final ICancelState cancelState,
        @Nonnull final Consumer<Map<String, Object>> effect) {
        Preconditions.checkNotNull(query, "query");
        Preconditions.checkNotNull(effect, "effect");

        long numberOfRows = 0;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            final Connection connection = LiquibaseUtils.getConnection(getDatabaseSession().getDatabase());
            statement = connection.createStatement();
            statement.setFetchSize(100);
            resultSet = statement.executeQuery(query);
            while (resultSet.next() && !cancelState.isCanceled()) {
                forCurrentRow(resultSet, effect);
                numberOfRows++;
            }
            return numberOfRows;
        } catch (final SQLException | IOException e) {
            throw new DataRetrievalFailureException("Could not fetch rows from query" + query, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }
    }

    private void forCurrentRow(final ResultSet resultSet, final Consumer<Map<String, Object>> effect)
            throws SQLException, IOException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        final Map<String, Object> columnValues = Maps.newHashMapWithExpectedSize(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            final String columnName = getColumnName(metaData, i);
            columnValues.put(columnName, resultSet.getObject(i));
        }
        effect.accept(columnValues);
    }

    @Override
    @Nonnull
    public Iterable<String> getColumnNames(@Nonnull final String tableName) {
        return from(findTable(tableName).getColumns()).transform(TO_LOWERCASE_COLUMN_NAME).toSet();
    }

    @Override
    @Nonnull
    public String getDatabaseType() {
        return databaseSession.getDatabase().getShortName();
    }

    @Override
    @Nonnull
    public Iterable<String> getTableNames() {
        return from(databaseSession.getSnapshot().get(Table.class)).transform(TO_LOWERCASE_TABLE_NAME)
                .filter(not(IS_HIBERNATE_TEMPORARY_TABLE))
                .toList();
    }

    @Override
    public IDatabaseTables getDatabaseTables() {
        return this.databaseTables;
    }

    /**
     * This implementation commits the change to the underlying database if the number of uncommitted changes has
     * reached the limit specified in the constructor.
     */
    @Override
    public void insert(@Nonnull final InsertDataChange change) {
        final List<ColumnConfig> columns = change.getColumns();

        // applyChange(change);
        final Database database = databaseSession.getDatabase();
        final StringBuilder builder = new StringBuilder("insert into ")
                .append(
                    database.escapeTableName(change.getCatalogName(), change.getSchemaName(), change.getTableName()))
                .append(" (");

        final StringBuilder questions = new StringBuilder();
        for (final ColumnConfig column : columns) {
            if (questions.length() > 0) {
                builder.append(", ");
                questions.append(", ");
            }
            builder.append(database.escapeColumnName(change.getCatalogName(),
                change.getSchemaName(),
                change.getTableName(),
                column.getName()));
            questions.append("?");
        }
        builder.append(") values (").append(questions).append(")");

        final Connection connection = LiquibaseUtils.getConnection(database);

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(builder.toString());

            for (int i = 0; i < columns.size(); ++i) {
                final ColumnConfig column = columns.get(i);
                final Object value = ColumnSerialisationType.convert(column.getValueObject());

                statement.setObject(i + 1, value);
            }
            statement.executeUpdate();
        } catch (final SQLException e) {
            throw new LiquibaseChangeExecutionException(change, e);
        } finally {
            JdbcUtils.closeStatement(statement);
        }

        commitIfBlockFilled();
    }

    @Override
    public void update(@Nonnull final UpdateDataChange change) {
        final Database database = databaseSession.getDatabase();

        final SqlStatement[] statements = change.generateStatements(database);
        try {
            database.execute(statements, Collections.<SqlVisitor> emptyList());
        } catch (final LiquibaseException e) {
            throw new LiquibaseChangeExecutionException(change, e);
        }
    }

    @Override
    public void withLock(final Consumer<ILiquibaseAccessor> effect) {
        lock();
        try {
            effect.accept(this);
        } finally {
            unlock();
        }
    }

    @VisibleForTesting
    void applyChange(final Change change) {
        try {
            // TODO to verify
            // change.init();
            change.finishInitialization();

            databaseSession.getDatabase().executeStatements(change, EMPTY_CHANGE_LOG, NO_VISITORS);
            LOGGER.debug(change.getConfirmationMessage());
            databaseSession.incrementChangeCount();
        } catch (final LiquibaseException e) {
            throw new LiquibaseChangeExecutionException(change, e);
        }
    }

    /**
     * Commits the current transaction in the underlying database, and resets the change counter.
     */
    public void commit() {
        try {
            databaseSession.getDatabase().commit();
            databaseSession.resetChangeCount();
        } catch (final DatabaseException e) {
            throw new LiquibaseDataAccessException("Failed to commit changes to database",
                    databaseSession.getDatabase(), e);
        }
    }

    /**
     * @param database
     * @return
     */
    public LockService getLockService(final Database database) {
        return LockServiceFactory.getInstance().getLockService(database);
    }

    /**
     * Rolls back the current transaction and resets the change counter.
     */
    public void rollback() {
        try {
            databaseSession.getDatabase().rollback();
            databaseSession.resetChangeCount();
        } catch (final DatabaseException e) {
            throw new LiquibaseDataAccessException("Failed to rollback changes to database",
                    databaseSession.getDatabase(), e);
        }
    }

    /**
     * Transforms column values for any database-specific mapping idiosyncrasies we need to account for.
     */
    public Object transformValue(final Database database, Object value, final int dataType, final int columnSize) {
        // map an Oracle NUMBER(1) back to a boolean
        if (database instanceof OracleDatabase && dataType == Types.DECIMAL && columnSize == 1
                && (BigDecimal.ZERO.equals(value) || BigDecimal.ONE.equals(value))) {
            value = BigDecimal.ONE.equals(value);
        }

        return value;
    }

    /**
     * A predicate that tests whether the table passed to it has a given name, ignoring upper/lower case differences.
     *
     * @param tableName
     *                  the name to compare actual table names against
     * @return a new predicate
     */
    private static Predicate<Table> byTableName(final String tableName) {
        // This implementation returns true if the table has the the expected name; false otherwise.
        return table -> table.getName().equalsIgnoreCase(tableName);
    }

    /**
     * Commit only if the number of changes is equal to the commit block size.
     */
    private void commitIfBlockFilled() {
        if (commitBlockSize > 0 && databaseSession.getChangeCount() >= commitBlockSize) {
            commit();
        }
    }

    /**
     * Finds the given Liquibase table in the database snapshot.
     *
     * @param tableName
     *                  the name of the table to find
     * @return a Liquibase table
     * @throws NoSuchElementException
     *                                if the table is not found
     */
    public Table findTable(final String tableName) {
        try {
            return find(databaseSession.getSnapshot().get(Table.class), byTableName(tableName));
        } catch (final NoSuchElementException e) {
            throw new NoSuchElementException(String.format("No table '%s' in database snapshot", tableName));
        }
    }

    /**
     * Applies the given effect to the current row in the given result set, with reference to the structure of the given
     * table definition.
     *
     * @param database
     *                  the current database to use.
     * @param table
     *                  the definition of the table from which the row was fetched
     * @param resultSet
     *                  the result set containing the row to apply the effect to
     * @param effect
     *                  the effect to apply
     * @throws IOException
     */
    private void forCurrentRow(final Database database,
        final Table table,
        final ResultSet resultSet,
        final Consumer<Map<String, Object>> effect) throws SQLException, IOException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        final Map<String, Object> columnValues = Maps.newHashMapWithExpectedSize(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            final String columnName = getColumnName(metaData, i);
            columnValues.put(columnName, getValue(resultSet, i, database, table.getColumn(columnName)));
        }
        effect.accept(columnValues);
    }

    public String getColumnName(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return metaData.getColumnName(columnIndex).toLowerCase();
    }

    private Object getValue(final ResultSet resultSet, final int index, final Database database, final Column column)
            throws SQLException, IOException {
        final DataType dataType = column.getType();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Get value from column '{}':'{}', size:{}, toString: {}",
                column.getName(),
                column.getType(),
                dataType.getColumnSize(),
                column);
        }
        return transformValue(database,
            getResultSetValue(resultSet, index),
            dataType.getDataTypeId(),
            dataType.getColumnSize() != null ? dataType.getColumnSize() : -1);
    }

    private static Object getResultSetValue(final ResultSet rs, final int index) throws SQLException, IOException {
        Object obj = null;
        try {
            obj = rs.getObject(index);
        } catch (final SQLException e) {
            if (e.getMessage().equals("The conversion from char to SMALLINT is unsupported.")) {
                // issue with sqlserver jdbc 3.0
                // http://social.msdn.microsoft.com/Forums/sqlserver/en-US/2c908b45-6f75-484a-a891-5e8206f8844f/ +
                // conversion-error-in-the-jdbc-30-driver-when-accessing-metadata
                obj = rs.getString(index);
            } else {
                throw e;
            }
        }
        if (obj instanceof Blob) {
            obj = ByteStreams.toByteArray(((Blob) obj).getBinaryStream());
        } else if (obj instanceof Clob) {
            obj = CharStreams.toString(((Clob) obj).getCharacterStream());
        } else if (obj != null && obj.getClass().getName().startsWith("oracle.sql.TIMESTAMP")) {
            obj = rs.getTimestamp(index);
        } else if (obj != null && obj.getClass().getName().startsWith("oracle.sql.DATE")) {
            final String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = rs.getTimestamp(index);
            } else {
                obj = rs.getDate(index);
            }
        } else if (obj != null && obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        }
        return obj;
    }

    public String orderByClause(final Table table, final String column) {
        final Schema schema = table.getSchema();
        return column == null ? ""
                : String.format(" ORDER BY %s ASC",
                    this.databaseSession.getDatabase()
                            .escapeColumnName(schema.getCatalogName(),
                                schema.getName(),
                                table.getName(),
                                // Gets the column name with its exact letter casing (since escapeColumnName() will
                                // quote the column name depending on the name and database, requiring an exact match).
                                // For more info, see the overrides of AbstractDatabase.escapeDatabaseObject(String)
                                // for each database.
                                table.getColumn(column).getName()));
    }

    /**
     * This implementation locks the Liquibase change log table, which is updated by the restore process.
     */
    private void lock() {
        LOGGER.debug("Locking Liquibase");
        try {
            final LockService service = getLockService(databaseSession.getDatabase());
            service.waitForLock();
            lockService = Optional.of(service);
            LOGGER.debug("Liquibase locked");
        } catch (final LockException e) {
            throw new CannotAcquireLockException("Could not lock Liquibase change log", e);
        }
    }

    private void unlock() {
        LOGGER.debug("Unlocking Liquibase");
        lockService.ifPresent(RELEASE_LOCK);
        lockService = empty();
        LOGGER.debug("Liquibase unlocked");
    }

}
