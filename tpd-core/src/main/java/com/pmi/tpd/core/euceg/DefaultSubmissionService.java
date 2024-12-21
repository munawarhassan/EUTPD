package com.pmi.tpd.core.euceg;

import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Submission;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IJobRunnerRequest;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.core.euceg.event.SubmissiontCreatedEvent;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.IReceiptVisitor;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.transaction.SpringTransactionUtils;

/**
 * @author Christophe Friederich
 * @author pascal schmid
 * @since 1.0
 */
@Singleton
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultSubmissionService implements ISubmissionService, IScheduledJobSource {

    /** */
    @SuppressWarnings("null")
    @Nonnull
    private static final JobId CANCEL_SUBMISSION_JOB_ID = JobId
            .of(CancelOldPendingSubmissionSchedulerJob.class.getSimpleName());

    /** */
    @Nonnull
    private static final JobRunnerKey CANCEL_SUBMISSION_JOB_RUNNER_KEY = JobRunnerKey
            .of(CancelOldPendingSubmissionSchedulerJob.class.getName());

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSubmissionService.class);

    /** */
    private final IAttachmentService attachmentService;

    /** */
    private final IAttachmentStore attachmentStore;

    /** */
    private final ISubmitterStore submitterStore;

    /** */
    private final IProductStore productStore;

    /** */
    private final IProductSubmissionStore productSubmissionStore;

    /** */
    private final I18nService i18nService;

    /** */
    private final IBackendManager backendManager;

    /** */
    private final IEventPublisher publisher;

    /** */
    private final IAuthenticationContext authContext;

    /** */
    private final TransactionTemplate requiresNewTransactionTemplate;

    /** */
    private final IApplicationProperties applicationProperties;

    /** */
    private final IEucegConstraintRuleManager constraintRules;

    /** */
    private Duration cancelInterval = Duration.ofHours(24); // default

    /**
     * Create new instance of {@link DefaultSubmissionService}.
     *
     * @param fileStorage
     *                                   a file storage.
     * @param attachmentStore
     *                                   a attachment store.
     * @param submitterStore
     *                                   a submitter store.
     * @param productSubmissionStore
     *                                   a product submission store.
     * @param productStore
     *                                   a product store.
     * @param backendManager
     *                                   a backend manager.
     * @param i18nService
     *                                   a localization service.
     * @param publisher
     *                                   a event publisher.
     * @param platformTransactionManager
     *                                   a transaction manager.
     */
    @Inject
    public DefaultSubmissionService(@Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IAttachmentService attachmentService, @Nonnull final IAttachmentStore attachmentStore,
            @Nonnull final ISubmitterStore submitterStore,
            @Nonnull final IProductSubmissionStore productSubmissionStore, @Nonnull final IProductStore productStore,
            @Nonnull final IBackendManager backendManager, @Nonnull final I18nService i18nService,
            @Nonnull final IEventPublisher publisher,
            @Nonnull final PlatformTransactionManager platformTransactionManager,
            @Nonnull final IAuthenticationContext authContext, final IEucegConstraintRuleManager constraintRules) {
        this.applicationProperties = Assert.checkNotNull(applicationProperties, "applicationProperties");
        this.attachmentService = Assert.checkNotNull(attachmentService, "attachmentService");
        this.attachmentStore = Assert.checkNotNull(attachmentStore, "attachmentStore");
        this.submitterStore = Assert.checkNotNull(submitterStore, "submitterStore");
        this.productSubmissionStore = Assert.checkNotNull(productSubmissionStore, "productSubmissionStore");
        this.productStore = Assert.checkNotNull(productStore, "productStore");
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
        this.backendManager = Assert.checkNotNull(backendManager, "backendManager");
        this.publisher = Assert.checkNotNull(publisher, "publisher");
        this.constraintRules = Assert.checkNotNull(constraintRules, "constraintRules");
        this.authContext = Assert.checkNotNull(authContext, "authContext");
        requiresNewTransactionTemplate = new TransactionTemplate(platformTransactionManager,
                SpringTransactionUtils.REQUIRES_NEW);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        final BackendProperties backendProperties = applicationProperties.getConfiguration(BackendProperties.class);
        cancelInterval = backendProperties.getOptions().getCancelInterval();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Register Cancel Submission Job-> cancelInterval:{}, await awaitBeforeCancel: {}",
                backendProperties.getOptions().getCancelInterval(),
                backendProperties.getOptions().getAwaitBeforeCancel());
        }
        schedulerService.registerJobRunner(CANCEL_SUBMISSION_JOB_RUNNER_KEY,
            new CancelOldPendingSubmissionSchedulerJob(backendProperties.getOptions().getAwaitBeforeCancel()));

        final long intervalMillis = cancelInterval.toMillis();
        schedulerService.scheduleJob(CANCEL_SUBMISSION_JOB_ID,
            JobConfig.forJobRunnerKey(CANCEL_SUBMISSION_JOB_RUNNER_KEY)
                    .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER)
                    .withSchedule(
                        Schedule.forInterval(intervalMillis, new Date(System.currentTimeMillis() + intervalMillis))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unschedule(@Nonnull final ISchedulerService schedulerService) throws SchedulerServiceException {
        schedulerService.unregisterJobRunner(CANCEL_SUBMISSION_JOB_RUNNER_KEY);
    }

    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public SubmitterRequest getSubmitter(@Nonnull final String submitterId) {
        return SubmitterRequest.from(this.submitterStore.get(Assert.checkNotNull(submitterId, "submitterId")));
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public SubmitterRequest createSubmitter(@Nonnull final SubmitterRequest submitter) {
        Assert.checkNotNull(submitter, "submitter");
        return SubmitterRequest.from(this.submitterStore.create(SubmitterEntity.builder()
                .submitterId(submitter.getSubmitterId())
                .name(submitter.getName())
                .details(submitter.getDetails())
                .submitter(submitter.getSubmitter())
                .status(SubmitterStatus.DRAFT)
                .build()));
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public SubmitterRequest updateSubmitter(@Nonnull final SubmitterRequest submitter) {
        Assert.checkNotNull(submitter, "submitter");
        Assert.isTrue(submitter.getSubmitterId() != null);
        final SubmitterEntity entity = this.submitterStore.get(submitter.getSubmitterId());
        return SubmitterRequest.from(this.submitterStore.save(entity.copy()
                .name(submitter.getName())
                .details(submitter.getDetails())
                .submitter(submitter.getSubmitter())
                .status(SubmitterStatus.DRAFT)
                .build()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    @Nonnull
    public SubmissionEntity createSubmission(@Nonnull final ISubmissionEntity entity) {
        Assert.checkNotNull(entity, "entity");
        return productSubmissionStore.create((SubmissionEntity) entity);
    }

    /**
     * Create a submission and store submission created using a {@link SubmissionSendRequest request}.
     *
     * @param request
     *                request used to create and store a new submission.
     * @return Returns the persisted instance of {@link SubmissionEntity}.
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, EucegException.class })
    @Override
    @Nonnull
    public SubmissionEntity createSubmission(@Nonnull final SubmissionSendRequest request) {
        Assert.checkNotNull(request, "request");
        Assert.checkNotNull(request.getSubmissionType(), "request.submissionType");

        final SubmissionTypeEnum submissionType = Assert.checkNotNull(request.getSubmissionType(),
            "request.submissionType");

        final ProductEntity productEntity = this.productStore
                .get(Assert.checkHasText(request.getProductNumber(), "request.productNumber"));
        if (!productEntity.isSendable()) {
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.submission.notsendable",
                productEntity.getProductNumber()));
        }
        final SubmitterEntity submitterEntity = submitterStore.get(productEntity.getSubmitterId());

        // find and set the latest TPD-ID.
        String productId = Eucegs.UNDEFINED_PRODUCT_ID;
        String previousProductId = null;
        final SubmissionEntity latest = productEntity.getLastestSubmission();

        if (SubmissionTypeEnum.MODIFICATION_NEW.equals(submissionType) && latest != null) {
            previousProductId = latest.getProductId();
        }

        constraintRules.checkPreviousIDExistsOnModificationNew(previousProductId, submissionType);

        if (Strings.isNullOrEmpty(previousProductId)) { // exclude MODIFICATION_NEW -> previousProductId can not be null
            constraintRules.checkNewProductSubmissionIsPossible(productEntity, submissionType);
            if (latest != null) {
                // set with latest TPD-ID
                productId = latest.getProductId();
            }
        }

        // creation of submission
        Submission submission = null;
        if (productEntity.getProductType().equals(ProductType.ECIGARETTE)) {
            // create a copy of product because is mutable.
            final EcigProduct product = (EcigProduct) productEntity.getProduct().clone();
            product.withPreviousProductID(Eucegs.productNumber(previousProductId));
            submission = new EcigProductSubmission().withProduct(product)
                    .withGeneralComment(Eucegs.string1000(productEntity.getPreferredGeneralComment()))
                    .withSubmissionType(Eucegs.submissionType(submissionType))
                    .withSubmitter(submitterEntity.getSubmitter());
        } else if (productEntity.getProductType().equals(ProductType.TOBACCO)) {
            // create a copy of product because is mutable.
            final TobaccoProduct product = (TobaccoProduct) productEntity.getProduct().clone();
            product.withPreviousProductID(Eucegs.productNumber(previousProductId));
            submission = new TobaccoProductSubmission().withProduct(product)
                    .withGeneralComment(Eucegs.string1000(productEntity.getPreferredGeneralComment()))
                    .withSubmissionType(Eucegs.submissionType(submissionType))
                    .withSubmitter(submitterEntity.getSubmitter());
        } else {
            throw new RuntimeException("unknow product type:" + productEntity.getProductType());
        }

        return createSubmission(SubmissionEntity.builder()
                .product(productEntity)
                .productId(productId)
                .internalProductNumber(productEntity.getInternalProductNumber())
                .submissionType(request.getSubmissionType())
                .submission(submission)
                .sendType(request.getSendType())
                .submitterId(productEntity.getSubmitterId())
                .sentBy(authContext.getCurrentUser().map(IUser::getUsername).orElse(null))
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    @Nonnull
    public ProductEntity createProduct(@Nonnull final IProductEntity entity) {
        Assert.checkNotNull(entity, "entity");
        final ProductEntity product = productStore.create((ProductEntity) entity);
        return product;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    @Nonnull
    public IProductEntity saveProduct(@Nonnull final ProductUpdateRequest request) {
        Assert.checkNotNull(request, "request");
        // if (entity.isReadOnly()) {
        // throw new EucegException(
        // i18nService.createKeyedMessage("app.service.euceg.product.readonly",
        // entity.getId()));
        // }
        final ProductEntity original = this.productStore.get(request.getProductNumber());
        final String newGeneralComment = request.getGeneralComment();
        final String originalGeneralComment = original.getPreferredGeneralComment();
        // not update if products are equal
        if (Objects.equal(original.getProduct(), request.getProduct())
                && Objects.equal(original.getPreviousProductNumber(), request.getPreviousProductNumber())
                && Objects.equal(newGeneralComment, originalGeneralComment)) {
            return original;
        }

        final ProductEntity product = productStore.save(original.copy()
                .generalComment(request.getGeneralComment())
                .product(request.getProduct())
                .status(ProductStatus.IMPORTED)
                .lastModifiedDate(DateTime.now()) // enforce revision
                .build());
        this.productSubmissionStore.updateLastestSubmissionIfNotSend(product);
        return product;
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional
    @Nonnull
    public IProductEntity updatePirStatus(@Nonnull final PirStatusUpdateRequest request) {
        Assert.checkNotNull(request, "request");
        final ProductEntity entity = this.productStore
                .get(Assert.checkNotNull(request.getProductNumber(), "request.productNumber"));
        return this.productStore.save(entity.copy().pirStatus(request.getNewStatus()).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, EucegException.class })
    @Nonnull
    public ISubmissionEntity createOrSendSubmission(@Nonnull final SubmissionSendRequest request)
            throws EucegException {
        Assert.checkNotNull(request, "request");
        final SubmissionEntity submission = createSubmission(request);
        if (SendSubmissionType.IMMEDIAT.equals(request.getSendType())) {
            return sendSubmission(submission);
        }
        return submission;
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = { RuntimeException.class, EucegException.class })
    public void sendSubmission(final @Nonnull Long submissionId) throws EucegException {
        // TODO check already sent
        sendSubmission(this.productSubmissionStore.get(submissionId));
    }

    /**
     * @param submissionEntity
     *                         the submission to send
     * @return Returns a newly {@link SubmissionEntity} instance.
     * @throws AttachmentIsSendingException
     *                                      occurs when try send attachment that is sending.
     * @throws EucegException
     *                                      occurs if the {@link IBackendManager} has not started.
     */
    @Nonnull
    protected SubmissionEntity sendSubmission(@Nonnull SubmissionEntity submissionEntity)
            throws AttachmentIsSendingException {
        Assert.checkNotNull(submissionEntity, "submissionEntity");
        if (!backendManager.isRunning()) {
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.submission.backend.notstarted"));
        }

        // if (!TransmitStatus.isSendable(submissionEntity.getTransferStatus())) {
        // throw new EucegException(
        // i18nService.createKeyedMessage("app.service.euceg.submission.alreadysent",
        // productId));
        // }

        final SubmitterEntity submitter = this.submitterStore.get(submissionEntity.getSubmitterId());
        // check is submitter is valid or already sent
        if (!SubmitterStatus.VALID.equals(submitter.getStatus())
                && !SubmitterStatus.SENT.equals(submitter.getStatus())) {
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.submission.submitter.notvalid"));
        }

        constraintRules.checkAttachmentIsSending(submissionEntity, submitter);

        // - send submitter detail
        // submitter is valid but not yet send
        if (!SubmitterStatus.SENT.equals(submitter.getStatus())) {
            submissionEntity = submissionEntity.copy()
                    .receipt(TransmitReceiptEntity.create(submissionEntity,
                        PayloadType.SUBMITER_DETAILS,
                        submitter.getSubmitterId(),
                        RandomUtil.uuid(),
                        TransmitStatus.AWAITING))
                    .build();
        }

        // - send attachment submission (Submission.getAttachments() *
        // Attachment.status.sent == false) to send,
        final Set<String> uuids = submissionEntity.getAttachments().keySet();

        for (final String uuid : uuids) {
            final IAttachmentEntity attachmentEntity = this.attachmentStore.get(uuid);
            // not yet send
            if (!attachmentEntity.isSent(submitter)) {
                final TransmitReceiptEntity receiptAttachment = TransmitReceiptEntity.create(submissionEntity,
                    PayloadType.ATTACHMENT,
                    attachmentEntity.getFilename(),
                    RandomUtil.uuid(),
                    TransmitStatus.AWAITING);
                submissionEntity = submissionEntity.copy().receipt(receiptAttachment).build();
                // - for each attachments sent, flags as sent
                submissionEntity.getAttachments().put(uuid, true);

                // update attachment status
                attachmentService.updateSendStatus(attachmentEntity, submitter, AttachmentSendStatus.SENDING);
            }
        }

        // - set submission to awaiting state
        final TransmitReceiptEntity receiptSubmission = TransmitReceiptEntity.create(submissionEntity,
            PayloadType.SUBMISSION,
            submissionEntity.getProduct().getProductNumber(),
            RandomUtil.uuid(),
            TransmitStatus.AWAITING);

        submissionEntity = this.productSubmissionStore
                .save(submissionEntity.copy().receipts(receiptSubmission).build());

        publisher.publish(new UpdatedSubmissionEvent(submissionEntity));

        final ProductEntity product = submissionEntity.getProduct();
        this.publisher
                .publish(new SubmissiontCreatedEvent(this, submissionEntity.getId(), submissionEntity.getProductId(),
                        submissionEntity.getSubmissionType(), product.getProductNumber(), product.getProductType()));

        return submissionEntity;
    }

    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    public void rejectSubmission(@Nonnull final Long submissionId) {
        var entitySubmission = this.productSubmissionStore.get(Assert.checkNotNull(submissionId, "submissionId"));
        var receipts = Lists.newArrayList(entitySubmission.getReceipts())
                .stream()
                .filter(r -> TransmitStatus.PENDING.equals(r.getTransmitStatus()))
                .map(receipt -> receipt.accept(new IReceiptVisitor<TransmitReceiptEntity, TransmitReceiptEntity>() {

                    @Override
                    public TransmitReceiptEntity visitAttachment(final TransmitReceiptEntity receiptEntity) {
                        final SubmitterEntity submitterEntity = submitterStore.get(entitySubmission.getSubmitterId());
                        final String filename = receiptEntity.getName();
                        final IAttachmentEntity attachmentEntity = attachmentService.getAttachmentByFilename(filename);
                        attachmentService
                                .updateSendStatus(attachmentEntity, submitterEntity, AttachmentSendStatus.NO_SEND);
                        return receiptEntity.copy().transmitStatus(TransmitStatus.CANCELLED).build();
                    }

                    @Override
                    public TransmitReceiptEntity visitSubmitterDetail(final TransmitReceiptEntity receiptEntity) {
                        final SubmitterEntity submitterEntity = submitterStore.get(entitySubmission.getSubmitterId());
                        submitterStore.save(submitterEntity.copy().status(SubmitterStatus.VALID).build());
                        return receiptEntity.copy().transmitStatus(TransmitStatus.CANCELLED).build();
                    }

                    @Override
                    public TransmitReceiptEntity visitSubmission(final TransmitReceiptEntity receiptEntity) {
                        // noop
                        return receiptEntity;
                    }
                }))
                .filter(Predicates.notNull())
                .collect(Collectors.toList());
        publisher.publish(new UpdatedSubmissionEvent(
                this.productSubmissionStore.saveAndFlush(entitySubmission.copy().receipts(receipts).build())));
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    public void cancelSubmission(@Nonnull final Long submissionId) {
        var entitySubmission = this.productSubmissionStore.get(Assert.checkNotNull(submissionId, "submissionId"));
        // // Cancel All awaiting receipts
        var receipts = entitySubmission.getReceipts()
                .stream()
                .filter(r -> TransmitStatus.AWAITING.equals(r.getTransmitStatus())
                        || TransmitStatus.PENDING.equals(r.getTransmitStatus()))
                .map(r -> r.copy().transmitStatus(TransmitStatus.CANCELLED).build())
                .collect(Collectors.toList());
        entitySubmission = this.productSubmissionStore.saveAndFlush(entitySubmission.copy().receipts(receipts).build());

        final SubmitterEntity submitter = this.submitterStore.get(entitySubmission.getSubmitterId());
        // reset the sending for all cancelled attachment
        for (final ITransmitReceiptEntity receipt : entitySubmission.getReceiptByType(PayloadType.ATTACHMENT)) {
            if (TransmitStatus.CANCELLED.equals(receipt.getTransmitStatus())) {
                final IAttachmentEntity attachmentEntity = this.attachmentService
                        .getAttachmentByFilename(receipt.getName());
                this.attachmentService.updateSendStatus(attachmentEntity, submitter, AttachmentSendStatus.NO_SEND);
            }
        }
        // not yet sent submission can be cancelled
        if (SubmissionStatus.NOT_SEND.equals(entitySubmission.getSubmissionStatus())) {
            entitySubmission = this.productSubmissionStore
                    .save(entitySubmission.copy().submissionStatus(SubmissionStatus.CANCELLED).build());
        }
        publisher.publish(new UpdatedSubmissionEvent(entitySubmission));
    }

    private void cancelOldPendingSubmsissions(final DateTime fromDate) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Execute cancel old pending submission -> fromDate: {}", fromDate);
        }
        Streams.stream(this.productSubmissionStore.getPendingOrSubmittingSubmissionsBefore(fromDate))
                .map(SubmissionEntity::getId)
                .forEach(id -> {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Cancel submission -> id: {}", id);
                    }
                    requiresNewTransactionTemplate.execute(status -> {
                        cancelSubmission(id);
                        return null;
                    });

                });
    }

    /**
     * @author Christophe Friederich
     */
    private class CancelOldPendingSubmissionSchedulerJob implements IJobRunner {

        private final Duration awaitBeforeCancel;

        public CancelOldPendingSubmissionSchedulerJob(final Duration awaitBeforeCancel) {
            this.awaitBeforeCancel = awaitBeforeCancel == null ? Duration.ofDays(30) : awaitBeforeCancel;
        }

        @Nullable
        @Override
        public JobRunnerResponse runJob(@Nonnull final IJobRunnerRequest request) {
            final DateTime from = DateTime.now().minus(this.awaitBeforeCancel.toMillis());
            DefaultSubmissionService.this.cancelOldPendingSubmsissions(from);
            return JobRunnerResponse.success();
        }
    }

}
