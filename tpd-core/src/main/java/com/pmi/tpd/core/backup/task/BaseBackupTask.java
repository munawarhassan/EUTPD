package com.pmi.tpd.core.backup.task;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.core.backup.BackupException;
import com.pmi.tpd.core.backup.CanceledBackupException;
import com.pmi.tpd.core.backup.IBackupClientProgressCallback;
import com.pmi.tpd.core.backup.IBackupState;
import com.pmi.tpd.core.backup.event.BackupCanceledEvent;
import com.pmi.tpd.core.backup.event.BackupFailedEvent;
import com.pmi.tpd.core.backup.event.BackupStartedEvent;
import com.pmi.tpd.core.backup.event.BackupSucceededEvent;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class BaseBackupTask implements IRunnableTask {

    /** */
    private final BackupClientPlaceholderStep backupClientPlaceholderStep;

    /** */
    private final IRunnableTask delegateTask;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final I18nService i18nService;

    /** */
    private final List<ILatchableService<?>> latchableServices;

    /** */
    private volatile boolean canceled;

    /**
     * @param eventPublisher
     * @param i18nService
     * @param taskFactory
     */
    public BaseBackupTask(final IDatabaseManager databaseManager, final IEventPublisher eventPublisher,
            final I18nService i18nService, final IBackupTaskFactory taskFactory,
            final IEventAdvisorService<?> eventAdvisorService, final ILatchableService<?>[] latchableServices) {
        this.backupClientPlaceholderStep = taskFactory.backupClientBackupStep();
        this.eventPublisher = eventPublisher;
        this.i18nService = i18nService;
        this.latchableServices = Lists.newArrayList(latchableServices);

        delegateTask = createDelegateBackupTask(backupClientPlaceholderStep,
            taskFactory,
            new SimpleBackupState(databaseManager.getHandle()),
            new MaintenanceApplicationEvent(eventAdvisorService.getEventType("performing-maintenance").orElseThrow(),
                    Product.getName() + " is unavailable while it is being backed up",
                    eventAdvisorService.getEventLevel(IEventAdvisorService.LEVEL_MAINTENANCE).orElseThrow(),
                    MaintenanceType.BACKUP));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void cancel() {
        canceled = true;
        delegateTask.cancel();
    }

    public IBackupClientProgressCallback getClientProgressCallback() {
        return backupClientPlaceholderStep;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return delegateTask.getProgress();
    }

    @Override
    public void run() {
        BackupException exception;
        try {
            eventPublisher.publish(new BackupStartedEvent(this));

            delegateTask.run();

            if (canceled) {
                throw new CanceledBackupException(i18nService.createKeyedMessage("app.backup.canceled"));
            }

            eventPublisher.publish(new BackupSucceededEvent(this));
        } catch (final Throwable t) {
            unlatchAll();

            // We set the exception to a variable so we can fire the correct migration ended
            // event
            if (t instanceof BackupException) {
                exception = (BackupException) t;
            } else {
                exception = new BackupException(i18nService.createKeyedMessage("app.backup.failed", Product.getName()),
                        t);
            }

            if (exception instanceof CanceledBackupException) {
                eventPublisher.publish(new BackupCanceledEvent(this));
            } else {
                eventPublisher.publish(new BackupFailedEvent(this));
            }

            throw exception;
        }
    }

    /**
     * @param backupClientPlaceholderStep
     * @param taskFactory
     * @param backupState
     * @param maintenanceEvent
     * @return
     */
    protected abstract IRunnableTask createDelegateBackupTask(BackupClientPlaceholderStep backupClientPlaceholderStep,
        IBackupTaskFactory taskFactory,
        IBackupState backupState,
        MaintenanceApplicationEvent maintenanceEvent);

    private void unlatchAll() {
        for (final ILatchableService<?> latchableService : latchableServices) {
            final ILatch latch = latchableService.getCurrentLatch();
            if (latch != null) {
                latch.unlatch();
            }
        }
    }
}
