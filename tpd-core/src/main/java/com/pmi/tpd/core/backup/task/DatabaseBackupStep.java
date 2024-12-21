package com.pmi.tpd.core.backup.task;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Preconditions;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseXmlWriterFactory;
import com.pmi.tpd.database.liquibase.ISchemaCreator;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseTables;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;
import com.pmi.tpd.security.IAuthenticationContext;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DatabaseBackupStep extends AbstractRunnableTask implements ILiquibaseBackupMonitor {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBackupStep.class);

  /** */
  public static final String DEFAULT_AUTHOR = "backup";

  /** */
  private final IAuthenticationContext authenticationContext;

  /** */
  private final ICancelState cancelState;

  /** */
  private final I18nService i18nService;

  /** */
  private volatile long rowsProcessed;

  /** */
  private final IBackupState state;

  /** */
  private volatile long totalRows;

  /** */
  @Value("${liquibase.commit.block.size:10000}")
  private long commitBlockSize;

  /**
   * @param state
   * @param authenticationContext
   * @param i18nService
   */
  public DatabaseBackupStep(final IBackupState state, final IAuthenticationContext authenticationContext,
      final I18nService i18nService) {

    this.authenticationContext = authenticationContext;
    this.cancelState = new SimpleCancelState();
    this.i18nService = i18nService;
    this.state = state;
  }

  @Override
  public void cancel() {
    cancelState.cancel(i18nService.createKeyedMessage("app.backup.backup.liquibase.canceled", Product.getName()));
  }

  @Nonnull
  @Override
  public IProgress getProgress() {
    return new ProgressTask(i18nService.getMessage("app.backup.backup.liquibase", Product.getName()),
        rowsProcessed == 0 || totalRows == 0 ? 0 : (int) Math.min(100, 100 * rowsProcessed / totalRows));
  }

  @Override
  public void run() {

    final ZipOutputStream stream = state.getBackupZipStream();
    Preconditions.checkState(stream != null, "A backup ZipOutputStream is required");

    try (ILiquibaseAccessor liquibaseDao = createLiquibaseAccessor(state.getSourceDatabase().getDataSource())) {

      final ILiquibaseMigrationDao migrationDao = createLiquibaseMigrationDao();

      final ZipEntry entry = new ZipEntry(IBackupState.LIQUIBASE_BACKUP_FILE);
      stream.putNextEntry(entry);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Backing up {} data to {}", Product.getName(), IBackupState.LIQUIBASE_BACKUP_FILE);
      }
      try {
        migrationDao.backup(liquibaseDao, new CloseShieldOutputStream(stream), getAuthor(), this, cancelState);
      } catch (final LiquibaseDataAccessException e) {
        throw new BackupException(i18nService.createKeyedMessage("app.backup.fail"), e);
      }

      stream.closeEntry();
    } catch (final IOException e) {
      throw new BackupException(i18nService.createKeyedMessage("app.backup.liquibase.failed", Product.getName()),
          e);
    }
  }

  protected ILiquibaseAccessor createLiquibaseAccessor(final DataSource dataSource) {
    // make sure the migrationDao uses the unlatched datasource
    final ISchemaCreator schemaCreator = getTaskFactory().getInstance(ISchemaCreator.class);
    final IDatabaseTables databaseTables = getTaskFactory().getInstance(IDatabaseTables.class);
    final String customChangePackageBase = getTaskFactory().getBean(String.class, "customChangePackageBase");
    return new DefaultLiquibaseAccessor(schemaCreator, databaseTables, dataSource, commitBlockSize,
        customChangePackageBase);
  }

  protected ILiquibaseMigrationDao createLiquibaseMigrationDao() {
    return new DefaultLiquibaseMigrationDao(new DefaultLiquibaseXmlWriterFactory());
  }

  @Override
  public void rowWritten() {
    this.rowsProcessed += 1;
  }

  @Override
  public void started(final long totalRows) {
    this.totalRows = totalRows;
  }

  private String getAuthor() {
    return authenticationContext.getCurrentUser().map(user -> user.getUsername()).orElse(DEFAULT_AUTHOR);
  }

}
