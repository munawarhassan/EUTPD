package com.pmi.tpd.core.backup.impl;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.context.ApplicationContext;

import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.backup.task.BackupClientPlaceholderStep;
import com.pmi.tpd.core.backup.task.BackupPhase;
import com.pmi.tpd.core.backup.task.BackupPhase.Builder;
import com.pmi.tpd.core.backup.task.BackupTask;
import com.pmi.tpd.core.backup.task.ChangelogsBackupStep;
import com.pmi.tpd.core.backup.task.ConfigurationBackupStep;
import com.pmi.tpd.core.backup.task.DatabaseBackupStep;
import com.pmi.tpd.core.backup.task.IBackupTaskFactory;
import com.pmi.tpd.core.backup.task.LatchAndDrainDatabaseBackupStep;
import com.pmi.tpd.core.backup.task.UnlatchDatabaseStep;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.core.maintenance.MaintenanceModePhase;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.scheduler.exec.support.SpringTaskFactory;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class BackupTaskFactory extends SpringTaskFactory implements IBackupTaskFactory {

    @Inject
    public BackupTaskFactory(final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Nonnull
    @Override
    public MaintenanceModePhase.Builder maintenanceModePhaseBuilder() {
        return createInstance(MaintenanceModePhase.Builder.class);
    }

    @Override
    public BackupTask backupTask() {
        return createInstance(BackupTask.class);
    }

    @Override
    public BackupClientPlaceholderStep backupClientBackupStep() {
        return createInstance(BackupClientPlaceholderStep.class);
    }

    @Override
    public LatchAndDrainDatabaseStep latchAndDrainDatabaseBackupStep(final LatchMode latchMode) {
        return create(LatchAndDrainDatabaseBackupStep.class, "latchMode", latchMode);
    }

    @Override
    public DatabaseBackupStep databaseBackupStep(final IBackupState state) {
        return create(DatabaseBackupStep.class, "backupState", checkNotNull(state, "state"));
    }

    @Override
    public Builder backupPhaseBuilder(final IBackupState state) {
        return create(BackupPhase.Builder.class, "backupState", checkNotNull(state, "state"));
    }

    @Override
    public ChangelogsBackupStep changelogsBackupStep(final IBackupState state) {
        return create(ChangelogsBackupStep.class, "backupState", checkNotNull(state, "state"));
    }

    @Override
    public ConfigurationBackupStep configurationBackupStep(final IBackupState state) {
        return create(ConfigurationBackupStep.class, "backupState", checkNotNull(state, "state"));
    }

    @Override
    public UnlatchDatabaseStep unlatchDatabaseStep(final LatchMode latchMode, final IDatabaseHandle databaseHandle) {
        return create(UnlatchDatabaseStep.Builder.class, "latchMode", latchMode).target(databaseHandle).build();
    }

}
