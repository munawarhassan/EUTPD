package com.pmi.tpd.euceg.core.task;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.api.lifecycle.ProgressEvent;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.api.lifecycle.notification.NotificationEvent;
import com.pmi.tpd.api.lifecycle.notification.NotificationRequest;
import com.pmi.tpd.api.lifecycle.notification.NotificationRequest.Severity;
import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.Throwables;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;
import com.pmi.tpd.spring.transaction.SpringTransactionUtils;

public class SubmissionTrackingReportStep extends AbstractRunnableTask implements ITaskMonitorProgress {

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final I18nService i18nService;

    @Nonnull
    private final TransactionTemplate requiredTransactionTemplate;

    /** */
    private final ICancelState cancelState;

    /** */
    private volatile long rowsProcessed;

    /** */
    private volatile long totalRows;

    /** */
    private volatile String message;

    private final ITrackingReportState state;

    @Inject
    public SubmissionTrackingReportStep(final IEucegTaskFactory taskFactory, final I18nService i18nService,
            @Nonnull final PlatformTransactionManager transactionManager, final IEventPublisher eventPublisher,
            final ITrackingReportState state) {
        setTaskFactory(taskFactory);
        this.cancelState = new SimpleCancelState();
        this.i18nService = i18nService;
        this.requiredTransactionTemplate = new TransactionTemplate(
                Assert.checkNotNull(transactionManager, "transactionManager"), SpringTransactionUtils.REQUIRED);
        this.eventPublisher = eventPublisher;
        this.state = state;

    }

    @Override
    public void cancel() {
        cancelState.cancel(i18nService.createKeyedMessage("app.euceg.report.canceled", Product.getName()));
    }

    @Override
    public @Nonnull IProgress getProgress() {
        return new ProgressTask(
                this.message == null ? i18nService.getMessage("app.euceg.report.progress", rowsProcessed, totalRows)
                        : this.message,
                rowsProcessed == 0 || totalRows == 0 ? 0 : (int) Math.min(100, 100 * rowsProcessed / totalRows));
    }

    @Override
    public void run() {
        this.requiredTransactionTemplate.execute(status -> {
            try {
                this.state.getExporter().export(state, this);

            } catch (final Throwable e) {
                Throwables.throwUnchecked(e);
            }
            return null;
        });

    }

    @Override
    public void increment() {
        this.rowsProcessed++;
        if (this.rowsProcessed % 5 == 0) {
            this.eventPublisher.publish(new ProgressEvent(this, getProgress()));
        }
    }

    @Override
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public void clearMessage() {
        this.message = null;
    }

    @Override
    public long getTotalRows() {
        return totalRows;
    }

    @Override
    public void finish() {
        this.eventPublisher.publish(new ProgressEvent(this, getProgress()));
        this.eventPublisher.publish(new NotificationEvent(new NotificationRequest(Severity.success,
                "The submission tracking report was performed successfully ", TimeUnit.SECONDS.toMillis(10))));

    }

    @Override
    public void started(final long totalRows) {
        this.totalRows = totalRows;
    }

}