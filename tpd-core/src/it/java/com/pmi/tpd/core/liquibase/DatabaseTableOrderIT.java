package com.pmi.tpd.core.liquibase;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BINARY;
import static java.sql.Types.BIT;
import static java.sql.Types.BLOB;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.CLOB;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.LONGNVARCHAR;
import static java.sql.Types.LONGVARBINARY;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.NCHAR;
import static java.sql.Types.NCLOB;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.NVARCHAR;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARBINARY;
import static java.sql.Types.VARCHAR;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.database.DatabaseTable;
import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.core.dbunit.DatabaseConnectionFactory;

/**
 * Ensures that the {@link DatabaseTable} enum is ordered so that performing inserts in that order will not violate any
 * foreign key constraints in the database.
 */
@Configuration
@TestExecutionListeners(listeners = { DbUnitTestExecutionListener.class }, inheritListeners = true)
@ContextConfiguration(classes = { DatabaseTableOrderIT.class })
@DbUnitConfiguration(databaseConnection = "dataSource")
public class DatabaseTableOrderIT extends BaseDaoTestIT {

    private static final Timestamp SAMPLE_TIMESTAMP = new Timestamp(1338979220L);

    private static final String SAMPLE_LOB_VALUE = "value";

    private static final String SAMPLE_CHAR_VALUE = "z";

    private static final int SAMPLE_NUMBER = 1;

    @Inject
    private DatabaseConnectionFactory connectionFactory;

    @Bean
    public DatabaseConnectionFactory connectionFactory(final DataSource dataSource) {
        return new DatabaseConnectionFactory(dataSource);
    }

    /**
     * A function that produces a sample value for a given column.
     * <p>
     * Since all of the values are the same for each column type, any foreign key relationships will be satisfied,
     * providing that the table they reference has been previously restored.
     */
    private static final Function<Column, Object> VALUE_FOR_COLUMN = column -> {
        switch (column.getDataType().getSqlType()) {
            case BIT:
            case BOOLEAN:
                return Boolean.FALSE;

            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case BIGINT:
            case FLOAT:
            case REAL:
            case DOUBLE:
            case NUMERIC:
            case DECIMAL:
                return SAMPLE_NUMBER;

            case DATE:
            case TIME:
            case TIMESTAMP:
                return SAMPLE_TIMESTAMP;

            case CHAR:
            case VARCHAR:
            case LONGVARCHAR:
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case NCHAR:
            case NVARCHAR:
            case LONGNVARCHAR:
                return SAMPLE_CHAR_VALUE;

            case BLOB:
            case CLOB:
            case NCLOB:
                return SAMPLE_LOB_VALUE;

            default:
                throw new IllegalStateException(
                        "dbunit reported unsupported SQL column type (" + column.getSqlTypeName() + "')");
        }
    };

    /**
     * A function that creates a new DBUnit table having the same structure as the table of the same name in the given
     * data set, and populated with one row of sample data.
     *
     * @param dataSet
     *            the template data set specifying the structure of the table to create
     * @return a function that converts a table name to a DBUnit table
     */
    private static Function<String, ITable> toTable(final IDataSet dataSet) {
        return new Function<>() {

            @Override
            public ITable apply(final String tableName) {
                try {
                    final ITableMetaData metaData = dataSet.getTable(tableName).getTableMetaData();
                    final List<Object> row = Lists.transform(Arrays.asList(metaData.getColumns()), VALUE_FOR_COLUMN);
                    return newTable(metaData, row);
                } catch (final DataSetException e) {
                    throw new RuntimeException("Failed to create and/or populate " + tableName, e);
                }
            }

            /**
             * Creates a new DBUnit table with a structure defined by the given meta data, and containing the given row.
             */
            private ITable newTable(final ITableMetaData metaData, final List<Object> row) throws DataSetException {
                final DefaultTable table = new DefaultTable(metaData);
                addRow(table, row);
                return table;
            }

            private void addRow(final DefaultTable table, final List<Object> row) throws DataSetException {
                table.addRow(Iterables.toArray(row, Object.class));
            }
        };
    }

    /**
     * This test inserts a single row of sample data into every {@link DatabaseTable database table}.
     * <p>
     * The same value is used for every integer column, so provided the tables are ordered in a manner that doesn't
     * violate foreign key constraints, each successive insert should work. If a row is inserted into a table that has a
     * foreign key reference to a table that has not yet been populated, this test will fail.
     * <p>
     * We use the natural order of {@link DatabaseTable} as the insertion order for tables when restoring the database
     * during a migration, so if this test is failing, it probably means that our migration process is probably broken
     * too. See the documentation on {@link DatabaseTable} for more info.
     */
    @Test
    public void testDatabaseTableOrderCompatibleWithForeignKeyConstraints() throws DatabaseUnitException, SQLException {
        final IDatabaseConnection connection = connectionFactory.newConnection();
        final IDataSet template = connection.createDataSet();
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSetWithSampleData(template));
    }

    /**
     * Creates a new data set based on the given template data set, and having one row of sample data in each table.
     *
     * @param template
     *            the data set specifying the structure of tables in the returned data set
     * @return a new data set with sample data
     */
    private IDataSet dataSetWithSampleData(final IDataSet template) throws DataSetException {
        final List<ITable> tables = Lists.transform(new DefaultDatabaseTables().getTableNames(), toTable(template));

        return new DefaultDataSet(Iterables.toArray(tables, ITable.class), true);
    }
}
