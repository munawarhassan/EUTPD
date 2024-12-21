package com.pmi.tpd.core.liquibase;

import static java.util.Optional.of;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmi.tpd.core.liquibase.upgrade.CustomChangePackage;
import com.pmi.tpd.database.liquibase.LiquibaseUtils;
import com.pmi.tpd.testing.junit5.TestCase;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddLookupTableChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.MergeColumnChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RawSQLChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.change.core.RenameTableChange;
import liquibase.change.core.RenameViewChange;
import liquibase.change.core.TagDatabaseChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

/**
 * This class contains one test that looks for violations of the following conventions for Liquibase change logs:
 * <ul>
 * <li>Database identifiers must be no longer than 30 characters.</li>
 * <li>Default values, if any, of nullable columns must be null</li>
 * </ul>
 * <p>
 * In fact, the above rules should be regarded as absolute constraints, since we don't want them ever to be violated; we
 * label them 'conventions' simply because Liquibase itself does not enforce them.
 */
public class ChangeLogConventionTest extends TestCase {

    private static final int MAX_IDENTIFIER_LENGTH = 30;

    private static final String CHANGE_LOG_FILE = "liquibase/master.xml";

    private static final Pattern PATTERN_NUMBER = Pattern
            .compile("\\s*NUMBER\\s*\\(\\s*(\\d*)\\s*(,\\s*\\d+\\s*)?\\)\\s*");

    private static final Pattern PATTERN_NUMERIC = Pattern
            .compile("\\s*NUMERIC\\s*\\(\\s*(\\d*)\\s*(,\\s*\\d+\\s*)?\\)\\s*");

    private static final Pattern PATTERN_DECIMAL = Pattern
            .compile("\\s*DECIMAL\\s*\\(\\s*(\\d*)\\s*(,\\s*\\d+\\s*)?\\)\\s*");

    private static final Set<String> SUPPORTED_DATABASES = ImmutableSet
            .of("h2", "hsqldb", "mysql", "oracle", "mssql", "postgresql");

    private static DatabaseChangeLog CHANGE_LOG;

    private final Set<Class<?>> foundCustomChanges = LiquibaseUtils
            .findCustomChanges(CustomChangePackage.class.getPackageName());

    private final Set<Class<?>> actualCustomChanges = Sets.newHashSet();

    private final Map<String, Table> tables = Maps.newHashMap();

    @BeforeAll
    public static void setupClass() throws LiquibaseException {
        CHANGE_LOG = parseChangeLog();
    }

    @Test
    @Disabled("need to modify liquibase existing before")
    public void testChangeLogConventions() throws LiquibaseException {
        CHANGE_LOG.getChangeSets().forEach(changeSet -> {
            checkDbmsSet(changeSet);
            checkChangeSet(changeSet);
        });

        SUPPORTED_DATABASES.forEach(database -> {
            CHANGE_LOG.getChangeSets()
                    .stream()
                    .filter(changeSet -> changeSet.getDbmsSet() == null || changeSet.getDbmsSet().contains(database))
                    .forEach(this::applyChangeSet);
            checkTables();
            checkFoundCustomChanges();
            resetTables();
        });
    }

    private void checkDbmsSet(final ChangeSet changeSet) {
        if (changeSet.getDbmsSet() != null) {
            final Set<String> unsupported = Sets.difference(changeSet.getDbmsSet(), SUPPORTED_DATABASES);
            assertTrue(unsupported.isEmpty(),
                "Found unsupported DBMS on changeset" + changeSet.getId() + ": " + unsupported);
        }
    }

    private void checkFoundCustomChanges() {
        for (final Class<?> customChange : actualCustomChanges) {
            assertTrue(foundCustomChanges.contains(customChange),
                "Custom change missing from set of found changes: " + customChange.getName());
        }
        for (final Class<?> customChange : foundCustomChanges) {
            assertTrue(actualCustomChanges.contains(customChange),
                "Invalid custom change in set of found changes: " + customChange.getName());
        }
    }

    private void checkTables() {
        for (final Table table : tables.values()) {
            for (final Column column : table.getColumns()) {
                if (!ColumnType.isKnownDataType(column.getType().getTypeName())) {
                    fail("Column " + table.getName() + "." + column.getName() + " has type "
                            + column.getType().getTypeName() + ", which is not one of the allowed types"
                            + " as specified in the " + ColumnType.class.getName() + " enum.");
                }
            }
            checkOra24816(table);
        }
    }

