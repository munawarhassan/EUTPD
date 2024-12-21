package com.pmi.tpd.core.backup.task;

import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * Maintenance task for creating a system backup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class BackupTask extends BaseBackupTask {

    /**
     * @param databaseManager
     * @param eventPublisher
     * @param i18nService
     * @param taskFactory
     * @param latchableServices
     */
    public BackupTask(final IDatabaseManager databaseManager, final IEventPublisher eventPublisher,
            final I18nService i18nService, final IBackupTaskFactory taskFactory,
            final IEventAdvisorService<?> eventAdvisorService, final ILatchableService<?>... latchableServices) {
        super(databaseManager, eventPublisher, i18nService, taskFactory, eventAdvisorService, latchableServices);
    }

    @Override
    protected IRunnableTask createDelegateBackupTask(final BackupClientPlaceholderStep backupClientPlaceholderStep,
        final IBackupTaskFactory taskFactory,
        final IBackupState backupState,
        final MaintenanceApplicationEvent maintenanceEvent) {
        return taskFactory.maintenanceModePhaseBuilder()
                .event(maintenanceEvent)
                .add(
                    taskFactory.backupPhaseBuilder(backupState)
                            .add(taskFactory.changelogsBackupStep(backupState), 2)
                            .add(taskFactory.latchAndDrainDatabaseBackupStep(LatchMode.LOCAL), 3) // 5
                            .add(taskFactory.databaseBackupStep(backupState), 93) // 98
                            .add(taskFactory.configurationBackupStep(backupState), 2) // 100 backup
                            .build(),
                    50)
                // Blocks until a '100' update from the client backup is received or until the backup is cancelled.
                // Client progress updates can be received at any time before this task executes via calls to
                // backupClientPlaceholderStep.onProgressUpdate() and will be factored into the overall progress
                .add(backupClientPlaceholderStep, 50) // 100 overall
                .add(taskFactory.unlatchDatabaseStep(LatchMode.LOCAL, null), 0)
                .build();
    }
}
