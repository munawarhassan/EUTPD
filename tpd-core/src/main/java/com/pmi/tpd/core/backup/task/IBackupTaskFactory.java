package com.pmi.tpd.core.backup.task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceFactory;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.database.spi.IDatabaseHandle;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IBackupTaskFactory extends ITaskMaintenanceFactory {

    @Nonnull
    BackupTask backupTask();

    @Nonnull
    BackupClientPlaceholderStep backupClientBackupStep();

    @Nonnull
    LatchAndDrainDatabaseStep latchAndDrainDatabaseBackupStep(@Nonnull LatchMode latchMode);

    @Nonnull
    DatabaseBackupStep databaseBackupStep(@Nonnull IBackupState state);

    @Nonnull
    BackupPhase.Builder backupPhaseBuilder(@Nonnull IBackupState state);

    @Nonnull
    ChangelogsBackupStep changelogsBackupStep(@Nonnull IBackupState state);

    @Nonnull
    ConfigurationBackupStep configurationBackupStep(@Nonnull IBackupState state);

    @Nonnull
    UnlatchDatabaseStep unlatchDatabaseStep(@Nonnull LatchMode latchMode, @Nullable IDatabaseHandle database);

}
