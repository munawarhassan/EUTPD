package com.pmi.tpd.core.elasticsearch;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.spring.transaction.SpringTransactionUtils.definitionFor;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;
import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;
import com.pmi.tpd.core.elasticsearch.task.IIndexerTaskFactory;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.security.annotation.Unsecured;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
@Slf4j
public class DefaultIndexerService implements IIndexerService {

    /** */
    private static final int NUMBER_ENTITY_FETCH = 50;

    /** */
    private final IIndexerTaskFactory taskFactory;

    /** */
    private final IMaintenanceService maintenanceService;

    /** */
    private final I18nService i18nService;

    /** */
    private final IIndexerOperations indexerOperations;

    /** */
    private final IProductSubmissionStore productSubmissionStore;

    /** */
    private final IProductStore productStore;

    /** */
    private final ISubmitterStore submitterStore;

    /** */
    private final IAttachmentStore attachmentStore;

    /** */
    private final TransactionTemplate transactionTemplate;

    @Inject
    public DefaultIndexerService(@Nonnull final IIndexerTaskFactory taskFactory,
            @Nonnull final IMaintenanceService maintenanceService, @Nonnull final I18nService i18nService,
            @Nonnull final IIndexerOperations indexerOperations,
            @Nonnull final IProductSubmissionStore productSubmissionStore, @Nonnull final IProductStore productStore,
            @Nonnull final ISubmitterStore submitterStore, @Nonnull final IAttachmentStore attachmentStore,
            @Nonnull final PlatformTransactionManager transactionManager) {

        this.taskFactory = checkNotNull(taskFactory, "taskFactory");
        this.maintenanceService = checkNotNull(maintenanceService, "maintenanceService");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.indexerOperations = checkNotNull(indexerOperations, "indexerOperations");
        this.productSubmissionStore = checkNotNull(productSubmissionStore, "productSubmissionStore");
        this.productStore = checkNotNull(productStore, "productStore");
        this.submitterStore = checkNotNull(submitterStore, "submitterStore");
        this.attachmentStore = checkNotNull(attachmentStore, "attachmentStore");
        this.transactionTemplate = new TransactionTemplate(checkNotNull(transactionManager, "transactionManager"),
                definitionFor(TransactionDefinition.PROPAGATION_REQUIRED, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public ITaskMaintenanceMonitor performIndex() {
        try {
            return maintenanceService.start(taskFactory.indexingTask(), MaintenanceType.INDEXING);
        } catch (final IllegalStateException e) {
            LOGGER.error("An attempt to index the database was blocked because maintenance is already in progress");
            throw new IndexingException(i18nService.createKeyedMessage("app.migration.already.running"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("used in index task")
    public void indexDatabase(@Nonnull final ITaskMonitorProgress monitor) {

        final var productCount = this.productStore.count();
        final var submissionCount = this.productSubmissionStore.count();
        final var attachmentCount = this.attachmentStore.count();
        final var submitterCount = this.submitterStore.count();

        checkNotNull(monitor, "monitor").started(productCount + submissionCount + submitterCount + attachmentCount);

        this.indexerOperations.clearAll();

        indexSubmitter(monitor, submitterCount);
        indexProduct(monitor, productCount);
        indexAttachment(monitor, attachmentCount);
        indexSubmission(monitor, submissionCount);

        indexerOperations.optimize();
        monitor.finish();
    }

    private void indexProduct(final ITaskMonitorProgress monitor, final long totalRows) {

        monitor.setMessage("Indexing Product");
        final MutableLong count = new MutableLong(0);
        final Pageable request = PageUtils.newRequest(0, NUMBER_ENTITY_FETCH);
        final var tmp = Lists.<ProductIndexed> newArrayListWithCapacity(NUMBER_ENTITY_FETCH);

        transactionTemplate.execute(status -> {
            PageUtils.asStream(p -> this.productStore.findAll(p), request)
                    .peek(e -> monitor
                            .setMessage(String.format("Indexing Product (%d/%d)", count.incrementAndGet(), totalRows)))
                    .peek(e -> monitor.increment())
                    .map(ProductIndexed::from)
                    .forEach(e -> {
                        tmp.add(e);
                        if (tmp.size() >= NUMBER_ENTITY_FETCH) {
                            this.indexerOperations.saveAllProduct(tmp);
                            tmp.clear();
                        }
                    });
            this.indexerOperations.saveAllProduct(tmp);
            return null;
        });
        monitor.clearMessage();;
    }

    private void indexSubmission(final ITaskMonitorProgress monitor, final long totalRows) {
        monitor.setMessage("Indexing Submission");
        final MutableLong count = new MutableLong(0);
        final Pageable request = PageUtils.newRequest(0, NUMBER_ENTITY_FETCH);
        final var tmp = Lists.<SubmissionIndexed> newArrayListWithCapacity(NUMBER_ENTITY_FETCH);

        transactionTemplate.execute(status -> {
            PageUtils.asStream(p -> this.productSubmissionStore.findAll(p), request)
                    .peek(e -> monitor.setMessage(
                        String.format("Indexing Submission (%d/%d)", count.incrementAndGet(), totalRows)))
                    .peek(e -> monitor.increment())
                    .map(SubmissionIndexed::from)
                    .forEach(e -> {
                        tmp.add(e);
                        if (tmp.size() >= NUMBER_ENTITY_FETCH) {
                            this.indexerOperations.saveAllSubmission(tmp);
                            tmp.clear();
                        }
                    });
            this.indexerOperations.saveAllSubmission(tmp);
            return null;
        });

        monitor.clearMessage();
    }

    private void indexSubmitter(final ITaskMonitorProgress monitor, final long totalRows) {
        monitor.setMessage("Indexing Submitter");
        final MutableLong count = new MutableLong(0);
        final Pageable request = PageUtils.newRequest(0, NUMBER_ENTITY_FETCH);
        final var tmp = Lists.<SubmitterIndexed> newArrayListWithCapacity(NUMBER_ENTITY_FETCH);

        transactionTemplate.execute(status -> {
            PageUtils.asStream(p -> this.submitterStore.findAll(p), request)
                    .peek(e -> monitor.setMessage(
                        String.format("Indexing Submitter (%d/%d)", count.incrementAndGet(), totalRows)))
                    .peek(e -> monitor.increment())
                    .map(SubmitterIndexed::from)
                    .forEach(e -> {
                        tmp.add(e);
                        if (tmp.size() >= NUMBER_ENTITY_FETCH) {
                            this.indexerOperations.saveAllSubmitter(tmp);
                            tmp.clear();
                        }
                    });
            this.indexerOperations.saveAllSubmitter(tmp);
            return null;
        });

        monitor.clearMessage();
    }

    private void indexAttachment(final ITaskMonitorProgress monitor, final long totalRows) {
        monitor.setMessage("Indexing Attachment");
        final Pageable request = PageUtils.newRequest(0, NUMBER_ENTITY_FETCH);
        final MutableLong count = new MutableLong(0);
        final var tmp = Lists.<AttachmentIndexed> newArrayListWithCapacity(NUMBER_ENTITY_FETCH);

        transactionTemplate.execute(status -> {
            PageUtils.asStream(p -> this.attachmentStore.findAll(p), request)
                    .peek(e -> monitor.setMessage(
                        String.format("Indexing Attachment (%d/%d)", count.incrementAndGet(), totalRows)))
                    .peek(e -> monitor.increment())
                    .map(AttachmentIndexed::from)
                    .forEach(e -> {
                        tmp.add(e);
                        if (tmp.size() >= NUMBER_ENTITY_FETCH) {
                            this.indexerOperations.saveAllAttachment(tmp);
                            tmp.clear();
                        }
                    });
            this.indexerOperations.saveAllAttachment(tmp);
            return null;
        });
        monitor.clearMessage();
    }

}
