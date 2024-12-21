package com.pmi.tpd.core.liquibase;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.any;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.database.liquibase.DefaultLiquibaseSession.TO_LOWERCASE_TABLE_NAME;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.support.JdbcUtils;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.core.backup.IMigrationTarget;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseSession;
import com.pmi.tpd.database.liquibase.ILiquibaseSession;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseTables;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.InsertDataChange;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.structure.core.Table;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseMigrationTarget implements IMigrationTarget {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseMigrationTarget.class);

  /** */
  public static final String TEST_TABLENAME = "APP_CS_TEST";

  /** */
  public static final String TEST_COLUMN = "TEST_COLUMN";

  /** */
  private final ILiquibaseSession session;

  /** */
  private final Connection connection;

  /** */
  private final IDatabaseTables databaseTables;

  /**
   * Create new Liquibase migration target.
   *
   * @param connection
   *                   the connection used to migrate data.
   */
  public LiquibaseMigrationTarget(@Nonnull final Connection connection, @Nonnull final IDatabaseTables databaseTables) {
    this.connection = checkNotNull(connection, "connection");
    this.databaseTables = checkNotNull(databaseTables, "databaseTables");
    this.session = new DefaultLiquibaseSession(connection);
  }

  /**
   * Close Liquibase session.
   */
  @Override
  @PreDestroy
  public void close() {
    session.close();
  }

  @Override
  public boolean hasNoClashingTables() {
    return !any(transform(session.getSnapshot().get(Table.class), TO_LOWERCASE_TABLE_NAME),
        Predicates.in(databaseTables.getTableNames()));
  }

  @Override
  public boolean hasRequiredSchemaPermissions() {
    try {
      execute(createTestTable(), insertTestRow("test"), deleteTestRow("test"), dropTestTable());
      return true;
    } catch (final LiquibaseException e) {
      LOGGER.debug("Exception thrown when checking schema permissions", e);
      // cleanup
      executeQuietly(dropTestTable());
      return false;
    }
  }

  @Override
  public boolean hasRequiredTemporaryTablePermission() {
    // Only MySQL has this extra permission which many people don't know about, but
    // which Hibernate needs
    if (session.getDatabase() instanceof MySQLDatabase) {
      try {
        final Statement statement = connection.createStatement();
        try {
          final String tablename = "HT_" + TEST_TABLENAME;
          statement.execute(String.format("CREATE TEMPORARY TABLE_NAME %s (id int)", tablename));
          statement.execute(String.format("DROP TEMPORARY TABLE_NAME %s", tablename));
        } finally {
          JdbcUtils.closeStatement(statement);
        }
      } catch (final SQLException e) {
        LOGGER.debug("Error encountered while testing for create temporary table permission", e);
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isCaseSensitive() {
    ResultSet resultSet = null;
    Statement statement = null;
    try {
      // create a table and insert a mixed-case string
      execute(createTestTable(), insertTestRow("SeNsItIvE"));

      // attempt to retrieve the string using a case insensitive query
      String query = String.format("SELECT * FROM %s WHERE %s = '%s'",
          escapeTableName(TEST_TABLENAME),
          escapeColumnName(TEST_TABLENAME, TEST_COLUMN),
          escapeString("sensitive"));
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      // if there are any results, the query must have been performed
      // case-insensitively. Fail the test.
      if (resultSet.next()) {
        return false;
      }
      resultSet.close();

      // sanity check our work by retrieving the row in a case-sensitive manner
      query = String.format("SELECT * FROM %s WHERE %s = '%s'",
          escapeTableName(TEST_TABLENAME),
          escapeColumnName(TEST_TABLENAME, TEST_COLUMN),
          escapeString("SeNsItIvE"));
      resultSet = statement.executeQuery(query);
      if (!resultSet.next()) {
        // if there are no results, something has gone horribly wrong
        throw new DataRetrievalFailureException("Failed to retrieve test string from database.");
      }
    } catch (final LiquibaseException e) {
      throw new LiquibaseDataAccessException("Create table failed", e);
    } catch (final SQLException e) {
      throw new InvalidDataAccessResourceUsageException("Case sensitivity query failed", e);
    } finally {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);

      executeQuietly(dropTestTable());
    }

    return true;
  }

  @Override
  public boolean isUtf8() {
    final Database database = session.getDatabase();

    int encodingColumn = 1;
    String sql;
    Set<String> valid;
    if (database instanceof MySQLDatabase) {
      encodingColumn = 2;
      sql = "show variables where variable_name = 'collation_database'";
      valid = ImmutableSet.of("utf8_bin");
    } else if (database instanceof OracleDatabase) {
      sql = "select value from nls_database_parameters where parameter='NLS_CHARACTERSET'";
      valid = ImmutableSet.of("AL32UTF8", "UTF8");
    } else if (database instanceof PostgresDatabase) {
      sql = "show server_encoding";
      valid = ImmutableSet.of("UTF8", "UNICODE");
    } else {
      LOGGER.warn("UTF8 support cannot be checked for the connected database");
      return true;
    }

    ResultSet resultSet = null;
    Statement statement = null;
    try {
      final Connection connection = LiquibaseUtils.getConnection(database);
      statement = connection.createStatement();
      resultSet = statement.executeQuery(sql);

      return resultSet.next() && valid.contains(resultSet.getString(encodingColumn));
    } catch (final SQLException e) {
      LOGGER.warn("Failed to confirm UTF-8 support in target database", e);

      // That we can't confirm UTF-8 support is not necessarily a problem at the
      // validate stage. At worst, the
      // migration will fail. At this point, we'll just report a warning.
      return true;
    } finally {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }
  }

  private ColumnConfig createTestColumn() {
    final ColumnConfig columnConfig = new ColumnConfig();
    columnConfig.setType("VARCHAR(255)");
    columnConfig.setName(TEST_COLUMN);
    return columnConfig;
  }

  /**
   * @return a {@link Change} that will create a table named
   *         {@link #TEST_TABLENAME}
   */
  private CreateTableChange createTestTable() {
    final CreateTableChange create = new CreateTableChange();
    create.setSchemaName(session.getDatabase().getDefaultSchemaName());
    create.setTableName(TEST_TABLENAME);
    create.addColumn(createTestColumn());
    return create;
  }

  /**
   * @param value
   *              the value corresponding to the row to delete
   * @return a {@link Change} that will delete a matching row from
   *         {@link #TEST_TABLENAME}
   */
  private DeleteDataChange deleteTestRow(final String value) {
    final DeleteDataChange delete = new DeleteDataChange();
    delete.setTableName(TEST_TABLENAME);
    final ColumnConfig column = createTestColumn();
    column.setValue(value);
    delete.setWhere(String.format("%s = '%s'", escapeColumnName(TEST_TABLENAME, TEST_COLUMN), escapeString(value)));
    return delete;
  }

  /**
   * @return a {@link Change} that will drop the table named
   *         {@link #TEST_TABLENAME}
   */
  private DropTableChange dropTestTable() {
    final DropTableChange drop = new DropTableChange();
    drop.setSchemaName(session.getDatabase().getDefaultSchemaName());
    drop.setTableName(TEST_TABLENAME);
    return drop;
  }

  private String escapeTableName(final String table) {
    return session.getDatabase()
        .escapeTableName(session.getDatabase().getDefaultCatalogName(),
            session.getDatabase().getDefaultSchemaName(),
            table);
  }

  private String escapeColumnName(final String table, final String column) {
    return session.getDatabase()
        .escapeColumnName(session.getDatabase().getDefaultCatalogName(),
            session.getDatabase().getDefaultSchemaName(),
            table,
            column);
  }

  private String escapeString(final String value) {
    return session.getDatabase().escapeStringForDatabase(value);
  }

  /**
   * @param changes
   *                changes to execute against the target database
   * @throws LiquibaseException
   *                            if a problem was encountered whilst executing a
   *                            change
   */
  private void execute(final Change... changes) throws LiquibaseException {
    for (final Change change : changes) {
      session.getDatabase()
          .executeStatements(change,
              DefaultLiquibaseAccessor.EMPTY_CHANGE_LOG,
              DefaultLiquibaseAccessor.NO_VISITORS);
    }
  }

  /**
   * Swallows any {@link LiquibaseException}s and logs them to debug. Will
   * continue to execute all changes even if an
   * earlier one fails.
   *
   * @param changes
   *                changes to execute against the target database
   */
  private void executeQuietly(final Change... changes) {
    for (final Change change : changes) {
      try {
        execute(change);
      } catch (final LiquibaseException e) {
        LOGGER.debug("Change execution failed", e);
      }
    }
  }

  /**
   * @param value
   *              the value to insert
   * @return a {@link Change} that will insert a new row into
   *         {@link #TEST_TABLENAME}
   */
  private InsertDataChange insertTestRow(final String value) {
    final InsertDataChange insert = new InsertDataChange();
    insert.setTableName(TEST_TABLENAME);
    final ColumnConfig column = createTestColumn();
    column.setValue(value);
    insert.addColumn(column);
    return insert;
  }
}