    private void resetTables() {
        tables.clear();
    }

    private void applyChange(final Change change) {
        if (change instanceof CreateTableChange) {
            final CreateTableChange createTableChange = (CreateTableChange) change;
            final Table table = new Table(null, null, createTableChange.getTableName());
            for (final ColumnConfig columnConfig : createTableChange.getColumns()) {
                table.getColumns().add(createColumn(table, columnConfig));
            }
            tables.put(table.getName(), table);
        } else if (change instanceof AddColumnChange) {
            final AddColumnChange addColumnChange = (AddColumnChange) change;
            final String tableName = addColumnChange.getTableName();
            final Table table = tables.get(tableName);
            for (final ColumnConfig columnConfig : addColumnChange.getColumns()) {
                table.getColumns().add(createColumn(table, columnConfig));
            }
        } else if (change instanceof RenameColumnChange) {
            final RenameColumnChange renameColumnChange = (RenameColumnChange) change;
            final String tableName = renameColumnChange.getTableName();
            final Table table = tables.get(tableName);
            final Column column = table.getColumn(renameColumnChange.getOldColumnName());
            column.setName(renameColumnChange.getNewColumnName());
        } else if (change instanceof ModifyDataTypeChange) {
            final ModifyDataTypeChange modifyDataTypeChange = (ModifyDataTypeChange) change;
            final String tableName = modifyDataTypeChange.getTableName();
            final Table table = tables.get(tableName);
            final Column column = table.getColumn(modifyDataTypeChange.getColumnName());
            column.setType(new DataType(modifyDataTypeChange.getNewDataType()));
        } else if (change instanceof DropColumnChange) {
            final DropColumnChange dropColumnChange = (DropColumnChange) change;
            final Table table = tables.get(dropColumnChange.getTableName());
            final int index = indexOf(table.getColumns(),
                column -> column.getName().equals(dropColumnChange.getColumnName()));
            if (index == -1) {
                throw new RuntimeException("Attempt to drop non-existent column " + dropColumnChange.getColumnName()
                        + " from " + dropColumnChange.getTableName() + " table");
            }
            table.getColumns().remove(index);
        } else if (change instanceof RenameTableChange) {
            final RenameTableChange renameTableChange = (RenameTableChange) change;
            final String tableName = renameTableChange.getOldTableName();
            final Table oldTable = tables.get(tableName);
            final Table newTable = new Table(null, null, renameTableChange.getNewTableName());
            newTable.getColumns().addAll(oldTable.getColumns());
            tables.remove(tableName);
            tables.put(newTable.getName(), newTable);
        } else if (change instanceof DropTableChange) {
            final DropTableChange dropTableChange = (DropTableChange) change;
            if (tables.remove(dropTableChange.getTableName()) == null) {
                throw new RuntimeException("Attempt to drop non-existent table " + dropTableChange.getTableName());
            }
        } else if (change instanceof AddForeignKeyConstraintChange) {
            // does not affect data types
        } else if (change instanceof AddNotNullConstraintChange) {
            // does not affect data types
        } else if (change instanceof AddPrimaryKeyChange) {
            // does not affect data types
        } else if (change instanceof AddUniqueConstraintChange) {
            // does not affect data types
        } else if (change instanceof CreateIndexChange) {
            // does not affect data types
        } else if (change instanceof CustomChangeWrapper) {
            // We can't possibly know what this does, but most likely it's not adjusting
            // data types
        } else if (change instanceof DropForeignKeyConstraintChange) {
            // does not affect data types
        } else if (change instanceof DropIndexChange) {
            // does not affect data types
        } else if (change instanceof DropPrimaryKeyChange) {
            // does not affect data types
        } else if (change instanceof DropUniqueConstraintChange) {
            // does not affect data types
        } else if (change instanceof InsertDataChange) {
            // does not affect data types
        } else if (change instanceof RawSQLChange) {
            // We can't possibly know what this does, but most likely it's not adjusting
            // data types
        } else if (change instanceof TagDatabaseChange) {
            // does not affect data types
        } else if (change instanceof UpdateDataChange) {
            // does not affect data types
        } else if (change instanceof DeleteDataChange) {
            // does not affect data types
        } else {
            fail("Encountered unauthorised change of type " + change.getClass().getName());
        }
    }

