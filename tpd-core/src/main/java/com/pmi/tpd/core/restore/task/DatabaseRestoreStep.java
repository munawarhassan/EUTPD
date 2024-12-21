package com.pmi.tpd.core.restore.task;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.io.Closeables;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseAccessor;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseXmlWriterFactory;
import com.pmi.tpd.database.liquibase.ISchemaCreator;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseChangeSet;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseTables;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DatabaseRestoreStep extends AbstractRunnableTask implements ILiquibaseRestoreMonitor {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseRestoreStep.class);

  /** */
  private final ICancelState cancelState;

  /** */
  private final I18nService i18nService;

  /** */
  private volatile RestoreProgress progress;

  /** */
  private final IApplicationConfiguration settings;

  /** */
  private final IRestoreState state;

  /** */
  @Value("${liquibase.commit.block.size:10000}")
  private long commitBlockSize;

  /**
   * @param state
   * @param backupDataSourceSupplier
   * @param i18nService
   * @param migrationDao
   * @param settings
   */
  @Inject
  public DatabaseRestoreStep(final IRestoreState state, final I18nService i18nService,
      final IApplicationConfiguration settings) {
    this.cancelState = new SimpleCancelState();
    this.i18nService = i18nService;
    this.settings = settings;
    this.state = state;
  }

  @Override
  public void cancel() {
    cancelState.cancel(i18nService.createKeyedMessage("app.backup.restore.database.canceled", Product.getName()));
  }

  @Nonnull
  @Override
  public IProgress getProgress() {
    final RestoreProgress progress = this.progress;

    return progress != null ? progress
        : new ProgressImpl(i18nService.getMessage("app.backup.restore.database", Product.getName()), 0);
  }

  @Override
  public void onAppliedChange() {
    final RestoreProgress progress = this.progress;

    if (progress != null) {
      progress.onChange();
    } else {
      throw new IllegalStateException(
          "No current changeset! Must call onBeginChangeset() before calling " + "onAppliedChange().");
    }
  }

  @Override
  public void onBeginChangeset(final ILiquibaseChangeSet change, final int index, final int total) {
    final RestoreProgress progress = this.progress;
    this.progress = new RestoreProgress(progress, index, total, change);
  }

  @Override
  public void onFinishedChangeset() {
    // no-op. Overall progress is incremented in onBeginChangeset. This ensures that
    // currentChangesetProgressMonitor is never set to null after the first
    // changeset is processed.
  }

  @Override
  public void run() {
    final File backupDir = state.getUnzippedBackupDirectory();
    checkState(backupDir != null, "Unpacked backup not found");

    final File liquibaseFile = new File(backupDir, IBackupState.LIQUIBASE_BACKUP_FILE);
    LOGGER.debug("Restoring {} data from {}", Product.getName(), liquibaseFile.getAbsolutePath());

    final DataSource dataSource = state.getTargetDatabase().getDataSource();
    try (ILiquibaseAccessor liquibaseDao = createLiquibaseAccessor(dataSource)) {
      final ILiquibaseMigrationDao migrationDao = createLiquibaseMigrationDao();
      final FileInputStream stream = new FileInputStream(liquibaseFile);
      try {
        migrationDao
            .restore(liquibaseDao, stream, settings.getTemporaryDirectory().toFile(), this, cancelState);
      } catch (final LiquibaseDataAccessException e) {
        throw new BackupException(i18nService.createKeyedMessage("app.restore.fail"), e);
      } finally {
        Closeables.closeQuietly(stream);
      }

    } catch (final IOException e) {
      throw new BackupException(i18nService.createKeyedMessage("app.backup.restore.database.failed",
          liquibaseFile.getAbsolutePath()), e);
    }
  }

  /**
   * @param dataSource
   * @return
   */
  protected ILiquibaseAccessor createLiquibaseAccessor(final DataSource dataSource) {
    // make sure the migrationDao uses the unlatched datasourc
    final ISchemaCreator schemaCreator = getTaskFactory().getInstance(ISchemaCreator.class);
    final IDatabaseTables databaseTables = getTaskFactory().getInstance(IDatabaseTables.class);
    final String customChangePackageBase = getTaskFactory().getBean(String.class, "customChangePackageBase");
    return new DefaultLiquibaseAccessor(schemaCreator, databaseTables, dataSource, commitBlockSize,
        customChangePackageBase);
  }

  /**
   * @return
   */
  protected ILiquibaseMigrationDao createLiquibaseMigrationDao() {
    return new DefaultLiquibaseMigrationDao(new DefaultLiquibaseXmlWriterFactory());
  }

  /**
   * @author Christophe Friederich
   * @since 1.3
   */
  private class RestoreProgress implements IProgress {

    /** */
    private final ILiquibaseChangeSet change;

    /** */
    private final int changesetCount;

    /** */
    private final int changesetIndex;

    /** */
    private final int previousWeight;

    /** */
    private volatile long changesCompleted;

    protected RestoreProgress(@Nullable final RestoreProgress previous, final int changesetIndex,
        final int changesetCount, final ILiquibaseChangeSet change) {
      this.change = change;
      this.changesetCount = changesetCount;
      this.changesetIndex = changesetIndex;
      this.previousWeight = previous == null ? 0 : previous.previousWeight + previous.change.getWeight();
    }

    @Nonnull
    @Override
    public String getMessage() {
      return i18nService.getMessage("app.restore.changeset.processing",
          changesetIndex,
          changesetCount,
          change.getChangeCount());
    }

    @Override
    public int getPercentage() {
      return previousWeight + (int) (changesCompleted * change.getWeight() / change.getChangeCount());
    }

    public void onChange() {
      ++changesCompleted;
    }
  }
}
