package com.pmi.tpd.core.backup.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.io.Flushables;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.database.config.DataSourcePropertySerializer;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * Step in the backup process that backs up the
 * {@value ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME} file.
 *
 * @author Christophe Friederich
 * @since
 */
public class ConfigurationBackupStep extends AbstractRunnableTask {

  /** */
  public static final String BANNER = "#> Produced for backup from default configuration";

  /** */
  public static final String CONFIG_FILE = ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME;

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationBackupStep.class);

  /** */
  private final Path sharedHomeDir;

  /** */
  private final I18nService i18nService;

  /** */
  private volatile int progress;

  /** */
  private final IBackupState state;

  /**
   * @param state
   * @param applicationSettings
   * @param i18nService
   */
  public ConfigurationBackupStep(final IBackupState state, final IApplicationConfiguration applicationSettings,
      final I18nService i18nService) {
    this.sharedHomeDir = applicationSettings.getSharedHomeDirectory();
    this.i18nService = i18nService;
    this.state = state;
  }

  @Nonnull
  @Override
  public IProgress getProgress() {
    return new ProgressImpl(i18nService.getMessage("app.backup.configuration", Product.getName()), progress);
  }

  @Override
  public void run() {
    final ZipOutputStream stream = state.getBackupZipStream();
    Preconditions.checkState(stream != null, "A backup ZipOutputStream is required");

    try {
      final ZipEntry entry = new ZipEntry(IBackupState.CONFIGURATION_BACKUP_FILE);
      stream.putNextEntry(entry);

      LOGGER.debug("Backing up {} configuration to {}",
          Product.getName(),
          IBackupState.CONFIGURATION_BACKUP_FILE);
      writeConfigurationBackup(new CloseShieldOutputStream(stream));

      stream.closeEntry();
      progress = 100;
    } catch (final IOException e) {
      throw new BackupException(
          i18nService.createKeyedMessage("app.backup.configuration.failed", Product.getName()), e);
    }
  }

  private void writeConfigurationBackup(final OutputStream stream) throws IOException {
    final File config = sharedHomeDir.resolve(CONFIG_FILE).toFile();
    if (config.exists()) {
      Files.copy(config, stream);
    } else {
      // app-config.properties doesn't exist. Rather than produce an empty file,
      // output the current database
      // settings (for record keeping)
      final BufferedWriter writer = new BufferedWriter(
          new OutputStreamWriter(stream, ApplicationConstants.getDefaultCharset()));
      try {
        writeDefaultConfiguration(writer);
      } finally {
        // Flush the writer but do _not_ close it. Flushing it pushes through any
        // buffered output, but we do
        // not want to close the output stream we wrapped when we constructed it
        Flushables.flush(writer, false);
      }
    }
  }

  /**
   * Writes the current {@link com.pmi.tpd.database.IDataSourceConfiguration} to
   * the provided {@code writer} .
   * <p>
   * A {@link #BANNER banner} is written first, which enables the restore
   * processing to detect that the configuration
   * being restored is just a placeholder and should not actually be restored.
   *
   * @param writer
   *               the writer to which the configuration should be written
   * @throws IOException
   *                     if the configuration cannot be written
   * @see DataSourcePropertySerializer
   */
  private void writeDefaultConfiguration(final BufferedWriter writer) throws IOException {
    final DataSourcePropertySerializer serializer = new DataSourcePropertySerializer(
        state.getSourceDatabase().getConfiguration());
    writer.write(BANNER + "\n");
    serializer.writeTo(writer);
  }
}
