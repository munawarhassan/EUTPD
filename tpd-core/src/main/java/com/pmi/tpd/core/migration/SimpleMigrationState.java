package com.pmi.tpd.core.migration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.backup.task.SimpleBackupState;
import com.pmi.tpd.database.spi.IDatabaseHandle;

public class SimpleMigrationState extends SimpleBackupState implements IMigrationState {

    private final IDatabaseHandle targetDatabase;

    private File unzippedBackupDirectory;

    public SimpleMigrationState(final IDatabaseHandle sourceDatabase, final IDatabaseHandle targetDatabase) {
        super(sourceDatabase);

        this.targetDatabase = targetDatabase;
    }

    @Nonnull
    @Override
    public IDatabaseHandle getTargetDatabase() {
        return targetDatabase;
    }

    @Override
    public File getUnzippedBackupDirectory() {
        return unzippedBackupDirectory;
    }

    @Override
    public void setUnzippedBackupDirectory(@Nonnull final File directory) {
        unzippedBackupDirectory = checkNotNull(directory, "directory");
    }
}
