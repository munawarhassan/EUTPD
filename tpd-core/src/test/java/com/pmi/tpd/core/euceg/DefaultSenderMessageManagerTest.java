package com.pmi.tpd.core.euceg;

import java.util.Optional;

import javax.inject.Provider;

import org.eu.ceg.AppResponse;
import org.eu.ceg.Attachment;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.ResponseStatus;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultSenderMessageManagerTest extends MockitoTestCase {

    @Mock
    private ISubmitterStore submitterStore;

    @Mock(lenient = true)
    private IProductSubmissionStore productSubmissionStore;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private IAttachmentService attachmentService;

    @Mock
    private IProductStore productStore;

    @Mock
    private IEventPublisher publisher;

    @Mock
    private IBackendManager backendManager;

    @Mock
    private ISubmissionService submissionService;

    private Provider<ISubmissionService> submissionServiceProvider;

    private Provider<IBackendManager> backendManagerProvider;

    private DefaultSenderManager senderMessageManager;

    @BeforeEach
    public void beforeEach() {
        backendManagerProvider = () -> backendManager;
        submissionServiceProvider = () -> submissionService;
        senderMessageManager = new DefaultSenderManager(platformTransactionManager, backendManagerProvider,
                submissionServiceProvider, attachmentService, submitterStore, productStore, productSubmissionStore,
                publisher) {

            @Override
            TransactionOperations createTransactionOperations(
                final PlatformTransactionManager platformTransactionManager,
                final TransactionDefinition transactionDefinition) {
                return TransactionOperations.withoutTransaction();
            }
        };
    }

    /**
    *
    */
    @Test
    public void shouldRejectMessageForTransmissionError() {
        final String messageId = RandomUtil.uuid();

        final TransmitReceiptEntity actualReceipt = TransmitReceiptEntity.create(SubmissionEntity.builder()
                .build(),
            PayloadType.ATTACHMENT,
            "attachment",
            messageId,
            TransmitStatus.PENDING);

        when(productSubmissionStore.findReceiptByMessageId(messageId)).thenReturn(Optional.of(actualReceipt));

        when(productSubmissionStore.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        senderMessageManager.handleMessageSendFailure(
            MessageSendFailure.builder().messageId(messageId).status(TransmitStatus.REJECTED).build());

        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        verify(productSubmissionStore).saveAndFlush(submissionCaptor.capture());

        final ISubmissionEntity submission = submissionCaptor.getValue();
        assertEquals(1, submission.getReceipts().size());

        final ITransmitReceiptEntity receipt = submission.getReceipts().get(0);

        assertEquals(TransmitStatus.REJECTED, receipt.getTransmitStatus());
        assertNull(receipt.getResponse());
    }

    @Test
    public void shouldUpdateReceiptForReceiveSuccessResponsse() {
        final String conversationId = RandomUtil.uuid();

        final TransmitReceiptEntity actualReceipt = TransmitReceiptEntity.create(SubmissionEntity.builder()
                .build(),
            PayloadType.SUBMISSION,
            "pom",
            conversationId,
            TransmitStatus.PENDING);

        when(productSubmissionStore.findReceiptByMessageId(conversationId)).thenReturn(Optional.of(actualReceipt));

        when(productSubmissionStore.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        senderMessageManager.handleResponse(Response.<AppResponse> builder()
                .conversationId(conversationId)
                .response(new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.SUCCESS))
                .build());

        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        verify(productSubmissionStore).saveAndFlush(submissionCaptor.capture());

        final ISubmissionEntity submission = submissionCaptor.getValue();
        assertEquals(1, submission.getReceipts().size());

        final ITransmitReceiptEntity receipt = submission.getReceipts().get(0);

        assertEquals(TransmitStatus.RECEIVED, receipt.getTransmitStatus());
    }

    @Test
    public void shouldRejectReceiptSubmissionOnSendAwaitPayloadWhenSubmissionIsInError() {

        final SubmissionEntity submission = SubmissionEntity.builder()
                .id(2121L)
                .submissionStatus(SubmissionStatus.ERROR)
                .build();

        final TransmitReceiptEntity attachmentReceipt = TransmitReceiptEntity
                .create(submission,
                    PayloadType.ATTACHMENT,
                    "pine needle oil.pdf",
                    RandomUtil.uuid(),
                    TransmitStatus.RECEIVED)
                .copy()
                .response(new ErrorResponse().withCode("ERR-RULES-0002-0002")
                        .withMessage(
                            "The value 'CREATE' of the 'action' attribute is not correct because the attachment ID already exists."))
                .build();

        final TransmitReceiptEntity submissionReceipt = TransmitReceiptEntity
                .create(submission, PayloadType.SUBMISSION, "POM.022003", RandomUtil.uuid(), TransmitStatus.AWAITING);

        final Page<TransmitReceiptEntity> pageReciepts = PageUtils.createPage(Lists.newArrayList(submissionReceipt),
            PageUtils.newRequest(0, 10));
        when(productSubmissionStore.getAwaitReceiptsToSend(any())).thenReturn(pageReciepts);

        when(productSubmissionStore.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        assertEquals(true, submission.isError(), "submission should be in error");
        senderMessageManager.sendAwaitPayload(5);

        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        verify(productSubmissionStore, times(1)).saveAndFlush(submissionCaptor.capture());

        final ISubmissionEntity updatedSubmission = submissionCaptor.getValue();
        assertEquals(2, updatedSubmission.getReceipts().size());

        assertEquals(TransmitStatus.REJECTED,
            updatedSubmission.getReceiptByType(PayloadType.SUBMISSION).get(0).getTransmitStatus(),
            "submission receipt should be rejected.");

    }

    @Test
    public void shouldSkipReceiptSubmissionOnSendAwaitPayloadWhenAttachmentisAwaiting() {

        // Given

        final String filename = "pine needle oil.pdf";
        final String submitterId = RandomUtil.uuid();

        final SubmissionEntity submission = SubmissionEntity.builder()
                .id(2121L)
                .submitterId(submitterId)
                .submissionStatus(SubmissionStatus.ERROR)
                .build();

        final TransmitReceiptEntity attachmentReceipt = TransmitReceiptEntity
                .create(submission, PayloadType.ATTACHMENT, filename, RandomUtil.uuid(), TransmitStatus.AWAITING);

        final IAttachmentEntity attEntity = AttachmentEntity.builder()
                .attachmentId(RandomUtil.uuid())
                .filename(filename)
                .build();

        final Attachment att = new Attachment();

        final SubmitterEntity submitterEntity = SubmitterEntity.builder().submitterId(submitterId).build();

        final TransmitReceiptEntity submissionReceipt = TransmitReceiptEntity
                .create(submission, PayloadType.SUBMISSION, "POM.022003", RandomUtil.uuid(), TransmitStatus.AWAITING);

        final Page<TransmitReceiptEntity> pageReciepts = PageUtils
                .createPage(Lists.newArrayList(submissionReceipt, attachmentReceipt), PageUtils.newRequest(0, 10));

        // When
        when(productSubmissionStore.getAwaitReceiptsToSend(any())).thenReturn(pageReciepts);

        when(productSubmissionStore.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        when(attachmentService.getAttachmentByFilename(eq(filename))).thenReturn(attEntity);

        when(submitterStore.get(eq(submitterId))).thenReturn(submitterEntity);

        when(attachmentService.createAttachment(eq(filename), eq(submitterEntity))).thenReturn(att);

        assertEquals(false, submission.isError(), "submission shouldn't be in error");
        senderMessageManager.sendAwaitPayload(5);

        // Then

        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        verify(productSubmissionStore, times(1)).saveAndFlush(submissionCaptor.capture());

        verify(backendManager, times(1)).sendPayload(any(), any());

        final ISubmissionEntity updatedSubmission = submissionCaptor.getValue();
        assertEquals(2, updatedSubmission.getReceipts().size());

        assertEquals(TransmitStatus.PENDING,
            updatedSubmission.getReceiptByType(PayloadType.ATTACHMENT).get(0).getTransmitStatus(),
            "attachment receipt should be pending.");

        assertEquals(TransmitStatus.AWAITING,
            updatedSubmission.getReceiptByType(PayloadType.SUBMISSION).get(0).getTransmitStatus(),
            "submission receipt should be awaiting.");

    }

    @Test
    public void shouldPendingReceiptSubmissionOnSendAwaitPayload() {

        final SubmissionEntity submission = SubmissionEntity.builder()
                .id(2121L)
                .productId("00007-16-00792")
                .submissionStatus(SubmissionStatus.NOT_SEND)
                .submissionType(SubmissionTypeEnum.CORRECTION)
                .product(ProductEntity.builder()
                        .productNumber(Eucegs.uuid())
                        .productType(ProductType.TOBACCO)
                        .submitterId("submitterId")
                        .status(ProductStatus.VALID)
                        .product(ProductSubmissionHelper.okFirstTobaccoProduct())
                        .build())
                .build();

        final TransmitReceiptEntity submissionReceipt = TransmitReceiptEntity
                .create(submission, PayloadType.SUBMISSION, "POM.022003", RandomUtil.uuid(), TransmitStatus.AWAITING);

        final Page<TransmitReceiptEntity> pageReciepts = PageUtils.createPage(Lists.newArrayList(submissionReceipt),
            PageUtils.newRequest(0, 10));
        when(productSubmissionStore.getAwaitReceiptsToSend(any())).thenReturn(pageReciepts);

        when(productSubmissionStore.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        assertEquals(false, submission.isError(), "submission shouldn't be in error");
        senderMessageManager.sendAwaitPayload(5);

        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        verify(productSubmissionStore, times(1)).saveAndFlush(submissionCaptor.capture());

        final ISubmissionEntity updatedSubmission = submissionCaptor.getValue();
        assertEquals(1, updatedSubmission.getReceipts().size());

        assertEquals(TransmitStatus.PENDING,
            updatedSubmission.getReceiptByType(PayloadType.SUBMISSION).get(0).getTransmitStatus(),
            "submission receipt should be pending.");

    }
}
