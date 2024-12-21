package com.pmi.tpd.database.config;

import java.util.Properties;

import javax.annotation.Nonnull;

import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class SimpleDataSourceConfiguration implements IDataSourceConfiguration {

  /** */
  private final String driverClassName;

  /** */
  private final String url;

  /** */
  private final String user;

  /** */
  private final String password;

  /**
   * Create new instance of {@link SimpleDataSourceConfiguration}.
   *
   * @param driverClassName
   *                        the driver class name
   * @param url
   *                        the jdbc url.
   * @param user
   *                        the user used for connection.
   * @param password
   *                        the password used for connection.
   */
  public SimpleDataSourceConfiguration(final String driverClassName, final String url, final String user,
      final String password) {
    this.driverClassName = driverClassName;
    this.url = url;
    this.user = user;
    this.password = password;
  }

  /** {@inheritDoc} */
  @Nonnull
  @Override
  public String getDriverClassName() {
    return driverClassName;
  }

  /** {@inheritDoc} */
  @Nonnull
  @Override
  public String getPassword() {
    return password;
  }

  /** {@inheritDoc} */
  @Override
  public Properties getProperties() {
    return null;
  }

  /** {@inheritDoc} */
  @Nonnull
  @Override
  public String getUrl() {
    return url;
  }

  /** {@inheritDoc} */
  @Nonnull
  @Override
  public String getUser() {
    return user;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPasswordSet() {
    return password != null;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "DataSourceConfiguration{" + "driverClassName='" + driverClassName + '\'' + ", url='" + url + '\''
        + ", user='" + user + '\'' + '}';
  }
}
