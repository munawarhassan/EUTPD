package com.pmi.tpd.database.liquibase;

import java.util.Date;

import liquibase.change.ColumnConfig;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseChangeSetTestUtils {

    private LiquibaseChangeSetTestUtils() {
        // utility class
    }

    public static ChangeSet newChangeSet(final String id,
        final String author,
        final String context,
        final String dbms) {
        return new ChangeSet(id, author, false, false, "liquibase-app-tests.xml", context, dbms, false, null);
    }

    public static DeleteDataChange newDeleteChange(final String tableName) {
        final DeleteDataChange delete = new DeleteDataChange();
        delete.setTableName(tableName);
        return delete;
    }

    public static InsertDataChange newInsertChange(final String tableName, final ColumnConfig... columns) {
        final InsertDataChange insert = new InsertDataChange();
        insert.setTableName(tableName);
        for (final ColumnConfig column : columns) {
            insert.addColumn(column);
        }
        return insert;
    }

    public static ColumnConfig newColumnConfig(final String name, final String value) {
        final ColumnConfig columnConfig = new ColumnConfig().setName(name);
        columnConfig.setValue(value);
        return columnConfig;
    }

    public static ColumnConfig newColumnConfig(final String name, final boolean value) {
        return new ColumnConfig().setName(name).setValueBoolean(value);
    }

    public static ColumnConfig newColumnConfig(final String name, final Number value) {
        return new ColumnConfig().setName(name).setValueNumeric(value);
    }

    public static ColumnConfig newColumnConfig(final String name, final Date value) {
        return new ColumnConfig().setName(name).setValueDate(value);
    }

}
