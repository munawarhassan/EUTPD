package com.pmi.tpd.core.elasticsearch.task;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.core.elasticsearch.task.event.IndexingCanceledEvent;
import com.pmi.tpd.core.elasticsearch.task.event.IndexingFailedEvent;
import com.pmi.tpd.core.elasticsearch.task.event.IndexingStartedEvent;
import com.pmi.tpd.core.elasticsearch.task.event.IndexingSucceededEvent;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;

/**
 * Maintenance task for creating a system backup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class IndexingTask implements IRunnableTask {

    /** */
    private final IRunnableTask delegateTask;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final I18nService i18nService;

    /** */
    private volatile boolean canceled;

    /**
     * @param eventPublisher
     * @param i18nService
     * @param taskFactory
     */
    public IndexingTask(final IEventPublisher eventPublisher, final I18nService i18nService,
            final IIndexerTaskFactory taskFactory, final IEventAdvisorService<?> eventAdvisorService,
            final ILatchableService<?>... latchableServices) {
        this.eventPublisher = eventPublisher;
        this.i18nService = i18nService;
        delegateTask = createDelegateIndexingTask(taskFactory,
            new MaintenanceApplicationEvent(eventAdvisorService.getEventType("performing-maintenance").orElseThrow(),
                    Product.getName() + " is unavailable while it is being indexed up",
                    eventAdvisorService.getEventLevel(IEventAdvisorService.LEVEL_MAINTENANCE).orElseThrow(),
                    MaintenanceType.INDEXING));
    }

    protected IRunnableTask createDelegateIndexingTask(final IIndexerTaskFactory taskFactory,
        final MaintenanceApplicationEvent maintenanceEvent) {
        return taskFactory.maintenanceModePhaseBuilder()
                .event(maintenanceEvent)
                .add(taskFactory.databaseIndexingStep(), 100)
                .build();
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

    @Override
    @Nonnull
    public IProgress getProgress() {
        return delegateTask.getProgress();
    }

    @Override
    public void run() {
        IndexingException exception;
        try {
            eventPublisher.publish(new IndexingStartedEvent(this));

            delegateTask.run();

            if (canceled) {
                throw new CanceledIndexingException(i18nService.createKeyedMessage("app.indexing.canceled"));
            }

            eventPublisher.publish(new IndexingSucceededEvent(this));
        } catch (final Throwable t) {

            // We set the exception to a variable so we can fire the correct migration ended
            // event
            if (t instanceof IndexingException) {
                exception = (IndexingException) t;
            } else {
                exception = new IndexingException(
                        i18nService.createKeyedMessage("app.indexing.failed", Product.getName()), t);
            }

            if (exception instanceof CanceledIndexingException) {
                eventPublisher.publish(new IndexingCanceledEvent(this));
            } else {
                eventPublisher.publish(new IndexingFailedEvent(this));
            }

            throw exception;
        }
    }

}
