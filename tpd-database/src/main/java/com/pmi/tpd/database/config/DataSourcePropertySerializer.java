package com.pmi.tpd.database.config;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import org.apache.commons.text.StringEscapeUtils;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.database.DatabaseConstants;
import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * A serializer capable of writing {@link DataSourceConfiguration} to a
 * properties file.
 */
public class DataSourcePropertySerializer {

  /** */
  private final IDataSourceConfiguration config;

  /**
   * Create new instance of {@link DataSourcePropertySerializer}.
   *
   * @param config
   *               a data source configuration to use
   */
  public DataSourcePropertySerializer(@Nonnull final IDataSourceConfiguration config) {
    this.config = Assert.checkNotNull(config, "config");
  }

  /**
   * Write data source properties to property file.
   *
   * @param writer
   *               a writer pointing to property file.
   * @throws IOException
   *                     If an I/O error occurs
   */
  public void writeTo(final Writer writer) throws IOException {
    writer.write(driverClassName());
    writer.write(url());
    writer.write(user());
    writer.write(password());
  }

  private String driverClassName() {
    return keyValueString(DatabaseConstants.PROP_JDBC_DRIVER, config.getDriverClassName());
  }

  private String password() {
    return keyValueString(DatabaseConstants.PROP_JDBC_PASSWORD, config.getPassword());
  }

  private String url() {
    return keyValueString(DatabaseConstants.PROP_JDBC_URL, config.getUrl());
  }

  private String user() {
    return keyValueString(DatabaseConstants.PROP_JDBC_USER, config.getUser());
  }

  private static String keyValueString(final String key, final String value) {
    return String.format("%s=%s\n", key, value == null ? "" : StringEscapeUtils.escapeJava(value));
  }
}
