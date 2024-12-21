package com.pmi.tpd.core.migration.impl;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.context.ApplicationContext;

import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.backup.impl.BackupTaskFactory;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.core.maintenance.ReleaseAffixedDatabaseStep;
import com.pmi.tpd.core.migration.IMigrationState;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.core.migration.task.BaseMigrationTask;
import com.pmi.tpd.core.migration.task.DatabaseMigrationTask;
import com.pmi.tpd.core.migration.task.DatabaseSetupTask;
import com.pmi.tpd.core.migration.task.FinalizeMigrationStep;
import com.pmi.tpd.core.migration.task.LatchAndDrainDatabaseMigrationStep;
import com.pmi.tpd.core.restore.IRestoreState;
import com.pmi.tpd.core.restore.RestorePhase;
import com.pmi.tpd.core.restore.RestorePhase.Builder;
import com.pmi.tpd.core.restore.task.DatabaseRestoreStep;
import com.pmi.tpd.core.restore.task.UnpackBackupFilesStep;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class MigrationTaskFactory extends BackupTaskFactory implements IMigrationTaskFactory {

    /** */
    private final IDatabaseManager databaseManager;

    @Inject
    public MigrationTaskFactory(final ApplicationContext applicationContext,
            @Nonnull final IDatabaseManager databaseManager) {
        super(applicationContext);
        this.databaseManager = checkNotNull(databaseManager, "databaseManager");
    }

    @Override
    public DatabaseMigrationTask migrationTask(final IDataSourceConfiguration configuration) {
        return create(DatabaseMigrationTask.class,
            BaseMigrationTask.QUALIFIER_TARGET_DATABASE,
            databaseManager.prepareDatabase(checkNotNull(configuration, "configuration")));
    }

    @Override
    public DatabaseSetupTask setupTask(final IDataSourceConfiguration configuration) {
        return create(DatabaseSetupTask.class,
            DatabaseSetupTask.QUALIFIER_TARGET_DATABASE,
            databaseManager.prepareDatabase(checkNotNull(configuration, "configuration")));
    }

    @Override
    public LatchAndDrainDatabaseStep latchAndDrainDatabaseMigrationStep(final LatchMode latchMode) {
        return create(LatchAndDrainDatabaseMigrationStep.class, "latchMode", latchMode);
    }

    @Override
    public ReleaseAffixedDatabaseStep releaseAffixedDatabaseStep() {
        return createInstance(ReleaseAffixedDatabaseStep.class);
    }

    @Override
    public Builder restorePhaseBuilder(final IRestoreState state) {
        return create(RestorePhase.Builder.class, "restoreState", checkNotNull(state, "state"));
    }

    @Override
    public UnpackBackupFilesStep unpackBackupFilesStep(final IMigrationState state) {
        return create(UnpackBackupFilesStep.class, "migrationState", checkNotNull(state, "state"));
    }

    @Override
    public DatabaseRestoreStep databaseRestoreStep(final IRestoreState state) {
        return create(DatabaseRestoreStep.class, "restoreState", checkNotNull(state, "state"));
    }

    @Override
    public FinalizeMigrationStep finalizeMigrationStep(final IMigrationState state) {
        return create(FinalizeMigrationStep.class, "restoreState", checkNotNull(state, "state"));
    }

}
