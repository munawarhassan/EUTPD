package com.pmi.tpd.core.euceg;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eu.ceg.AppResponse;
import org.eu.ceg.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.event.SubmissiontSentEvent;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.IReceiptVisitor;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.euceg.backend.core.message.MessageCurrentStatus;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.spring.transaction.SpringTransactionUtils;

@Singleton
public class DefaultSenderManager implements ISubmissionSenderManager {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSenderManager.class);

    /** */
    private final ISubmitterStore submitterStore;

    /** */
    private final IProductStore productStore;

    /** */
    private final IProductSubmissionStore productSubmissionStore;

    /** */
    private final Provider<IBackendManager> backendManager;

    /** */
    private final IAttachmentService attachmentService;

    /** */
    private final Provider<ISubmissionService> submissionServiceProvider;

    /** */
    private final IEventPublisher publisher;

    /** manage transaction manually to include transaction in locked block. */
    private final TransactionOperations requiresNew;

    /** manage transaction manually to include transaction in locked block. */
    private final TransactionOperations requiredTransaction;

    /** */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private final Lock writeLock = lock.writeLock();

    private final Lock readLock = lock.readLock();

    @Inject
    public DefaultSenderManager(@Nonnull final PlatformTransactionManager platformTransactionManager,
            @Nonnull final Provider<IBackendManager> backendManager,
            @Nonnull final Provider<ISubmissionService> submissionServiceProvider,
            @Nonnull final IAttachmentService attachmentService, @Nonnull final ISubmitterStore submitterStore,
            @Nonnull final IProductStore productStore, @Nonnull final IProductSubmissionStore productSubmissionStore,
            @Nonnull final IEventPublisher publisher) {
        this.requiresNew = createTransactionOperations(platformTransactionManager, SpringTransactionUtils.REQUIRES_NEW);
        this.requiredTransaction = createTransactionOperations(platformTransactionManager,
            SpringTransactionUtils.definitionFor(TransactionDefinition.PROPAGATION_REQUIRED));
        this.backendManager = Assert.checkNotNull(backendManager, "backendManager");
        this.submissionServiceProvider = Assert.checkNotNull(submissionServiceProvider, "submissionServiceProvider");
        this.attachmentService = Assert.checkNotNull(attachmentService, "attachmentService");
        this.submitterStore = Assert.checkNotNull(submitterStore, "submitterStore");
        this.productStore = Assert.checkNotNull(productStore, "productStore");
        this.productSubmissionStore = Assert.checkNotNull(productSubmissionStore, "productSubmissionStore");
        this.publisher = Assert.checkNotNull(publisher, "publisher");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleSubmitResponse(final @Nonnull SubmitResponse message) {
        // there is not specific action on submitresponse except on error
        if (!message.isErrorMessage()) {
            return;
        }
        // escape message without messageId
        if (Strings.isNullOrEmpty(message.getMessageId())) {
            return;
        }
        // lock outside of transaction
        writeLock.lock();
        try {
            this.requiredTransaction.<Void> execute(status -> {
                updateReceipt(message.getMessageId(), TransmitStatus.REJECTED, null);
                return null;
            });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessageSent(final @Nonnull MessageSent message) {
        // lock outside of transaction
        writeLock.lock();
        try {
            this.requiredTransaction.<Void> execute(status -> {
                final String conversationId = message.getMessageId();
                productSubmissionStore.findReceiptByMessageId(conversationId).ifPresentOrElse(receipt -> {
                    final SubmissionEntity submission = receipt.getSubmission();
                    final SubmitterEntity submitter = this.submitterStore.get(submission.getSubmitterId());
                    receipt.accept(new IReceiptVisitor<TransmitReceiptEntity, Void>() {

                        @Override
                        public Void visitSubmitterDetail(final TransmitReceiptEntity receiptEntity) {
                            // TODO maybe add sending status to submitter
                            submitterStore.save(submitter.copy().status(SubmitterStatus.SENT).build());
                            return null;
                        }

                        @Override
                        public Void visitAttachment(final TransmitReceiptEntity receiptEntity) {
                            return null;
                        }

                        @Override
                        public Void visitSubmission(final TransmitReceiptEntity receiptEntity) {
                            // TODO maybe add sending status to product
                            final ProductEntity product = productStore
                                    .save(submission.getProduct().copy().status(ProductStatus.SENT).build());
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("Product {} Status Updated: {}",
                                    product.getProductNumber(),
                                    product.getStatus());
                            }
                            return null;
                        }

                    });
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Payload {} sent -> status updated: {}",
                            receipt.getType(),
                            receipt.getTransmitStatus());
                    }
                }, () -> LOGGER.warn("The Send Message with conversationId {} is not recognized.", conversationId));

                return null;
            });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleResponse(final @Nonnull Response<AppResponse> message) {
        // lock outside of transaction
        writeLock.lock();
        try {
            this.requiredTransaction.<Void> execute(status -> {
                message.getResponses()
                        .forEach(resp -> updateReceipt(message.getConversationId(), TransmitStatus.from(resp), resp));
                return null;
            });
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessageReceiveFailure(final @Nonnull MessageReceiveFailure message) {
        // lock outside of transaction
        writeLock.lock();
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Message receive failure: messageId {}, status {}",
                    message.getMessageId(),
                    message.getStatus());
            }
            this.requiredTransaction.<Void> execute(status -> {
                updateReceipt(message.getMessageId(), message.getStatus(), null);
                return null;
            });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessageSendFailure(final @Nonnull MessageSendFailure message) {
        // lock outside of transaction
        writeLock.lock();
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Message send failure: messageId {}, status {}",
                    message.getMessageId(),
                    message.getStatus());
            }
            this.requiredTransaction.<Void> execute(status -> {
                updateReceipt(message.getMessageId(), message.getStatus(), null);
                return null;
            });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCurrentStatus(@Nonnull final MessageCurrentStatus message) {
        readLock.lock();
        try {
            this.requiredTransaction.<Void> execute(status -> {
                // publish current submission status
                productSubmissionStore.findReceiptByMessageId(message.getMessageId()).ifPresent(receipt -> {
                    final ISubmissionEntity submission = receipt.getSubmission();
                    if (submission != null && SubmissionStatus.PENDING.equals(submission.getSubmissionStatus())) {
                        // update publish only for pending submission
                        this.publisher.publish(new UpdatedSubmissionEvent(receipt.getSubmission()));
                    }
                });
                return null;
            });
        } finally {
            readLock.unlock();
        }

    }

    @Override
    @Transactional(readOnly = true)
    public void sendDeferredSubmission(final int batchSize) {

        // lock outside of transaction
        writeLock.lock();
        try {

            final Iterable<Long> submissions = this.productSubmissionStore.getDeferredSubmissions(batchSize);
            for (final Long id : submissions) {
                requiresNew.execute(status -> {
                    try {
                        if (id != null && submissionServiceProvider.get() != null) {
                            submissionServiceProvider.get().sendSubmission(id);
                        }
                    } catch (

                    final EucegException ex) {
                        LOGGER.warn(ex.getMessage(), ex);
                        // continue
                    }
                    return null;
                });
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendAwaitPayload(final int batchSize) {
        // lock outside of transaction
        writeLock.lock();
        try {
            requiredTransaction.<Void> execute(status -> {
                final Page<TransmitReceiptEntity> receipts = this.productSubmissionStore
                        .getAwaitReceiptsToSend(PageUtils.newRequest(0, batchSize));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Call send await payload job -> numberOfReceipts: {}", receipts.getNumberOfElements());
                }
                for (final TransmitReceiptEntity receipt : receipts) {
                    sendAwaitPayloadInTransaction(receipt);
                }
                return null;
            });

        } finally {
            writeLock.unlock();
        }

    }

    private Void sendAwaitPayloadInTransaction(final TransmitReceiptEntity receipt) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Send AwaitPayload from receipt: {}", receipt);
        }

        final SubmissionEntity submission = receipt.getSubmission();

        // reject receipt is submission is in error.
        if (submission.isError()) {
            final SubmissionEntity updatedSubmission = this.productSubmissionStore.saveAndFlush(
                submission.copy().receipt(receipt.copy().transmitStatus(TransmitStatus.REJECTED).build()).build());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("SendAwaitPayload -> Submission is in Error, Receipt has been Rejected: {}", receipt);
            }
            publisher.publish(new UpdatedSubmissionEvent(updatedSubmission));
            return null;
        }

        final boolean updateReceipt = receipt.accept(new IReceiptVisitor<TransmitReceiptEntity, Boolean>() {

            @Override
            public Boolean visitAttachment(final TransmitReceiptEntity receiptEntity) {
                sendPayloadAttachment(receipt);
                return true;
            }

            @Override
            public Boolean visitSubmitterDetail(final TransmitReceiptEntity receiptEntity) {
                sendPayloadSubmitterDetail(receipt);
                return true;
            }

            @Override
            public Boolean visitSubmission(final TransmitReceiptEntity receiptEntity) {
                if (isSubmissionReadyToSend(receipt)) {
                    sendPayloadSubmission(receipt);
                    return true;
                }
                return false;
            }
        });

        if (updateReceipt) {
            final TransmitReceiptEntity updatedReceipt = receipt.copy().transmitStatus(TransmitStatus.PENDING).build();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("SendAwaitPayload -> Update Receipt: {}", updatedReceipt);
            }
            SubmissionEntity submissionCopy = submission.copy().receipt(updatedReceipt).build();
            submissionCopy = this.productSubmissionStore.saveAndFlush(submission);
            publisher.publish(new UpdatedSubmissionEvent(submissionCopy));
        }

        return null;
    }

    private void updateReceipt(@Nonnull final String conversationId,
        @Nonnull final TransmitStatus status,
        @Nullable final AppResponse response) {

        // the conversion id corresponds to message id. see
        // BackendService#createMessaging
        final Optional<TransmitReceiptEntity> receiptOptional = productSubmissionStore
                .findReceiptByMessageId(conversationId);
        if (receiptOptional.isEmpty()) {
            LOGGER.warn("The Retrieved response with conversationId {} is not recognized.", conversationId);
            return;
        }
        TransmitReceiptEntity receipt = receiptOptional.get();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Received Payload {} -> {}, state '{}' with messageId '{}', new status: {}",
                receipt.getType(),
                receipt.getName(),
                receipt.getTransmitStatus(),
                conversationId,
                status);
        }
        SubmissionEntity submission = receipt.getSubmission();
        if (response == null) {
            receipt = receipt.copy().transmitStatus(status).build();
        } else {
            receipt = receipt.copy()
                    .response(response)
                    .responseMessageId(response.getUuid())
                    .transmitStatus(status)
                    .build();
        }

        submission = this.productSubmissionStore.saveAndFlush(submission.copy().receipts(receipt).build());

        // synchronize product associated to submission
        applyProductStatus(receipt);
        // if receipt concerns a attachment, then propagate the status
        applyAttachmentSendStatus(receipt);
        // publish the current status of submission
        this.publisher.publish(new UpdatedSubmissionEvent(submission));

    }

    private void applyAttachmentSendStatus(final TransmitReceiptEntity receipt) {
        if (PayloadType.ATTACHMENT.equals(receipt.getType())) {
            final SubmitterEntity submitter = this.submitterStore.get(receipt.getSubmission().getSubmitterId());
            final IAttachmentEntity attachmentEntity = this.attachmentService
                    .getAttachmentByFilename(receipt.getName());
            // after set the attachment status as sent if no error
            // otherwise, set the attachment status as no send
            final AttachmentSendStatus sendStatus = !receipt.isError() ? AttachmentSendStatus.SENT
                    : AttachmentSendStatus.NO_SEND;
            this.attachmentService.updateSendStatus(attachmentEntity, submitter, sendStatus);
        }
    }

    private void applyProductStatus(final TransmitReceiptEntity receipt) {
        // synchronize product associated to submission
        if (PayloadType.SUBMISSION.equals(receipt.getType())) {
            final SubmissionEntity submission = receipt.getSubmission();
            final SubmissionStatus submissionStatus = submission.getSubmissionStatus();
            if (SubmissionStatus.CANCELLED.equals(submissionStatus) || SubmissionStatus.ERROR.equals(submissionStatus)
                    || SubmissionStatus.SUBMITTED.equals(submissionStatus)) {
                ProductEntity product = submission.getProduct();
                if (SubmissionStatus.SUBMITTED.equals(submissionStatus)) {
                    product = product.copy().status(ProductStatus.SENT).build();
                }
                // enforce product update to synchronize product status and PIR status
                this.productStore.updateOnSubmission(product);
            }
        }
    }

    private void sendPayloadSubmitterDetail(final ITransmitReceiptEntity receipt) {
        final String messageId = receipt.getMessageId();
        final ISubmissionEntity submission = receipt.getSubmission();
        final SubmitterEntity submitter = this.submitterStore.get(submission.getSubmitterId());
        backendManager.get().sendPayload(messageId, submitter.getXmlSubmitterDetail());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Send Submitter {}, state '{}' with messageId '{}'",
                submission.getProductId(),
                submitter.getStatus(),
                messageId);
        }
    }

    private void sendPayloadAttachment(final ITransmitReceiptEntity receipt) {
        final String messageId = receipt.getMessageId();
        final ISubmissionEntity submission = receipt.getSubmission();
        final SubmitterEntity submitter = this.submitterStore.get(submission.getSubmitterId());
        // get attachment
        final String filename = receipt.getName();
        final IAttachmentEntity attachmentEntity = this.attachmentService.getAttachmentByFilename(filename);
        final Attachment attachment = attachmentService.createAttachment(filename, submitter);
        // send attachment
        backendManager.get().sendPayload(messageId, attachment);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Send Attachemnt {}, state '{}' with messageId '{}'",
                filename,
                attachmentEntity.getStatus(submitter),
                messageId);
        }
    }

    private void sendPayloadSubmission(final TransmitReceiptEntity receipt) {
        final String messageId = receipt.getMessageId();
        final SubmissionEntity submission = receipt.getSubmission();
        backendManager.get().sendPayload(messageId, submission.getXmlSubmission());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Send Submission {}, state '{}' with messageId '{}'",
                submission.getProductId(),
                submission.getSubmissionStatus(),
                messageId);
        }
        final ProductEntity product = submission.getProduct();
        this.publisher.publish(new SubmissiontSentEvent(this, submission.getId(), submission.getProductId(),
                submission.getSubmissionType(), product.getProductNumber(), product.getProductType()));
    }

    /**
     * @param receipt
     * @return
     */
    @VisibleForTesting
    boolean isSubmissionReadyToSend(final TransmitReceiptEntity receipt) {
        final SubmissionEntity submission = receipt.getSubmission();
        if (submission.isError()) {
            return false;
        }
        // if all have been sent except submission
        return submission.getReceipts()
                .stream()
                .filter(r -> !r.equals(receipt))
                .allMatch(r -> TransmitStatus.RECEIVED.equals(r.getTransmitStatus()));
    }

    TransactionOperations createTransactionOperations(final PlatformTransactionManager platformTransactionManager,
        final TransactionDefinition transactionDefinition) {
        return new TransactionTemplate(platformTransactionManager, transactionDefinition);
    }

}
