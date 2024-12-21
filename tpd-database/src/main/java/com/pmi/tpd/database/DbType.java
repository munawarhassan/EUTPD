package com.pmi.tpd.database;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.joda.time.Duration;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.util.ClassUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Encapsulates the characteristics of each of the database types that Application supports.
 * <p>
 * Note: When adding a new {@code DbType}, in addition to a constant here, new entries need to be added to:
 * <ul>
 * <li>{@code DbConfig.properties} for names</li>
 * <li>{@code help-paths.properties} for documentation</li>
 * </ul>
 *
 * @since 1.3
 * @author Christophe Friederich
 */
public enum DbType {

    /**
     * SQL Server using the Microsoft JDBC driver.
     * <p>
     * Unlike jTDS, Microsoft is actively maintaining their driver. That means Microsoft's driver is a full JDBC 4 (Java
     * 6) compliant driver, where jTDS is still JDBC 3. Lack of full JDBC 4 support causes issues when using DbUnit with
     * Hibernate 4, due to Hibernate 4's {@code SQLException} translation, and breaks completely from Hibernate 4.1.8
     * and beyond.
     *
     * @link https://hibernate.onjira.com/browse/HHH-7778
     */
    MSSQL("mssql", "Microsoft SQL Server", "jdbc:sqlserver", //
            "com.microsoft.sqlserver.jdbc.SQLServerDriver", false, of(1433)) {

        @Override
        @Nonnull
        public String generateUrl(@Nonnull final String hostName, @Nonnull final String database, final int port) {
            return String.format("%1$s://%2$s:%3$d;databaseName=%4$s;", getProtocol(), hostName, port, database);
        }
    },
    /**
     *
     */
    MYSQL("mysql", "MySQL", "jdbc:mysql", //
            "com.mysql.jdbc.Driver", false, of(3306)) {

        @Override
        @Nonnull
        public String generateUrl(@Nonnull final String hostName, @Nonnull final String database, final int port) {
            return String.format(
                "%1$s://%2$s:%3$d/%4$s?"
                        + "characterEncoding=utf8&useUnicode=true&sessionVariables=storage_engine%%3DInnoDB",
                getProtocol(),
                hostName,
                port,
                database);
        }

        @Nonnull
        @Override
        public Map<String, String> getPropertyMap(final Duration timeout) {
            return ImmutableMap.of("connectTimeout", String.valueOf(timeout.getMillis()));
        }

        @Override
        public boolean isClusterable() {
            return false;
        }
    },
    /**
     *
     */
    ORACLE("oracle", "Oracle", "jdbc:oracle:thin", //
            "oracle.jdbc.driver.OracleDriver", true, of(1521)) {

        @Override
        @Nonnull
        public String generateUrl(@Nonnull final String hostName, @Nonnull final String service, final int port) {
            return String.format("%1$s:@//%2$s:%3$d/%4$s", getProtocol(), hostName, port, service);
        }

        @Nonnull
        @Override
        public Map<String, String> getPropertyMap(final Duration timeout) {
            return ImmutableMap.of("oracle.net.CONNECT_TIMEOUT", String.valueOf(timeout.getMillis()));
        }
    },
    /**
     *
     */
    POSTGRES("postgres", "PostgreSQL", "jdbc:postgresql", //
            "org.postgresql.Driver", false, of(5432)) {

        @Override
        @Nonnull
        public String generateUrl(@Nonnull final String hostName, @Nonnull final String database, final int port) {
            return String.format("%1$s://%2$s:%3$d/%4$s", getProtocol(), hostName, port, database);
        }
    };

    /**
     * A set of all of the DbType values.
     * <p>
     * The iterator returned by the iterator() method of this set traverses the elements in their natural order (the
     * order in which the enum constants are declared).
     */
    public static final List<DbType> AS_LIST = ImmutableList.copyOf(values());

    /**
     * A map of JDBC driver class names to the DB types they support.
     */
    private static final Map<String, DbType> ENUM_BY_DRIVER = mapByDriver();

