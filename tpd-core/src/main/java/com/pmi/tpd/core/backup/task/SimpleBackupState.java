package com.pmi.tpd.core.backup.task;

import java.io.File;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.database.spi.IDatabaseHandle;

import de.schlichtherle.truezip.zip.ZipOutputStream;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class SimpleBackupState implements IBackupState {

  /** */
  private final IDatabaseHandle sourceDatabase;

  /** */
  private File backupFile;

  /** */
  private ZipOutputStream backupZipStream;

  /**
   * @param sourceDatabase
   */
  public SimpleBackupState(final IDatabaseHandle sourceDatabase) {
    this.sourceDatabase = sourceDatabase;
  }

  @Override
  public File getBackupFile() {
    return backupFile;
  }

  @Override
  public ZipOutputStream getBackupZipStream() {
    return backupZipStream;
  }

  @Nonnull
  @Override
  public IDatabaseHandle getSourceDatabase() {
    return sourceDatabase;
  }

  @Override
  public void setBackupFile(@Nonnull final File backupFile) {
    this.backupFile = backupFile;
  }

  @Override
  public void setBackupZipStream(@Nonnull final ZipOutputStream backupZipStream) {
    this.backupZipStream = backupZipStream;
  }
}
