package com.pmi.tpd.core.elasticsearch.task;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;

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
import com.pmi.tpd.core.elasticsearch.IIndexerService;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public class DatabaseIndexingStep extends AbstractRunnableTask implements ITaskMonitorProgress {

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final I18nService i18nService;

    /** */
    private final IIndexerService indexerService;

    /** */
    private final ICancelState cancelState;

    /** */
    private volatile long rowsProcessed;

    /** */
    private volatile long totalRows;

    /** */
    private volatile String message;

    // /** */
    // private volatile long productCount;
    //
    // /** */
    // private volatile long submissionCount;
    //
    // /** */
    // private volatile long attachmentCount;
    //
    // /** */
    // private volatile long submitterCount;

    @Inject
    public DatabaseIndexingStep(final IIndexerTaskFactory taskFactory, final I18nService i18nService,
            final IEventPublisher eventPublisher, final IIndexerService indexerService) {
        setTaskFactory(taskFactory);
        this.cancelState = new SimpleCancelState();
        this.i18nService = i18nService;
        this.eventPublisher = eventPublisher;
        this.indexerService = indexerService;

    }

    @Override
    public void cancel() {
        cancelState.cancel(i18nService.createKeyedMessage("app.index.indexing.canceled", Product.getName()));
    }

    @Override
    @Nonnull
    public IProgress getProgress() {
        return new ProgressTask(
                this.message == null ? i18nService.getMessage("app.index.indexing.database", Product.getName())
                        : this.message,
                rowsProcessed == 0 || totalRows == 0 ? 0 : (int) Math.min(100, 100 * rowsProcessed / totalRows));
    }

    @Override
    public void run() {
        indexerService.indexDatabase(this);

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
    //
    // @Override
    // public long getTotalProducts() {
    // return productCount;
    // }
    //
    // @Override
    // public long getTotalAttachments() {
    // return attachmentCount;
    // }
    //
    // @Override
    // public long getTotalSubmissions() {
    // return submissionCount;
    // }
    //
    // @Override
    // public long getTotalSubmitters() {
    // return submitterCount;
    // }

    @Override
    public long getTotalRows() {
        return totalRows;
    }

    @Override
    public void finish() {
        this.eventPublisher.publish(new ProgressEvent(this, getProgress()));
        this.eventPublisher.publish(new NotificationEvent(new NotificationRequest(Severity.success,
                "The indexing was performed successfully ", TimeUnit.SECONDS.toMillis(10))));

    }

    @Override
    public void started(final long totalRows) {
        this.totalRows = totalRows;

    }

}
