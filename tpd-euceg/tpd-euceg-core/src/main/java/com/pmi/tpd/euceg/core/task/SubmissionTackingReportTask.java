package com.pmi.tpd.euceg.core.task;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.core.task.event.EucegTaskCanceledEvent;
import com.pmi.tpd.euceg.core.task.event.EucegTaskFailedEvent;
import com.pmi.tpd.euceg.core.task.event.EucegTaskStartedEvent;
import com.pmi.tpd.euceg.core.task.event.EucegTaskSucceededEvent;

/**
 * Maintenance task for creating a system backup.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class SubmissionTackingReportTask implements IRunnableTask {

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
    public SubmissionTackingReportTask(final @Nonnull IEventPublisher eventPublisher,
            final @Nonnull I18nService i18nService, final @Nonnull IEucegTaskFactory taskFactory,
            final @Nonnull ITrackingReportState state) {
        this.eventPublisher = eventPublisher;
        this.i18nService = i18nService;
        delegateTask = createDelegateTask(taskFactory, state);
    }

    protected IRunnableTask createDelegateTask(final @Nonnull IEucegTaskFactory taskFactory,
        final @Nonnull ITrackingReportState state) {
        return taskFactory.trackingReportPhaseBuilder(state).add(taskFactory.trackingReportStep(state), 100).build();
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
        EucegException exception;
        try {
            eventPublisher.publish(new EucegTaskStartedEvent(this));

            delegateTask.run();

            if (canceled) {
                throw new CanceledEucegTaskException(i18nService.createKeyedMessage("app.euceg.report.canceled"));
            }

            eventPublisher.publish(new EucegTaskSucceededEvent(this));
        } catch (final Throwable t) {

            // We set the exception to a variable so we can fire the correct migration ended
            // event
            if (t instanceof EucegException) {
                exception = (EucegException) t;
            } else {
                exception = new EucegException(
                        i18nService.createKeyedMessage("app.euceg.report.failed", Product.getName()), t);
            }

            if (exception instanceof CanceledEucegTaskException) {
                eventPublisher.publish(new EucegTaskCanceledEvent(this));
            } else {
                eventPublisher.publish(new EucegTaskFailedEvent(this));
            }

            throw exception;
        }
    }

}