    private void applyChangeSet(final ChangeSet changeSet) {
        for (final Change change : changeSet.getChanges()) {
            applyChange(change);
        }
    }

    private Column createColumn(final Table table, final ColumnConfig columnConfig) {
        final Column column = new Column();
        column.setName(columnConfig.getName());
        column.setType(new DataType(columnConfig.getType()));
        column.setNullable(
            columnConfig.getConstraints() == null || of(columnConfig.getConstraints().isNullable()).orElse(false));
        column.setDefaultValue(columnConfig.getDefaultValueObject());
        final ConstraintsConfig constraints = columnConfig.getConstraints();
        if (constraints != null) {
            if (constraints.isPrimaryKey()) {
                table.setPrimaryKey(new PrimaryKey("pk_" + table.getName() + "_" + column.getName(), null, null,
                        table.getName(), column));
            }
            if (constraints.isUnique()) {
                table.getUniqueConstraints()
                        .add(new UniqueConstraint("u_" + table.getName() + "_" + column.getName(), null, null,
                                table.getName(), column));
            }
        }
        return column;
    }

    private void checkChange(final Change change) {
        if (change instanceof CreateTableChange) {
            checkIdentifierLength("table name", ((CreateTableChange) change).getTableName(), change);
            checkColumns(change, ((CreateTableChange) change).getColumns());
        } else if (change instanceof CreateViewChange) {
            checkIdentifierLength("view name", ((CreateViewChange) change).getViewName(), change);
        } else if (change instanceof AddPrimaryKeyChange) {
            checkIdentifierLength("constraint name", ((AddPrimaryKeyChange) change).getConstraintName(), change);
        } else if (change instanceof AddColumnChange) {
            checkColumns(change, ((AddColumnChange) change).getColumns());
        } else if (change instanceof TagDatabaseChange) {
            checkIdentifierLength("tag", ((TagDatabaseChange) change).getTag(), change);
        } else if (change instanceof CreateIndexChange) {
            checkIdentifierLength("index name", ((CreateIndexChange) change).getIndexName(), change);
        } else if (change instanceof AddForeignKeyConstraintChange) {
            checkIdentifierLength("foreign key name",
                ((AddForeignKeyConstraintChange) change).getConstraintName(),
                change);
        } else if (change instanceof AddUniqueConstraintChange) {
            checkIdentifierLength("unique constraint name",
                ((AddUniqueConstraintChange) change).getConstraintName(),
                change);
        } else if (change instanceof AddLookupTableChange) {
            final AddLookupTableChange addLookupTableChange = (AddLookupTableChange) change;
            checkIdentifierLength("final constraint name", addLookupTableChange.getFinalConstraintName(), change);
            checkIdentifierLength("new table name", addLookupTableChange.getNewTableName(), change);
            checkIdentifierLength("new column name", addLookupTableChange.getNewColumnName(), change);
        } else if (change instanceof RenameColumnChange) {
            checkIdentifierLength("new column name", ((RenameColumnChange) change).getNewColumnName(), change);
        } else if (change instanceof RenameTableChange) {
            checkIdentifierLength("new table name", ((RenameTableChange) change).getNewTableName(), change);
        } else if (change instanceof RenameViewChange) {
            checkIdentifierLength("new view name", ((RenameViewChange) change).getNewViewName(), change);
        } else if (change instanceof MergeColumnChange) {
            checkIdentifierLength("final column name", ((MergeColumnChange) change).getFinalColumnName(), change);
        } else if (change instanceof CustomChangeWrapper) {
            actualCustomChanges.add(((CustomChangeWrapper) change).getCustomChange().getClass());
        }
    }

    private void checkChangeSet(final ChangeSet changeSet) {
        for (final Change change : changeSet.getChanges()) {
            checkChange(change);
        }
    }

    private static void checkColumn(final Change change, final ColumnConfig column) {
        checkIdentifierLength("column name", column.getName(), change);
        checkColumnConstraints(change, column.getConstraints());
        if (isColumnNullable(column)) {
            checkColumnForNonNullDefaultValue(change, column);
        }
        checkDataType(change, column, PATTERN_NUMBER);
        checkDataType(change, column, PATTERN_NUMERIC);
        checkDataType(change, column, PATTERN_DECIMAL);
    }

