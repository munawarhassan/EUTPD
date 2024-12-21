package com.pmi.tpd.database.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.database.DbType;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IMutableDataSourceConfiguration;
import com.pmi.tpd.database.hibernate.UnsupportedJdbcUrlException;

/**
 * A default implementation of the {@link IMutableDataSourceConfiguration}
 * interface.
 * <p>
 * Note: This class has been reviewed and approved for having mutable state. It
 * is primarily to support database
 * migration, which requires <i>some</i> mechanism for updating the driver class
 * name and URL at runtime. The
 * {@link IDataSourceConfiguration} interface, which is how this object is
 * generally injected, provides an immutable
 * fascia for the class, so to normal consumers it appears immutable.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultDataSourceConfiguration implements IMutableDataSourceConfiguration {

  /** */
  public static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

  /** */
  public static final String JTDS_PROTOCOL = "jdbc:jtds:sqlserver";

  /**
   * Pattern matching jTDS JDBC URLs.
   * <p>
   * This pattern defines the following groups:
   * <ol>
   * <li>Host name</li>
   * <li>Port (Optional)</li>
   * <li>Database name (Optional)</li>
   * <li>Additional parameters (Optional)</li>
   * </ol>
   * URLs can specify a database name two ways:
   * <ol>
   * <li>jdbc:jtds:sqlserver://hostname:port/database: Separated from the
   * host/port with a forward slash</li>
   * <li>jdbc:jtds:sqlserver://hostname:port;databaseName=database: In the
   * additional parameter section with the
   * parmeter name "databaseName"</li>
   * </ol>
   * The {@link DbType#MSSQL SQL Server} database type always generates them the
   * second way, but jTDS supports both.
   * As a nicety, this URL does as well.
   */
  public static final Pattern JTDS_URL = Pattern
      .compile(JTDS_PROTOCOL + "://([^:;/]++)(?::([\\d]{1,5}))?(?:/([^;]++))?(?:$|([^:/].*))");

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataSourceConfiguration.class);

  /** */
  private long connectTimeout;

  /** */
  private String driverClassName;

  /** */
  private String password;

  /** */
  private String url;

  /** */
  private String user;

  /**
   * Create new instance of {@link DefaultDataSourceConfiguration}.
   *
   * @param driverClassName
   *                        a driver class name.
   * @param user
   *                        a user name.
   * @param password
   *                        a password.
   * @param url
   *                        a jdbc url.
   */
  public DefaultDataSourceConfiguration(final String driverClassName, final String user, final String password,
      final String url) {
    update(driverClassName, url, user, password);
  }

  @Nonnull
  @Override
  public IDataSourceConfiguration copy() {
    return new SimpleDataSourceConfiguration(driverClassName, url, user, password);
  }

  @Nonnull
  @Override
  public String getDriverClassName() {
    return driverClassName;
  }

  @Nonnull
  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Properties getProperties() {
    final Properties properties = new Properties();

    // These properties are mandated by JDBC. DriverManager.getConnection(String,
    // String, String) takes the user and
    // password values and sets them in a new Properties instance and then calls the
    // Properties-based override. This
    // just moves that behaviour up. It's necessary because of how BoneCP works
    // internally. If a Properties object
    // is
    // provided, it calls DriverManager.getConnection(String, Properties) instead of
    // the version accepting a user
    // and
    // password. It will automatically add these to the properties if we don't, but
    // it logs warnings as it does so.
    putIfNotBlank(properties, "user", getUser());
    putIfNotBlank(properties, "password", getPassword());

    // If a connect timeout has been specified, apply the driver-specific property
    // for it. If there is no DbType for
    // the JDBC URL (read: HSQL), no timeout is applied. That's fine because HSQL
    // can't timeout anyway.
    if (connectTimeout > 0L) {
      DbType.forDriver(getDriverClassName())
          .ifPresent(
              type -> properties.putAll(type.getPropertyMap(Duration.standardSeconds(connectTimeout))));
    }

    return properties;
  }

  @Nonnull
  @Override
  public String getUrl() {
    return url;
  }

  @Nonnull
  @Override
  public String getUser() {
    return user;
  }

  @Override
  public boolean isPasswordSet() {
    return StringUtils.isNotEmpty(password);
  }

  /**
   * Sets the jdbc connection timeout.
   *
   * @param connectTimeout
   *                       a timeout to use (in seconds).
   */
  public void setConnectTimeout(final long connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  @Nonnull
  @Override
  public IDataSourceConfiguration update(final IDataSourceConfiguration configuration) {
    // Capture the existing settings
    final IDataSourceConfiguration old = copy();

    // Update the settings with the provided values
    update(configuration.getDriverClassName(),
        configuration.getUrl(),
        configuration.getUser(),
        configuration.getPassword());

    // Return the old settings
    return old;
  }

  @Override
  public String toString() {
    return user + "@" + url + " via " + driverClassName;
  }

  /**
   * Updates the driver class name, JDBC URL, user and password values.
   * <p>
   * The jTDS driver for SQLServer is no longer supported as it does not play well
   * with newer versions of Hibernate.
   * It's just too outdated. When jTDS details are detected, they are massaged
   * over to Microsoft's own driver format.
   * <p>
   * This method may be supplied with jTDS values by one of two ways: through the
   * constructor where the values are
   * injected via Spring during startup from the app-config.properties of a system
   * configured to use jTDS values; or
   * via the {@link #update(IDataSourceConfiguration) update} called by the
   * restore client restoring a system
   * configured to use the jTDS driver. There is no path other code path in
   * application that can trigger this special
   * processing.
   *
   * @param driverClassName
   *                        the JDBC driver class name
   * @param url
   *                        the JDBC url
   * @param user
   *                        the JDBC username
   * @param password
   *                        the JDBC password
   */
  private void update(String driverClassName, String url, final String user, final String password) {
    if (JTDS_DRIVER.equals(driverClassName)) {
      LOGGER.debug(
          "Replacing jTDS JDBC details with Microsoft equivalents. Original values: Driver = [{}], URL = [{}]",
          driverClassName,
          url);

      url = rewriteUrl(url);
      driverClassName = DbType.MSSQL.getDriverClassName();
      LOGGER.warn("Replaced jTDS JDBC details with Microsoft equivalents. New values: Driver = [{}], URL = [{}]",
          driverClassName,
          url);
    }
    this.driverClassName = driverClassName;
    this.user = user;
    this.password = password;
    this.url = url;
  }

  private static Map<String, String> parseParameters(final String value) {
    if (StringUtils.isBlank(value)) {
      return Maps.newHashMap();
    }
    final String[] pieces = value.split(";");

    final Map<String, String> parameters = new HashMap<>(pieces.length);
    for (final String piece : pieces) {
      if (StringUtils.isBlank(piece)) {
        continue;
      }
      final String[] keyValue = piece.split("=");

      parameters.put(keyValue[0], keyValue[1]);
    }
    return parameters;
  }

  private static int parsePort(final String value) {
    int port;
    if (value == null) {
      port = DbType.MSSQL.getDefaultPort().get();
    } else {
      port = Integer.parseInt(value);
    }
    return port;
  }

  private static void putIfNotBlank(final Properties properties, final String key, final String value) {
    if (StringUtils.isNotBlank(value)) {
      properties.put(key, value);
    }
  }

  private static String rewriteUrl(final String url) {
    // Microsoft's JDBC URL is very similar to jTDS's, in how the system constructs
    // it, but some customer
    // installations use custom URLs in order to leverage domain authentication and
    // other features. Those
    // properties vary, sometimes widely, between Microsoft's JDBC driver and jTDS.
    final Matcher matcher = JTDS_URL.matcher(url);
    if (matcher.matches()) {
      final Map<String, String> parameters = parseParameters(matcher.group(4));

      String databaseName = matcher.group(3);
      if (databaseName == null) {
        databaseName = parameters.remove("databaseName");
      }
      if (databaseName != null && parameters.isEmpty()) {
        final int port = parsePort(matcher.group(2));

        // If the URL only uses the databaseName (which is how the system constructs
        // it), the URL for
        // Microsoft's JDBC driver is identical to the one for jTDS except using a
        // different protocol.
        return DbType.MSSQL.generateUrl(matcher.group(1), databaseName, port);
      }
    }

    // If we could not extract the database name (SQL Server supports setting a
    // default database for
    // a user, so it's not technically required--but the system can't generate a URL
    // without one) or
    // if there are additional parameters supplied, or if the pattern doesn't match,
    // the URL cannot
    // be automatically rewritten.
    LOGGER.error("JDBC URL {} was not generated by {} and cannot be updated automatically", url, Product.getName());
    throw new UnsupportedJdbcUrlException("The configured JDBC URL is no longer supported");
  }
}
