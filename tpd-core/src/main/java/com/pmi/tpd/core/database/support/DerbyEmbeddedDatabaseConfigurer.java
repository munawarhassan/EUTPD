package com.pmi.tpd.core.database.support;

import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.jdbc.datasource.embedded.OutputStreamFactory;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer}
 * for the Apache Derby database.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class DerbyEmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DerbyEmbeddedDatabaseConfigurer.class);

  /** */
  private static final String URL_TEMPLATE = "jdbc:derby:%s;%s";

  /** Error code that indicates successful shutdown. */
  private static final String SHUTDOWN_CODE = "08006";

  /** Error code that indicates database not found. */
  private static final String DATABSE_NOT_FOUND_CODE = "XJ004";

  /** */
  private static final String SHUTDOWN_COMMAND = "shutdown=true";

  /** singleton instance. */
  private static DerbyEmbeddedDatabaseConfigurer instance;

  /** user name. */
  private String username = "sa";

  /** password. */
  private String password = "";

  /**
   * Get the singleton
   * {@link com.pmi.tpd.core.database.support.DerbyEmbeddedDatabaseConfigurer}
   * instance.
   *
   * @return the configurer
   * @throws java.lang.ClassNotFoundException
   *                                          if Derby is not on the classpath
   */
  public static synchronized DerbyEmbeddedDatabaseConfigurer getInstance() throws ClassNotFoundException {
    if (instance == null) {
      // disable log file
      System.setProperty("derby.stream.error.method",
          OutputStreamFactory.class.getName() + ".getNoopOutputStream");
      instance = new DerbyEmbeddedDatabaseConfigurer();
    }
    return instance;
  }

  private DerbyEmbeddedDatabaseConfigurer() {
  }

  /**
   * Sets user name and password connection.
   *
   * @param username
   *                 user name used (can be null).
   * @param password
   *                 password used (can be null).
   * @return Returns the fluent
   *         {@link com.pmi.tpd.core.database.support.DerbyEmbeddedDatabaseConfigurer}
   *         instance.
   */
  public DerbyEmbeddedDatabaseConfigurer setConnection(@Nullable final String username,
      @Nullable final String password) {
    if (StringUtils.hasText(username)) {
      this.username = username;
    }
    if (StringUtils.hasText(password)) {
      this.password = password;
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public void configureConnectionProperties(final ConnectionProperties properties, final String databaseName) {
    properties.setDriverClass(EmbeddedDriver.class);
    properties.setUrl(String.format(URL_TEMPLATE, databaseName, "create=true"));
    properties.setUsername(username);
    properties.setPassword(password);
  }

  /** {@inheritDoc} */
  @Override
  public void shutdown(final DataSource dataSource, final String databaseName) {
    try {
      new EmbeddedDriver().connect(String.format(URL_TEMPLATE, databaseName, SHUTDOWN_COMMAND), new Properties());
    } catch (final SQLException ex) {
      if (!SHUTDOWN_CODE.equals(ex.getSQLState()) && !DATABSE_NOT_FOUND_CODE.equals(ex.getSQLState())) {
        LOGGER.warn("Could not shutdown Derby database", ex);
        return;
      }
    } finally {
      // force garbage collection to unload the EmbeddedDriver
      // so Derby can be restarted
      System.gc();
    }
  }

}