    private static void checkColumnForNonNullDefaultValue(final Change change, final ColumnConfig column) {
        if (!column.hasDefaultValue()) {
            return;
        }
        MatcherAssert.assertThat(
            "Default value of nullable column " + column.getName() + " of " + change.getChangeSet().getId()
                    + " in change set " + change.getChangeSet().getId(),
            column.getDefaultValueObject(),
            nullValue());
    }

    private static void checkColumnConstraints(final Change change, final ConstraintsConfig constraints) {
        if (constraints == null) {
            return;
        }
        checkIdentifierLength("primary key name", constraints.getPrimaryKeyName(), change);
        checkIdentifierLength("foreign key name", constraints.getForeignKeyName(), change);
        checkIdentifierLength("unique constraint name", constraints.getUniqueConstraintName(), change);
    }

    private void checkColumns(final Change change, final List<? extends ColumnConfig> columns) {
        for (final ColumnConfig column : columns) {
            checkColumn(change, column);
        }
    }

    /**
     * Checks the given column's data type against a pattern that would be interpreted as a BOOLEAN in Oracle. Such data
     * types are not allowed.
     *
     * @param change
     *            the change in which the column configuration is specified
     * @param column
     *            the column that must have a data type that does not match the pattern
     * @param pattern
     *            if the data type matches this pattern, the test fails
     */
    private static void checkDataType(final Change change, final ColumnConfig column, final Pattern pattern) {
        final String dataType = column.getType();
        final Matcher m = pattern.matcher(dataType);

        if (m.matches()) {
            final String precisionString = m.group(1);

            final String scaleString = m.group(2);
            final boolean zeroScale = scaleString == null || scaleString.substring(1).trim().equals("0");

            if (zeroScale && precisionString.equals("1")) {
                fail("Column " + column.getName() + " of " + change.getChangeSet().getId() + " in change set "
                        + change.getChangeSet().getId() + " has type " + dataType
                        + ", which would be interpreted as a BOOLEAN in Oracle."
                        + " For more information, see the CustomOracleTypeConverter class.");
            }
        }
    }

    private static void checkIdentifierLength(final String identifierName,
        final String identifier,
        final Change change) {
        if (identifier == null) {
            return;
        }
        MatcherAssert.assertThat(
            "Length of " + identifierName + " (" + identifier + ") of " + change.getChangeSet().getId()
                    + " in change set " + change.getChangeSet().getId() + " is > " + MAX_IDENTIFIER_LENGTH,
            identifier.length(),
            lessThanOrEqualTo(MAX_IDENTIFIER_LENGTH));
    }

    /**
     * This method attempts to reduce the probability of us encountering Oracle error
     * <a href="http://ora-24816.ora-code.com/">ORA-24816</a>, which occurs if a VARCHAR(4000) column is bound in a
     * database operation <b>after</b> a LOB.
     * <p>
     * We can't check for all SQL statements in the application, especially since the are buried in Hibernate, but we
     * can at least ensure that LOB's and VARCHAR(4000) columns don't coexist in tables.
     */
    private static void checkOra24816(final Table table) {
        final Optional<Column> lob = table.getColumns()
                .stream()
                .filter(column -> column.getType().getTypeName().equalsIgnoreCase("CLOB")
                        || column.getType().getTypeName().equalsIgnoreCase("BLOB"))
                .findFirst();

        final Optional<Column> varchar4000 = table.getColumns()
                .stream()
                .filter(column -> column.getType().getTypeName().equalsIgnoreCase("VARCHAR(4000)")
                        || column.getType().getTypeName().equalsIgnoreCase("NVARCHAR(4000)"))
                .findFirst();

        if (lob.isPresent() && varchar4000.isPresent()) {
            fail("The " + table.getName() + " table"
                    + " has both a VARCHAR(4000) column and a LOB column, which can cause an error in Oracle."
                    + " For more information, see http://ora-24816.ora-code.com/.");
        }
    }

    private static <T> int indexOf(final List<T> list, final Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Because the {@link liquibase.change.ColumnConfig#isNullable()} method is faulty
     */
    private static boolean isColumnNullable(final ColumnConfig column) {
        return column.getConstraints() == null || column.getConstraints().isNullable() == Boolean.TRUE;
    }

    private static DatabaseChangeLog parseChangeLog() throws LiquibaseException {
        final ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        final ChangeLogParser parser = ChangeLogParserFactory.getInstance()
                .getParser(CHANGE_LOG_FILE, resourceAccessor);
        return parser.parse(CHANGE_LOG_FILE, new ChangeLogParameters(), resourceAccessor);
    }
}
