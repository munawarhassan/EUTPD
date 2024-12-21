package com.pmi.tpd.core.database;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.IOException;
import java.sql.Connection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.core.backup.IMigrationTarget;
import com.pmi.tpd.core.liquibase.LiquibaseMigrationTarget;
import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.DatabaseValidationException;
import com.pmi.tpd.database.spi.IDatabaseSupplier;
import com.pmi.tpd.database.spi.IDatabaseTables;
import com.pmi.tpd.database.spi.IDatabaseValidator;
import com.pmi.tpd.database.spi.IDetailedDatabase;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DatabaseValidator implements IDatabaseValidator {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseValidator.class);

  /** */
  private final IClusterService clusterService;

  /** */
  private final IDatabaseSupplier databaseSupplier;

  /** */
  private final I18nService i18nService;

  /** */
  private final IDatabaseTables databaseTables;

  /**
   * Create new instance of {@link DatabaseValidator}.
   * <p>
   * <b>Note: </b> Used by the backup client, which cannot test for clustering.
   * </p>
   *
   * @param databaseSupplier
   *                         the database to use.
   * @param i18nService
   *                         i18n resolver.
   */
  public DatabaseValidator(@Nonnull final IDatabaseSupplier databaseSupplier, @Nonnull final I18nService i18nService,
      @Nonnull final IDatabaseTables databaseTables) {
    this(null, databaseSupplier, i18nService, databaseTables);
  }

  /**
   * Create new instance of {@link DatabaseValidator}.
   *
   * @param clusterService
   *                         the cluster to use
   * @param databaseSupplier
   *                         the database to use.
   * @param i18nService
   *                         i18n resolver.
   */
  @Inject
  public DatabaseValidator(@Nullable final IClusterService clusterService,
      @Nonnull final IDatabaseSupplier databaseSupplier, @Nonnull final I18nService i18nService,
      @Nonnull final IDatabaseTables databaseTables) {
    this.clusterService = clusterService;
    this.databaseSupplier = checkNotNull(databaseSupplier, "databaseSupplier");
    this.i18nService = checkNotNull(i18nService, "i18nService");
    ;
    this.databaseTables = checkNotNull(databaseTables, "databaseTables");
    ;
  }

  /**
   * {@inheritDoc}
   *
   * @throws IOException
   */
  @Override
  public void validate(@Nonnull final DataSource dataSource) {
    Connection connection = null;
    try {
      connection = DataSourceUtils.getConnection(dataSource);

      final IDetailedDatabase database = databaseSupplier.getForConnection(connection);
      if (database.getSupportLevel() == DatabaseSupportLevel.UNSUPPORTED) {
        throw new DatabaseValidationException(i18nService.createKeyedMessage("app.db.unsupporteddatabase",
            Product.getName(),
            database.getName(),
            database.getVersion()));
      }

      if (!database.isInternal() && isClusterIsAvailable() && !database.isClusterable()) {
        throw new DatabaseValidationException(i18nService.createKeyedMessage("app.db.notclusterable",
            Product.getClusterName(),
            database.getName(),
            database.getVersion()));
      }

      try (IMigrationTarget target = createMigrationTarget(connection)) {
        if (!target.isUtf8()) {
          LOGGER.debug("The target database is not configured for UTF-8 support");
          throw new DatabaseValidationException(
              i18nService.createKeyedMessage("app.db.validation.notutf8", Product.getName()));
        }
        if (!target.hasNoClashingTables()) {
          LOGGER.debug("The target database contains {} tables", Product.getName());
          throw new DatabaseValidationException(
              i18nService.createKeyedMessage("app.db.validation.notempty", Product.getName()));
        }
        if (!target.hasRequiredSchemaPermissions()) {
          LOGGER.debug("The database user does not have the required schema permissions");
          throw new DatabaseValidationException(i18nService
              .createKeyedMessage("app.db.validation.insufficientpermissions", Product.getName()));
        }
        if (!target.hasRequiredTemporaryTablePermission()) {
          LOGGER.debug("The database user does not have the required temporary table permission");
          throw new DatabaseValidationException(
              i18nService.createKeyedMessage("app.db.validation.insufficienttemporarytablepermission",
                  Product.getName()));
        }
        if (!target.isCaseSensitive()) {
          LOGGER.debug("The target database is not case sensitive.");
          throw new DatabaseValidationException(
              i18nService.createKeyedMessage("app.db.validation.notcasesensitive", Product.getName()));
        }
      } catch (final IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    } finally {
      DataSourceUtils.releaseConnection(connection, dataSource);
    }
  }

  /**
   * create new instance of {@link IMigrationTarget}.
   *
   * @param connection
   *                   connection to use.
   * @return Returns new instance of {@link IMigrationTarget}.
   */
  protected IMigrationTarget createMigrationTarget(final Connection connection) {
    return new LiquibaseMigrationTarget(connection, databaseTables);
  }

  /**
   * @return {@code true} if the {@link IClusterService} was set and clustering is
   *         {@link IClusterService#isAvailable
   *         is available}; otherwise, {@code false}.
   */
  private boolean isClusterIsAvailable() {
    return clusterService != null && clusterService.isAvailable();
  }
}