    /**
     * A map of DB type keys (as provided to the constructor) to corresponding enum instances.
     */
    private static final Map<String, DbType> ENUM_BY_KEY = mapByKey();

    /**
     * Base for help keys in help-paths.properties.
     */
    private static final String APP_DB_MIGRATION_HELP_KEY = "app.help.db.migration.";

    // These variables hold the values to be shown by default on the DB configuration form
    /** */
    private final Optional<String> defaultDatabaseName;

    /** */
    private final Optional<String> defaultHostName;

    /** */
    private final Optional<Integer> defaultPort;

    /** */
    private final Optional<String> defaultUserName;

    /**
     * a display name for this DB type.
     */
    private final String displayName;

    /**
     * The name of the JDBC driver class that could be used to access a database of this DB type.
     */
    private final String driverClassName;

    /**
     * A short (5-20 char) string that can be used as a key for this database type; for example, when db types are made
     * available for selection on a web page.
     */
    private final String key;

    /**
     * The prefix of JDBC URLs that target this DB type.
     */
    private final String protocol;

    /**
     * Whether the JDBC URLs generated by this DB type incorporate an SID rather than a database name.
     */
    private final boolean usesSid;

    /**
     * Constructs a DB type with the given key, display name key, protocol, driver class name, uses SID, and default
     * port; and which does not have a default database name, database host name, nor user name.
     */
    DbType(final String key, final String displayNameKey, final String protocol, final String driverClassName,
            final boolean usesSid, final Optional<Integer> defaultPort) {
        this(key, displayNameKey, protocol, driverClassName, usesSid, empty(), empty(), defaultPort, empty());
    }

    /**
     * Constructs a DB type with the given key, display name key, protocol, driver class name, uses SID, default
     * database name, default host name, default port; and default user name.
     */
    DbType(final String key, final String displayName, final String protocol, final String driverClassName,
            final boolean usesSid, final Optional<String> defaultDatabaseName, final Optional<String> defaultHostHame,
            final Optional<Integer> defaultPort, final Optional<String> defaultUserName) {
        this.defaultDatabaseName = defaultDatabaseName;
        this.defaultHostName = defaultHostHame;
        this.defaultPort = defaultPort;
        this.defaultUserName = defaultUserName;
        this.displayName = displayName;
        this.driverClassName = driverClassName;
        this.key = key;
        this.protocol = protocol;
        this.usesSid = usesSid;
    }

    /**
     * Returns the DB type that is supported by the given JDBC driver.
     *
     * @param driverClassName
     *                        the name of the JDBC driver supporting the database
     * @return some(DbType), or none() if none of the DB types are supported by the JDBC driver
     * @throws NullPointerException
     *                              if the driver class name is null
     */
    @Nonnull
    public static Optional<DbType> forDriver(@Nonnull final String driverClassName) {
        checkNotNull(driverClassName, "driverClassName");
        return ofNullable(ENUM_BY_DRIVER.get(driverClassName));
    }

    /**
     * Returns the DB type corresponding to the given key.
     *
     * @param key
     *            the key of the returned DB type
     * @return some(DbType), or none() if none of the DB types correspond to the key
     * @throws NullPointerException
     *                              if the key is null
     */
    @Nonnull
    public static Optional<DbType> forKey(@Nonnull final String key) {
        return ofNullable(ENUM_BY_KEY.get(key));
    }

    /**
     * Sets the timeout duration for obtaining a connection via the given data source.
     *
     * @param dataSource
     *                   the data source that will be used to obtain connections
     * @param timeout
     *                   the initial connection timeout duration
     */
    public void applyTimeout(final AbstractDriverBasedDataSource dataSource, final Duration timeout) {
        setDataSourceProperties(getPropertyMap(timeout), dataSource);
    }

    /**
     * Produces a JDBC URL incorporating the given host name, database name or SID, and port.
     *
     * @param hostName
     *                 the host name of the machine on which the database is running
     * @param database
     *                 the name of the database, or alternatively its service identifier (when {@link #usesSid()} is
     *                 true
     * @param port
     *                 the port on which the database can be accessed at the {@code hostName}
     * @return a new string that can be used as a JDBC URL when accessing databases of this type
     */
    @Nonnull
    public abstract String generateUrl(@Nonnull String hostName, @Nonnull String database, int port);

