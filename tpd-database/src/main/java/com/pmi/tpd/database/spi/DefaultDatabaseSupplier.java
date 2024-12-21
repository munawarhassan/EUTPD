package com.pmi.tpd.database.spi;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.versioning.Version;
import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.DbType;
import com.pmi.tpd.database.UnsupportedDatabaseException;

import io.atlassian.util.concurrent.LazyReference;
import io.atlassian.util.concurrent.ResettableLazyReference;

/**
 * A default implementation of {@link IDatabaseSupplier}.
 * <p>
 * This implementation is able to validate support for the following database vendors:
 * <ul>
 * <li>Derby</li>
 * <li>HSQL</li>
 * <li>Microsoft SQL Server</li>
 * <li>MySQL</li>
 * <li>Oracle</li>
 * <li>PostgreSQL</li>
 * </ul>
 * When the server starts, support for the connected database is <i>automatically verified</i> by {@link #validate()}.
 * If the connected database is not supported, an exception will be thrown which will prevent the server from starting.
 * This handles the case where, for example, the system is migrated to a MySQL 5.5 database which is later upgraded to
 * an unsupported version of MySQL 5.6.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultDatabaseSupplier implements IDatabaseAffixed, IDatabaseSupplier {

    /** name of database {@value #NAME_DERBY}. */
    public static final String NAME_DERBY = "Apache Derby";

    /** name of database {@value #NAME_HSQL}. */
    public static final String NAME_HSQL = "HSQL";

    /** name of database {@value #NAME_MYSQL}. */
    public static final String NAME_MYSQL = "MySQL";

    /** name of database {@value #NAME_ORACLE}. */
    public static final String NAME_ORACLE = "Oracle";

    /** name of database {@value #NAME_POSTGRESQL}. */
    public static final String NAME_POSTGRESQL = "PostgreSQL";

    /** name of database {@value #NAME_SQL_SERVER}. */
    public static final String NAME_SQL_SERVER = "Microsoft SQL Server";

    /** name of HSQL database engine {@value #NAME_DERBY}. */
    private static final String HSQL_PRODUCT_NAME = "HSQL Database Engine";

    /** */
    private static final Map<String, Function<IDatabase, IDetailedDatabase>> NAMES_TO_DETAILERS = ImmutableMap
            .<String, Function<IDatabase, IDetailedDatabase>> builder()
            .put(NAME_DERBY, new DerbySupportLevel())
            .put(NAME_HSQL, new HsqlSupportLevel())
            .put(NAME_SQL_SERVER, new SqlServerSupportLevel())
            .put(NAME_MYSQL, new MySqlSupportLevel())
            .put(NAME_ORACLE, new OracleSupportLevel())
            .put(NAME_POSTGRESQL, new PostgresSupportLevel())
            .build();

    /**
     * Parses the product version out of Oracle's verbose version strings.
     * <p>
     * Version strings in Oracle look like this: "Oracle Database 11g Express Edition Release 11.2.0.2.0 - Production".
     * Given this example, this pattern will parse out "11.2.0.2.0".
     */
    private static final Pattern PATTERN_ORACLE_VERSION = Pattern.compile(".+\\s([\\d\\.]+)\\s.*", Pattern.DOTALL);

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDatabaseSupplier.class);

    /** */
    private final ResettableLazyReference<IDetailedDatabase> databaseReference;

    /** */
    private boolean ignoreUnsupported;

    /**
     * Create new instance of {@link DefaultDatabaseSupplier}.
     *
     * @param dataSource
     *                   the data source used.
     */
    @Inject
    public DefaultDatabaseSupplier(final DataSource dataSource) {
        databaseReference = new ResettableLazyReference<>() {

            @Override
            protected IDetailedDatabase create() { // Implemented API allows checked exceptions; none should be thrown
                return detailsFor(dataSource);
            }
        };
    }

    @Nonnull
    @Override
    public IDetailedDatabase get() {
        try {
            // noinspection ConstantConditions
            return databaseReference.get();
        } catch (final LazyReference.InitializationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ExecutionException) {
                // The only way the cause should end up as an ExecutionException, based on InitializationException's
                // constructor, is if the ExecutionException has no cause Throwable set. As long as those semantics
                // remain, this will always set cause to null. Throwables.propagateIfPossible handles that case and
                // will not throw. That, in turn, will result in the InitializationException getting rethrown.
                //
                // This logic is here _purely_ in case the InitializationException semantics change.
                cause = cause.getCause();
            }
            Throwables.throwIfUnchecked(cause);

            // This should never happen, but if the unwrapped exception is a checked exception, just throw the
            // InitializationException after all
            throw e;
        }
    }

    @Nonnull
    @Override
    public IDetailedDatabase getForConnection(@Nonnull final Connection connection) {
        return detailsFor(databaseFor(checkNotNull(connection, "connection")));
    }

    /**
     * Resets the {@link #databaseReference database reference} to release the old database. Since the reference is
     * lazy, any thread which attempts to retrieve database details after this is called will block on the latch until
     * the new database is available (or the old database is restored).
     */
    @Override
    public void release() {
        databaseReference.reset();
    }

    /**
     * Sets the indicating whether ignore check on the unsupported database.
     *
     * @param ignoreUnsupported
     *                          sets {@code true} skip the unsupported database check, otherwise {@code false}.
     * @see #validate()
     */
    public void setIgnoreUnsupported(final boolean ignoreUnsupported) {
        this.ignoreUnsupported = ignoreUnsupported;
    }

    /**
     * Ensures the connected {@link #get() database} is not {@link DatabaseSupportLevel#UNSUPPORTED unsupported}.
     * <p>
     * Setup and migration should never allow connecting to an unsupported database. However, the database may be
     * upgraded from a supported version to an unsupported version, such as upgrading MySQL from 5.5 to 5.6, or it may
     * have been configured by directly setting JDBC values in {@code app-config.properties}.
     * <p>
     * If the connected database is unsupported, an exception will be thrown to halt startup. This service should always
     * be initialised before Liquibase or Hibernate to ensure the system does not attempt to create, migrate or validate
     * the schema.
     *
     * @throws UnsupportedDatabaseException
     *                                      if the connected database is unsupported
     */
    @PostConstruct
    public void validate() {
        if (ignoreUnsupported) {
            LOGGER.warn("Not checking database support level; the check has been disabled");
            return;
        }
        final IDetailedDatabase database = get();

        if (DatabaseSupportLevel.UNSUPPORTED == database.getSupportLevel()) {
            throw new UnsupportedDatabaseException("The configured database is unsupported", database);
        }
    }

    @Nonnull
    private static IDatabase databaseFor(final Connection connection) {
        try {
            final DatabaseMetaData metaData = connection.getMetaData();
            final String productName = metaData.getDatabaseProductName();
            final String productVersion = metaData.getDatabaseProductVersion();
            final int majorVersion = metaData.getDatabaseMajorVersion();
            final int minorVersion = metaData.getDatabaseMinorVersion();
            final Version version = parseVersion(productName, productVersion, majorVersion, minorVersion);

            // For HSQL, simplify the name from "HSQL Database Engine" to simply "HSQL"
            return new DefaultJdbcMetadataDatabase(HSQL_PRODUCT_NAME.equals(productName) ? NAME_HSQL : productName,
                    version, majorVersion, minorVersion);
        } catch (final SQLException e) {
            throw new DataRetrievalFailureException("Could not load database metadata", e);
        }
    }

    @Nonnull
    private static IDatabase databaseFor(final DataSource dataSource) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);

            return databaseFor(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Nonnull
    private static IDetailedDatabase detailsFor(final IDatabase database) {
        final Function<IDatabase, IDetailedDatabase> detailer = NAMES_TO_DETAILERS.get(database.getName());
        if (detailer == null) {
            LOGGER.error("{} is not a supported database", database.getName());

            return new DefaultDetailedDatabase(database, DatabaseSupportLevel.UNSUPPORTED, null);
        }

        return detailer.apply(database);
    }

    @Nonnull
    private static IDetailedDatabase detailsFor(final DataSource dataSource) {
        return detailsFor(databaseFor(dataSource));
    }

    private static Version parseVersion(final String productName,
        final String productVersion,
        final int majorVersion,
        final int minorVersion) {
        if (NAME_ORACLE.equals(productName)) {
            final Matcher matcher = PATTERN_ORACLE_VERSION.matcher(productVersion);
            if (matcher.matches()) {
                return new Version(matcher.group(1));
            }

            LOGGER.warn("Could not parse Oracle version [{}]; using major and minor versions", productVersion);
            return new Version(majorVersion, minorVersion);
        }

        return new Version(productVersion);
    }

    /**
     * Abstract class allowing create a {@link IDetailedDatabase} from {@link IDatabase} instance.
     *
     * @author Christophe Friederich
     */
    private abstract static class AbstractSupportLevel implements Function<IDatabase, IDetailedDatabase> {

        /** */
        private final DbType type;

        protected AbstractSupportLevel(final DbType type) {
            this.type = type;
        }

        @Override
        public IDetailedDatabase apply(final IDatabase database) {
            return new DefaultDetailedDatabase(database, supportFor(database), type);
        }

        @Nonnull
        protected abstract DatabaseSupportLevel supportFor(@Nonnull IDatabase database);
    }

    /**
     * Specific implementation for Derby database.
     *
     * @author Christophe Friederich
     */
    private static class DerbySupportLevel extends AbstractSupportLevel {

        DerbySupportLevel() {
            super(null);
        }

        @Nonnull
        @Override
        protected DatabaseSupportLevel supportFor(@Nonnull final IDatabase database) {
            return DatabaseSupportLevel.SUPPORTED;
        }
    }

    /**
     * Specific implementation for HSQL database.
     *
     * @author Christophe Friederich
     */
    private static class HsqlSupportLevel extends AbstractSupportLevel {

        HsqlSupportLevel() {
            super(null);
        }

        @Nonnull
        @Override
        protected DatabaseSupportLevel supportFor(@Nonnull final IDatabase database) {
            return DatabaseSupportLevel.SUPPORTED;
        }
    }

    /**
     * Specific implementation for MySql database.
     *
     * @author Christophe Friederich
     */
    private static class MySqlSupportLevel extends AbstractSupportLevel {

        MySqlSupportLevel() {
            super(DbType.MYSQL);
        }

        @Nonnull
        @Override
        protected DatabaseSupportLevel supportFor(@Nonnull final IDatabase database) {
            if (database.getMajorVersion() == 5) {
                final int minor = database.getMinorVersion();
                if (minor == 0) {
                    // MySQL 5.0 is not part of the matrix build, so it cannot be marked as SUPPORTED, but ad hoc
                    // testing has shown that it works. Since it's not continuously verified, but has been known
                    // to work, the level is left as UNKNOWN rather than UNSUPPORTED
                    return DatabaseSupportLevel.UNKNOWN;
                }
                if (minor == 1 || minor == 5 || minor == 6 && database.getPatchVersion() >= 16) {
                    // MySQL 5.1 and 5.5 (all patch versions) are supported
                    // MySQL 5.6.16+ is supported
                    return DatabaseSupportLevel.SUPPORTED;
                }
            }

            // MySQL <= 5.6.10 has a critical bug which affects permission check queries. 5.6.11 includes a fix for
            // that bug, but suffers from another bug which breaks certain order by clauses. 5.6.16 includes a fix
            // for that bug and all existing tests pass on 5.6.16.
            //
            // After this experience with 5.6.x, future releases of MySQL will be handled pessimistically. As a result,
            // 5.7.x will be considered unsupported until a final version is released and has been explicitly verified
            // as supported.
            //
            // See http://bugs.mysql.com/bug.php?id=68424 -> Query optimizer bug (affects permissions)
            // See http://bugs.mysql.com/bug.php?id=69005 -> Row ordering bug (affects repository lists)
            return DatabaseSupportLevel.UNSUPPORTED;
        }
    }

    /**
     * Specific implementation for Oracle database.
     *
     * @author Christophe Friederich
     */
    private static class OracleSupportLevel extends AbstractSupportLevel {

        /** list of oracle supported version. */
        private static final Set<Integer> SUPPORTED_VERSIONS = ImmutableSet.of(11, 12);

        OracleSupportLevel() {
            super(DbType.ORACLE);
        }

        @Nonnull
        @Override
        protected DatabaseSupportLevel supportFor(@Nonnull final IDatabase database) {
            // We only test against the SUPPORTED_VERSIONS. Earlier versions may work
            // but we aren't sure.
            return SUPPORTED_VERSIONS.contains(database.getMajorVersion()) ? DatabaseSupportLevel.SUPPORTED
                    : DatabaseSupportLevel.UNKNOWN;
        }
    }

    /**
     * Specific implementation for PostgreSQL database.
     *
     * @author Christophe Friederich
     */
    private static class PostgresSupportLevel extends AbstractSupportLevel {

        /** */
        private static final Version POSTGRES_8_2 = new Version(8, 2);

        /** */
        private static final Version POSTGRES_9_5 = new Version(9, 5);

        PostgresSupportLevel() {
            super(DbType.POSTGRES);
        }

        @Nonnull
        @Override
        protected DatabaseSupportLevel supportFor(@Nonnull final IDatabase database) {
            return between(database.getVersion(), POSTGRES_8_2, POSTGRES_9_5) ? DatabaseSupportLevel.SUPPORTED
                    : DatabaseSupportLevel.UNKNOWN;
        }

        private static boolean between(final Version version, final Version lower, final Version upper) {
            return version.compareTo(lower) > -1 && version.compareTo(upper) < 0;
        }
    }

    /**
     * Specific implementation for SqlServer database.
     *
     * @author Christophe Friederich
     */
    private static class SqlServerSupportLevel extends AbstractSupportLevel {

        /** */
        private static final Map<Integer, DatabaseSupportLevel> LEVELS_BY_VERSION = ImmutableMap.of(9,
            DatabaseSupportLevel.DEPRECATED, // 2005 support was deprecated in 3.10 for removal in 4.0
            10,
            DatabaseSupportLevel.SUPPORTED, // 2008 is actively supported
            11,
            DatabaseSupportLevel.SUPPORTED, // 2012 is actively supported
            12,
            DatabaseSupportLevel.SUPPORTED); // 2014 is actively supported

        SqlServerSupportLevel() {
            super(DbType.MSSQL);
        }

        @Nonnull
        @Override
        public DatabaseSupportLevel supportFor(@Nonnull final IDatabase database) {
            return MoreObjects.firstNonNull(LEVELS_BY_VERSION.get(database.getMajorVersion()),
                DatabaseSupportLevel.UNKNOWN); // Earlier (and later) versions may work, but are not tested
        }
    }
}
