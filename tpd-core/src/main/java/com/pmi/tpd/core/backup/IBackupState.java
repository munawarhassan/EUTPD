package com.pmi.tpd.core.backup;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.database.spi.IDatabaseHandle;

import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * Represents the state about a backup step/phase that is shared between the
 * various steps in the backup process.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBackupState {

  /** */
  String ACTIVE_OBJECTS_BACKUP_FILE = "active-objects-data.xml";

  /** */
  String CHANGELOGS_BACKUP_FILE = "changelogs.zip";

  /** */
  String CONFIGURATION_BACKUP_FILE = ApplicationConstants.CONFIG_PROPERTIES_FILE_NAME;

  /** */
  String LIQUIBASE_BACKUP_FILE = "app-data.xml";

  /**
   * @return handle for the database that needs to be backed up
   */
  @Nonnull
  IDatabaseHandle getSourceDatabase();

  /**
   * @return the {@link File} where the backup is to be written to. Can be
   *         {@code null}, in which case no backup file
   *         has been created (yet).
   */
  @Nullable
  File getBackupFile();

  /**
   * @return the {@link ZipOutputStream} of the backup file that is being written.
   *         Can be {@code null}, in which case
   *         no backup file has been created (yet).
   */
  @Nullable
  ZipOutputStream getBackupZipStream();

  /**
   * @param backupFile
   */
  void setBackupFile(@Nonnull File backupFile);

  /**
   * @param zipOutputStream
   */
  void setBackupZipStream(@Nonnull ZipOutputStream zipOutputStream);
}
