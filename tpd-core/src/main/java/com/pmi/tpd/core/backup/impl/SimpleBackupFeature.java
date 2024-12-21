package com.pmi.tpd.core.backup.impl;

import static com.pmi.tpd.api.util.Assert.notNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.backup.IBackupFeature;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class SimpleBackupFeature implements IBackupFeature {

  /** */
  private final BackupFeatureMode mode;

  /** */
  private final String group;

  /** */
  private final String name;

  public SimpleBackupFeature(@Nonnull final String group, @Nonnull final String name,
      @Nonnull final BackupFeatureMode mode) {
    this.mode = notNull(mode);
    this.group = notNull(group);
    this.name = notNull(name);
  }

  @Nonnull
  @Override
  public String getGroup() {
    return group;
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public BackupFeatureMode getMode() {
    return mode;
  }
}
