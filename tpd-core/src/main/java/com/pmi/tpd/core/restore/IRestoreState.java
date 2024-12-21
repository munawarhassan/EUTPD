package com.pmi.tpd.core.restore;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.database.spi.IDatabaseHandle;

public interface IRestoreState {

  @Nonnull
  IDatabaseHandle getTargetDatabase();

  @Nullable
  File getUnzippedBackupDirectory();

  void setUnzippedBackupDirectory(@Nonnull File file);
}