    /**
     * @return Returns a {@link String} representing the default database name.
     */
    @Nonnull
    public Optional<String> getDefaultDatabaseName() {
        return defaultDatabaseName;
    }

    /**
     * @return Returns a {@link String} representing the default host name.
     */
    @Nonnull
    public Optional<String> getDefaultHostName() {
        return defaultHostName;
    }

    /**
     * @return Returns a {@link Option} representing the default port.
     */
    @Nonnull
    public Optional<Integer> getDefaultPort() {
        return defaultPort;
    }

    /**
     * @return Returns a {@link Option} representing the default user.
     */
    @Nonnull
    public Optional<String> getDefaultUserName() {
        return defaultUserName;
    }

    /**
     * @return Returns a {@link String} representing the display name.
     */
    @Nonnull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Returns a {@link String} representing the driver class name.
     */
    @Nonnull
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * If a new {@code DbType} is added, must add an entry for this key in help-paths.properties.
     *
     * @return the help key for the setup for this particular database type
     */
    @Nonnull
    public String getHelpKey() {
        return APP_DB_MIGRATION_HELP_KEY + getKey();
    }

    /**
     * @return Returns the unique of this database type.
     */
    @Nonnull
    public String getKey() {
        return key;
    }

    /**
     * Produces a map of connection properties suitable for passing to the JDBC driver when asking for connections.
     *
     * @param timeout
     *                the timeout duration to be used when attempting to obtain a connection
     * @return a new map
     */
    @Nonnull
    public Map<String, String> getPropertyMap(final Duration timeout) {
        return ImmutableMap.of("loginTimeout", String.valueOf(timeout.getStandardSeconds()));
    }

    /**
     * @return Returns a {@link String} representing the protocol to use.
     */
    @Nonnull
    public String getProtocol() {
        return protocol;
    }

    /**
     * Retrieves a flag indicating whether this database can be used with a Data Center, which allows clustering servers
     * using the same database.
     *
     * @return {@code true} if this database can be used in a cluster; otherwise, {@code false} for databases which can
     *         only be used by standalone, non-Data Center installations
     */
    public boolean isClusterable() {
        return true;
    }

    /**
     * Determines whether a JDBC driver is available for this DB type by searching the classpath for a class that has
     * the {@link #driverClassName}.
     *
     * @return Returns {@code true} whether the JDBC driver is available, otherwise {@code false}.
     */
    public boolean isDriverAvailable() {
        return ClassUtils.isPresent(driverClassName, getClass().getClassLoader());
    }

    /**
     * @return Returns {@code true} whether the JDBC URLs generated by this DB type incorporate an SID rather than a
     *         database name, otherwise {@code false}.
     */
    public boolean usesSid() {
        return usesSid;
    }

    private static Map<String, DbType> mapByDriver() {
        final ImmutableMap.Builder<String, DbType> builder = ImmutableMap.builder();
        for (final DbType t : values()) {
            builder.put(t.getDriverClassName(), t);
        }
        return builder.build();
    }

    private static Map<String, DbType> mapByKey() {
        final ImmutableMap.Builder<String, DbType> builder = ImmutableMap.builder();
        for (final DbType t : values()) {
            builder.put(t.getKey(), t);
        }
        return builder.build();
    }

    /**
     * Sets the connection properties of the given data source using the keys of the given map as property names, and
     * the values of the map as property values.
     *
     * @param pairs
     *                   a map of connection property names to property values
     * @param dataSource
     *                   the connection properties of this data source will be modified
     */
    private void setDataSourceProperties(final Map<String, String> pairs,
        final AbstractDriverBasedDataSource dataSource) {
        Properties connectionProperties = dataSource.getConnectionProperties();
        if (connectionProperties == null) {
            connectionProperties = new Properties();
        }
        connectionProperties.putAll(pairs);
        dataSource.setConnectionProperties(connectionProperties);
    }
}
