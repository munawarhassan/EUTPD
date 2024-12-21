package com.pmi.tpd.database.liquibase;

import com.google.common.base.Charsets;

/**
 * A bunch of constants related to Liquibase, particularly its XML serialization format.
 * <p>
 * This is a utility class, and not designed to be implemented.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class LiquibaseConstants {

    /** */
    public static final String DATABASE_CHANGE_LOG = "databaseChangeLog";

    /** */
    public static final String CHANGE_SET = "changeSet";

    /** */
    public static final String INSERT = "insert";

    /** */
    public static final String TABLE_NAME = "tableName";

    /** */
    public static final String DELETE = "delete";

    /** */
    public static final String COLUMN = "column";

    /** */
    public static final String COLUMN_NAME = "name";

    /** */
    public static final String COLUMN_TYPE = "colType";

    /** */
    public static final String CHANGE_SET_ID = "id";

    /** */
    public static final String CHANGE_SET_AUTHOR = "author";

    /** */
    public static final String CHANGE_SET_CONTEXT = "context";

    /** */
    public static final String CHANGE_SET_DBMS = "dbms";

    /** */
    public static final String WHERE = "where";

    /** encoding used for all the serialization/deserialization. */
    public static final String ENCODING = Charsets.UTF_8.name();

    /** Prevents instantiation. */
    private LiquibaseConstants() {
        throw new java.lang.UnsupportedOperationException("Attempt to instantiate a utility class");
    }
}
