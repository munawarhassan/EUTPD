package com.pmi.tpd.core.backup;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.backup.impl.BackupFeatureMode;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBackupFeature {

  @Nonnull
  String getGroup();

  @Nonnull
  String getName();

  @Nonnull
  BackupFeatureMode getMode();
}
