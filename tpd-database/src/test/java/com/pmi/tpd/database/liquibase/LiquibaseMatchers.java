package com.pmi.tpd.database.liquibase;

import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * A bunch of Hamcrest matchers that work on Liquibase types.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseMatchers {

    /** Prevents instantiation */
    private LiquibaseMatchers() {
        throw new UnsupportedOperationException("Attempt to instantiate utility class");
    }

    public static HasTableName hasTableName(final String tableName) {
        return new HasTableName(tableName);
    }

    public static final class HasTableName extends TypeSafeMatcher<InsertDataChange> {

        private final String tableName;

        public HasTableName(final String tableName) {
            this.tableName = tableName;
        }

        @Override
        public boolean matchesSafely(final InsertDataChange insertDataChange) {
            return insertDataChange.getTableName().equals(tableName);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("has table name: ").appendValue(tableName);
        }
    }

    public static HasDbms hasDbms(final String dbms) {
        return new HasDbms(dbms);
    }

    public static final class HasDbms extends TypeSafeMatcher<ChangeSet> {

        private final String dbms;

        public HasDbms(final String dbms) {
            this.dbms = dbms;
        }

        @Override
        public boolean matchesSafely(final ChangeSet changeSet) {
            return changeSet.getDbmsSet().contains(dbms);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("has dbms");
        }
    }

    public static <V> ColumnConfigMatcher<V> columnConfig(final String columnName, final V columnValue) {
        return new ColumnConfigMatcher<>(columnName, columnValue);
    }

    public static final class ColumnConfigMatcher<V> extends TypeSafeMatcher<ColumnConfig> {

        private final String columnName;

        private final V columnValue;

        public ColumnConfigMatcher(final String columnName, final V columnValue) {
            this.columnName = columnName;
            this.columnValue = columnValue;
        }

        @Override
        public boolean matchesSafely(final ColumnConfig columnConfig) {
            if (!columnName.equals(columnConfig.getName())) {
                return false;
            }

            if (columnValue instanceof Boolean) {
                return columnValue.equals(columnConfig.getValueBoolean());
            } else if (columnValue instanceof Date) {
                return columnValue.equals(columnConfig.getValueDate());
            } else if (columnValue instanceof Number) {
                // inverse comparison, lets liquibase check it.
                return columnConfig.getValueNumeric().equals(columnValue);
            } else {
                return columnValue.equals(columnConfig.getValueObject());
            }
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("has name: ")
                    .appendValue(columnName)
                    .appendText(" and has value: ")
                    .appendValue(columnValue);
        }
    }

}
