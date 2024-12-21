package com.pmi.tpd.database.liquibase;

import static com.google.common.collect.Collections2.transform;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.liquibase.backup.LiquibaseUnsupportedDatabaseException;

import liquibase.change.custom.CustomChange;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

/**
 * Utilities for working with Liquibase.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class LiquibaseUtils {

    /** */
    private static final Function<BeanDefinition, Class<?>> BD_TO_CLASS = definition -> {
        try {
            return Class.forName(definition.getBeanClassName());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Bean class was missing from the class path", e);
        }
    };

    private LiquibaseUtils() {
        throw new UnsupportedOperationException("Static utility class - not for instantiation");
    }

    /**
     * Searches the class path for classes detailing custom changes to the database.
     *
     * @return an {@link Iterable} of classes detailing custom changes
     */
    public static Set<Class<?>> findCustomChanges(final String basePackage) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        provider.addIncludeFilter(new AssignableTypeFilter(CustomChange.class));

        return ImmutableSet.copyOf(transform(provider.findCandidateComponents(basePackage), BD_TO_CLASS));
    }

    /**
     * Extracts the underlying JDBC {@code Connection} that is backing the provided Liquibase {@code Database}.
     *
     * @param database
     *                 the {@code Database} representation in Liquibase
     * @return the {@code Connection} for the database
     */
    public static Connection getConnection(final Database database) {
        return ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
    }

    /**
     * Finds a Liquibase database that supports the given data source. Caller is responsible to close it.
     *
     * @param dataSource
     *                   the data source that we want to interact with
     * @return a supported database
     * @throws IllegalStateException
     *                                               if the data source provides a null connection
     * @throws CannotGetJdbcConnectionException
     *                                               if a JDBC connection could not be obtained
     * @throws LiquibaseUnsupportedDatabaseException
     *                                               if Liquibase recognises the database, but does not support it
     * @throws LiquibaseDataAccessException
     *                                               if Liquibase couldn't create a database
     */
    public static Database findDatabase(@Nonnull final DataSource dataSource) {
        checkNotNull(dataSource, "datasource");

        final Database database = findDatabaseForConnection(getConnection(dataSource));
        if (database instanceof UnsupportedDatabase) {
            throw new LiquibaseUnsupportedDatabaseException(database.getDatabaseProductName());
        }
        return database;
    }

    /**
     * Finds the relevant database implementation for the given JDBC connection, catching any database exception and
     * re-throwing it as a runtime exception. Caller is responsible to close it.
     *
     * @return can return an instance of {@link liquibase.database.core.UnsupportedDatabase}. See
     *         {@link #findDatabase(javax.sql.DataSource)}.
     * @throws LiquibaseDataAccessException
     *                                      if it fails to find a database implementation
     */
    public static Database findDatabaseForConnection(@Nonnull final Connection connection) {
        checkNotNull(connection, "connection");

        try {
            return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        } catch (final DatabaseException e) {
            throw new LiquibaseDataAccessException("Failed to find a database implementation", e);
        }
    }

    /**
     * Constructs a Spring {@code JdbcTemplate} which can be used to perform JDBC operations against the provided
     * Liquibase {@code Database}.
     *
     * @param database
     *                 the {@code Database} representation in Liquibase
     * @return a new {@code JdbcTemplate} for the database
     */
    public static JdbcTemplate getJdbcTemplate(final Database database) {
        final DataSource dataSource = new SingleConnectionDataSource(getConnection(database), true);

        return new JdbcTemplate(dataSource);
    }

    /**
     * Provides a SEPARATE (non spring managed) non-null connection, or dies trying. This is important when used with
     * Liquibase's LockService as it commits any existing transaction.
     */
    private static Connection getConnection(final DataSource dataSource) {
        try {
            final Connection connection = dataSource.getConnection();
            if (connection == null) {
                throw new IllegalStateException("Data source produced null connection");
            }
            return connection;
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException("JDBC connection could not be obtained for use by Liquibase DAO",
                    e);
        }
    }
}
