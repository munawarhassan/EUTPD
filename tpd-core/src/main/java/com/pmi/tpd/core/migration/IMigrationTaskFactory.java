package com.pmi.tpd.core.migration;

import javax.annotation.Nonnull;

import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.backup.task.IBackupTaskFactory;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.core.maintenance.ReleaseAffixedDatabaseStep;
import com.pmi.tpd.core.migration.task.DatabaseMigrationTask;
import com.pmi.tpd.core.migration.task.DatabaseSetupTask;
import com.pmi.tpd.core.migration.task.FinalizeMigrationStep;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.core.restore.RestorePhase;
import com.pmi.tpd.core.restore.task.DatabaseRestoreStep;
import com.pmi.tpd.core.restore.task.UnpackBackupFilesStep;
import com.pmi.tpd.database.IDataSourceConfiguration;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMigrationTaskFactory extends IBackupTaskFactory {

    @Nonnull
    DatabaseMigrationTask migrationTask(@Nonnull IDataSourceConfiguration newConfiguration);

    @Nonnull
    DatabaseSetupTask setupTask(@Nonnull IDataSourceConfiguration newConfiguration);

    @Nonnull
    LatchAndDrainDatabaseStep latchAndDrainDatabaseMigrationStep(@Nonnull LatchMode latchMode);

    /**
     * @return a task which will release any {@link com.pmi.tpd.database.spi.IDatabaseAffixed database-affixed}
     *         components so they can pick up the new database
     */
    @Nonnull
    ReleaseAffixedDatabaseStep releaseAffixedDatabaseStep();

    @Nonnull
    RestorePhase.Builder restorePhaseBuilder(@Nonnull IRestoreState state);

    @Nonnull
    UnpackBackupFilesStep unpackBackupFilesStep(@Nonnull IMigrationState state);

    @Nonnull
    DatabaseRestoreStep databaseRestoreStep(@Nonnull IRestoreState state);

    @Nonnull
    FinalizeMigrationStep finalizeMigrationStep(@Nonnull IMigrationState state);

}
