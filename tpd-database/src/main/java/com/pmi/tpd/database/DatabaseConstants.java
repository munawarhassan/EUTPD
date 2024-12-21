package com.pmi.tpd.database;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DatabaseConstants {

    /**
     * The property name for the driver class to use to connect to the database.
     */
    public static final String PROP_JDBC_DRIVER = "database.jdbc.driverClassName";

    /**
     * The property name for the URL to use to connect to the database.
     */
    public static final String PROP_JDBC_URL = "database.jdbc.url";

    /**
     * The property name for the username to use to connect to the database.
     */
    public static final String PROP_JDBC_USER = "database.jdbc.username";

    /**
     * The property name for the password to use to connect to the database.
     */
    public static final String PROP_JDBC_PASSWORD = "database.jdbc.password";

    /**
     * Handy set of JDBC properties.
     *
     * @since 3.8
     */
    @SuppressWarnings("null")
    @Nonnull
    public static final Set<String> PROPS_JDBC = ImmutableSet.of(DatabaseConstants.PROP_JDBC_DRIVER,
        DatabaseConstants.PROP_JDBC_URL,
        DatabaseConstants.PROP_JDBC_USER,
        DatabaseConstants.PROP_JDBC_PASSWORD);

    private DatabaseConstants() {
        throw new UnsupportedOperationException("Cannot instantiate " + getClass().getName());
    }
}
