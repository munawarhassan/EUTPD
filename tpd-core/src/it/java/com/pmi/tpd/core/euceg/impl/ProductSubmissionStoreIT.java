package com.pmi.tpd.core.euceg.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.RandomStringUtils;
import org.eu.ceg.AppResponse;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.ResponseStatus;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductSubmission;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.euceg.ProductSubmissionHelper;
import com.pmi.tpd.core.euceg.spi.IProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.spi.IProductRepository;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionRepository;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IPayloadEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

@Configuration
@ContextConfiguration(classes = { ProductSubmissionStoreIT.class })
public class ProductSubmissionStoreIT extends BaseDaoTestIT {

    @Inject
    private IProductSubmissionStore store;

    @Inject
    private IProductStore productStore;

    @Bean
    public IProductSubmissionRepository productSubmissionRepository(final EntityManager entityManager) {
        return new JpaProductSubmissionRepository(entityManager);
    }

    @Bean
    public IProductRepository productRepository(final EntityManager entityManager) {
        return new JpaProductRepository(entityManager);
    }

    @Bean
    public IProductIdGeneratorRepository productIdGeneratorRepository(final EntityManager entityManager) {
        return new JpaProductIdGeneratorRepository(entityManager);
    }

    @Bean
    public IProductSubmissionStore productSubmissionStore(final IProductSubmissionRepository repository,
        final IProductIdGeneratorRepository productIdGeneratorRepository) {
        return new ProductSubmissionStore(repository, productIdGeneratorRepository);
    }

    @Bean
    public IProductStore productStore(final IProductRepository repository) {
        return new ProductStore(repository);
    }

    @Test
    public void checkProductIdGeneration() {
        final String submitterId = RandomStringUtils.randomNumeric(5);
        final ProductEntity product = productStore.create(createTobaccoProduct(submitterId).build());
        final ISubmissionEntity submission = store
                .create(createTobaccoSubmission(submitterId).product(product).build());
        assertThat("The id is required", submission.getId(), IsNull.notNullValue());
        assertThat("The productId must be correctly generated",
            submission.getProductId(),
            Is.is(SubmissionProductIdGenerator.generate(submitterId, 1)));
    }

    // TODO replace with findAll method
    // @Test
    // public void searchProductIds() {
    // SubmissionEntity submission = null;
    // for (int i = 1; i < 50; i++) {
    // final String submitterId = String.format("%05d", i % 10);
    // final ProductEntity product =
    // productStore.create(createTobaccoProduct(submitterId).build());
    // submission =
    // store.create(createTobaccoSubmission(submitterId).product(product).build());
    // }
    //
    // // search with for specific submitter
    // List<String> list = store.searchProductIds(ProductType.TOBACCO, "00001");
    // assertThat("Exists productid for '00001' submitter", list.size(), Is.is(5));
    //
    // // search with exact productId
    // list = store.searchProductIds(ProductType.TOBACCO,
    // submission.getProductId());
    // assertThat(list.size(), Is.is(1));
    //
    // // search with exact productId for all types
    // list = store.searchProductIds(null, "00001");
    // assertThat(list.size(), Is.is(5));
    //
    // // search unknown submitter
    // list = store.searchProductIds(ProductType.TOBACCO, "99999");
    // assertThat("the list must be empty", list, IsEmptyCollection.empty());
    //
    // // no search term
    // list = store.searchProductIds(null, null);
    // assertThat("the list must be empty", list, IsEmptyCollection.empty());
    // }

    /**
     *
     */
    @Test
    public void getPendingMessageIdsWithDifferentSubmitter() {
        // create new submission for first submitter, with Submission PENDING
        // n = 1 pending
        final String submitterId1 = "submitter1";
        final ProductEntity product = productStore.create(createTobaccoProduct(submitterId1).build());
        SubmissionEntity submission1 = store.create(createTobaccoSubmission(submitterId1).product(product).build());
        submission1 = submission1.copy()
                .receipt(TransmitReceiptEntity.create(submission1,
                    PayloadType.SUBMISSION,
                    submission1.getProductId(),
                    Eucegs.uuid(),
                    TransmitStatus.PENDING))
                .build();
        store.saveAndFlush(submission1);
        // create new submission for second submitter, with Submission PENDING and one
        // attachment RECEIVED
        // n = 2 pendings (without attachment because RECEIVED)
        final String submitterId2 = "submitter2";
        final ProductEntity product2 = productStore.create(createTobaccoProduct(submitterId2).build());
        final SubmissionEntity submission2 = store
                .create(createTobaccoSubmission(submitterId2).product(product2).build());
        submission2.getReceipts()
                .addAll(Arrays.asList(
                    TransmitReceiptEntity.create(submission2,
                        PayloadType.SUBMISSION,
                        submission2.getProductId(),
                        Eucegs.uuid(),
                        TransmitStatus.PENDING),
                    TransmitReceiptEntity.create(submission2,
                        PayloadType.ATTACHMENT,
                        submission2.getProductId(),
                        Eucegs.uuid(),
                        TransmitStatus.RECEIVED)));
        store.saveAndFlush(submission2);
        // create new submission for second submitter, with Submission RECEIVED
        // n = 2 pendings (without submission because RECEIVED)
        final String submitterId3 = "submitter3";
        final ProductEntity product3 = productStore.create(createTobaccoProduct(submitterId3).build());
        SubmissionEntity submission3 = store.create(createTobaccoSubmission(submitterId3).product(product3).build());
        submission3 = submission3.copy()
                .receipt(TransmitReceiptEntity.create(submission3,
                    PayloadType.SUBMISSION,
                    submission3.getProductId(),
                    Eucegs.uuid(),
                    TransmitStatus.RECEIVED))
                .build();
        store.saveAndFlush(submission3);
        final Set<String> set = store.getPendingMessageIds();
        assertEquals(2, set.size());
    }

    @Test
    public void getPendingMessageIdsWithSameSubmitter() {
        // create PENDING submission
        // n = 1 pending
        final String submitterId = "submitter1";
        final ProductEntity product = productStore.create(createTobaccoProduct(submitterId).build());
        final SubmissionEntity submission1 = store
                .create(createTobaccoSubmission(submitterId).product(product).build());
        submission1.getReceipts()
                .add(TransmitReceiptEntity.create(submission1,
                    PayloadType.SUBMISSION,
                    submission1.getProductId(),
                    Eucegs.uuid(),
                    TransmitStatus.PENDING));
        store.saveAndFlush(submission1);
        // create PENDING submission and PENDING attachment
        // n = 3 pending
        final ProductEntity product2 = productStore.create(createTobaccoProduct(submitterId).build());
        final SubmissionEntity submission2 = store
                .create(createTobaccoSubmission(submitterId).product(product2).build());
        submission2.getReceipts()
                .addAll(Arrays.asList(
                    TransmitReceiptEntity.create(submission2,
                        PayloadType.SUBMISSION,
                        submission2.getProductId(),
                        Eucegs.uuid(),
                        TransmitStatus.PENDING),
                    TransmitReceiptEntity.create(submission2,
                        PayloadType.ATTACHMENT,
                        submission2.getProductId(),
                        Eucegs.uuid(),
                        TransmitStatus.PENDING)));
        store.saveAndFlush(submission2);
        // create RECEIVED submission
        // n = 3 pending
        final ProductEntity product3 = productStore.create(createTobaccoProduct(submitterId).build());
        SubmissionEntity submission3 = store.create(createTobaccoSubmission(submitterId).product(product3).build());
        submission3 = submission3.copy()
                .receipt(TransmitReceiptEntity.create(submission3,
                    PayloadType.SUBMISSION,
                    submission3.getProductId(),
                    Eucegs.uuid(),
                    TransmitStatus.RECEIVED))
                .build();
        store.saveAndFlush(submission3);
        final Set<String> set = store.getPendingMessageIds();
        assertEquals(3, set.size());
    }

    @Test
    public void testSynchronizationBetweenEntityAndTobaccoSubmission() {
        final ProductEntity productEntity = this.productStore.create(createTobaccoProduct("submitterId").build());
        SubmissionEntity entity = createTobaccoSubmission("submitterId").product(productEntity).build();
        assertEquals(Eucegs.UNDEFINED_PRODUCT_ID,
            ((TobaccoProductSubmission) entity.getSubmission()).getProduct().getProductID().getValue());
        entity = this.store.create(entity);
        assertEquals(entity.getProductId(),
            ((TobaccoProductSubmission) entity.getSubmission()).getProduct().getProductID().getValue(),
            "the entity product id and the product id of product must be the same");

    }

    @Test
    public void getExistingOne() {
        final ProductEntity productEntity = this.productStore.create(createTobaccoProduct("submitterId").build());
        SubmissionEntity entity = createTobaccoSubmission("submitterId").product(productEntity).build();
        entity = this.store.create(entity);
        assertEquals(entity.getId(), this.store.get(entity.getId()).getId());
        assertTrue(this.store.exists(entity.getId()));
    }

    @Test
    public void getNotFoundOne() {
        assertThrows(EntityNotFoundException.class, () -> this.store.get(Long.MIN_VALUE));
    }

    @Test
    public void testSynchronizationBetweenEntityAndEcigSubmission() {
        final ProductEntity productEntity = this.productStore.create(createEcigProduct("submitterId").build());
        SubmissionEntity entity = createEcigSubmission("submitterId").product(productEntity).build();
        assertEquals(Eucegs.UNDEFINED_PRODUCT_ID,
            ((EcigProductSubmission) entity.getSubmission()).getProduct().getProductID().getValue());
        entity = this.store.save(entity);
        assertEquals(entity.getProductId(),
            ((EcigProductSubmission) entity.getSubmission()).getProduct().getProductID().getValue(),
            "the entity product id and the product id of product must be the same");

    }

    @Test
    public void createSubmissionWithProductId() {
        final ProductEntity productEntity = this.productStore.create(createEcigProduct("submitterId").build());
        SubmissionEntity entity = createEcigSubmission("submitterId").product(productEntity)
                .productId("12345-12-12345")
                .build();
        assertEquals(Eucegs.UNDEFINED_PRODUCT_ID,
            ((EcigProductSubmission) entity.getSubmission()).getProduct().getProductID().getValue());
        entity = this.store.save(entity);
        assertEquals("12345-12-12345",
            ((EcigProductSubmission) entity.getSubmission()).getProduct().getProductID().getValue(),
            "the entity product id and the product id of product must be the same");
    }

    @Test
    public void updateReceiptStatus() {
        final String submitterId = "submitterId";
        final String messageId = RandomUtil.uuid();

        final ProductEntity productEntity = this.productStore.create(createEcigProduct(submitterId).build());
        SubmissionEntity entity = this.store.create(createEcigSubmission(submitterId).product(productEntity).build());
        entity = entity.copy()
                .receipt(TransmitReceiptEntity.create(entity,
                    PayloadType.SUBMISSION,
                    entity.getProductId(),
                    messageId,
                    TransmitStatus.PENDING))
                .build();
        this.store.save(entity);
        TransmitReceiptEntity receiptEntity = this.store.findReceiptByMessageId(messageId).orElseThrow();
        receiptEntity = receiptEntity.copy().transmitStatus(TransmitStatus.DELETED).build();
        entity = this.store.save(receiptEntity.getSubmission());
        receiptEntity = Iterables.getFirst(entity.getReceipts(), null);
        assertEquals(TransmitStatus.DELETED, receiptEntity.getTransmitStatus());
        assertEquals(PayloadType.SUBMISSION, receiptEntity.getType());
        assertEquals(messageId, receiptEntity.getMessageId());
    }

    /**
     *
     */
    @Test
    public void updateReceiptResponse() {
        final String submitterId = "submitterId";
        final String messageId = RandomUtil.uuid();

        final ProductEntity productEntity = this.productStore.create(createEcigProduct(submitterId).build());
        SubmissionEntity entity = this.store.create(createEcigSubmission(submitterId).product(productEntity).build());
        entity = entity.copy()
                .receipt(TransmitReceiptEntity.create(entity,
                    PayloadType.SUBMISSION,
                    entity.getProductId(),
                    messageId,
                    TransmitStatus.PENDING))
                .build();
        this.store.save(entity);
        // success response
        TransmitReceiptEntity receiptEntity = this.store.findReceiptByMessageId(messageId).orElseThrow();
        AppResponse response = new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.SUCCESS);
        receiptEntity = receiptEntity.copy().response(response).transmitStatus(TransmitStatus.from(response)).build();

        entity = this.store.save(receiptEntity.getSubmission());
        receiptEntity = Iterables.getFirst(entity.getReceipts(), null);

        assertEquals(TransmitStatus.RECEIVED, receiptEntity.getTransmitStatus());
        assertEquals(PayloadType.SUBMISSION, receiptEntity.getType());
        assertEquals(messageId, receiptEntity.getMessageId());
        assertFalse(receiptEntity.isError());
        assertEquals(PayloadType.SUBMISSION, receiptEntity.getResponseType());

        // error response
        receiptEntity = this.store.findReceiptByMessageId(messageId).orElseThrow();
        response = new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.ERROR);
        receiptEntity = receiptEntity.copy().response(response).transmitStatus(TransmitStatus.from(response)).build();

        entity = this.store.save(receiptEntity.getSubmission());
        receiptEntity = Iterables.getFirst(entity.getReceipts(), null);

        assertEquals(TransmitStatus.REJECTED, receiptEntity.getTransmitStatus());
        assertEquals(PayloadType.SUBMISSION, receiptEntity.getType());
        assertEquals(messageId, receiptEntity.getMessageId());
        assertTrue(receiptEntity.isError());
        assertEquals(PayloadType.SUBMISSION, receiptEntity.getResponseType());

    }

    @Test
    public void testProgressIndicator() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity submission = createTobaccoSubmission("12345").product(product).build();
        submission = this.store.save(submission);
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());
        assertEquals(0.0f, submission.getProgress(), 0.0f);
        // AWAITING
        TransmitReceiptEntity receipt = TransmitReceiptEntity.create(submission,
            PayloadType.SUBMISSION,
            submission.getProductId(),
            RandomUtil.uuid(),
            TransmitStatus.AWAITING);
        submission = submission.copy().receipt(receipt).build();
        submission = this.store.save(submission);
        receipt = getReceiptByMessageId(submission.getReceipts(), receipt.getMessageId());
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());
        assertEquals(0.0f, submission.getProgress(), 0.0f, "awaiting status must be skipped");

        // attachement pending
        TransmitReceiptEntity attReceipt = TransmitReceiptEntity.create(submission,
            PayloadType.ATTACHMENT,
            "attachment.pdf",
            RandomUtil.uuid(),
            TransmitStatus.PENDING);
        submission = submission.copy().receipt(attReceipt).build();
        submission = this.store.save(submission);

        attReceipt = getReceiptByMessageId(submission.getReceipts(), attReceipt.getMessageId());
        assertEquals(2, submission.getReceipts().size());
        assertEquals(0.0f, submission.getProgress(), 0.0f);

        TransmitReceiptEntity attReceived = TransmitReceiptEntity.create(submission,
            PayloadType.ATTACHMENT,
            "attachment-received.pdf",
            RandomUtil.uuid(),
            TransmitStatus.PENDING);
        submission = submission.copy().receipt(attReceived).build();
        submission = this.store.save(submission);

        attReceived = getReceiptByMessageId(submission.getReceipts(), attReceived.getMessageId());
        assertEquals(3, submission.getReceipts().size());
        assertEquals(0.0f, submission.getProgress(), 0.1f);

        TransmitReceiptEntity attRejected = TransmitReceiptEntity.create(submission,
            PayloadType.ATTACHMENT,
            "attachment-rejected.pdf",
            RandomUtil.uuid(),
            TransmitStatus.PENDING);
        submission = submission.copy().receipt(attRejected).build();
        submission = this.store.save(submission);

        attRejected = getReceiptByMessageId(submission.getReceipts(), attRejected.getMessageId());
        assertEquals(4, submission.getReceipts().size());
        assertEquals(0.0f, submission.getProgress(), 0.0f);

        attReceived = attReceived.copy().transmitStatus(TransmitStatus.RECEIVED).build();
        submission = submission.copy().receipt(attReceived).build();
        submission = this.store.save(submission);

        assertEquals(4, submission.getReceipts().size());
        assertEquals(0.25f, submission.getProgress(), 0.0f); // 1/4

        attRejected = attRejected.copy().transmitStatus(TransmitStatus.REJECTED).build();
        submission = submission.copy().receipt(attRejected).build();
        submission = this.store.save(submission);

        assertEquals(4, submission.getReceipts().size());
        assertEquals(0.5f, submission.getProgress(), 0.0f); // 2/4

        // set pending attachment as REJECTED
        attReceipt = attReceipt.copy().transmitStatus(TransmitStatus.REJECTED).build();
        submission = submission.copy().receipts(attReceipt).build();
        submission = this.store.save(submission);

        assertEquals(4, submission.getReceipts().size());
        assertEquals(0.75f, submission.getProgress(), 0.0f); // 3/4

        // set submission as PENDING
        receipt = receipt.copy().transmitStatus(TransmitStatus.PENDING).build();
        submission = submission.copy().receipts(receipt).build();
        submission = this.store.save(submission);

        assertEquals(0.75f, submission.getProgress(), 0.0f);

        // set submission as RECEIVED
        receipt = receipt.copy().transmitStatus(TransmitStatus.RECEIVED).build();
        submission = submission.copy().receipt(receipt).build();
        submission = this.store.save(submission);

        assertEquals(1.0f, submission.getProgress(), 0.0f); // 4/4
    }

    @Test
    public void testSubmissionDeferredStatusNotSend() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity submission = createTobaccoSubmission("12345", SendSubmissionType.DEFERRED).product(product)
                .build();
        submission = this.store.save(submission);
        assertEquals(SubmissionStatus.NOT_SEND, submission.getSubmissionStatus());

    }

    @Test
    public void testSubmissionManualStatusNotSend() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity submission = createTobaccoSubmission("12345", SendSubmissionType.MANUAL).product(product)
                .build();
        submission = this.store.save(submission);
        assertEquals(SubmissionStatus.NOT_SEND, submission.getSubmissionStatus());

    }

    @Test
    public void testSubmissionStatusInError() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity submission = createTobaccoSubmission("12345").product(product).build();
        submission = this.store.save(submission);
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        TransmitReceiptEntity receipt = TransmitReceiptEntity.create(submission,
            PayloadType.SUBMISSION,
            submission.getProductId(),
            RandomUtil.uuid(),
            TransmitStatus.AWAITING);
        submission = submission.copy().receipt(receipt).build();
        submission = this.store.save(submission);

        receipt = getReceiptByMessageId(submission.getReceipts(), receipt.getMessageId());
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        TransmitReceiptEntity attRejected = TransmitReceiptEntity.create(submission,
            PayloadType.ATTACHMENT,
            "attachment-rejected.pdf",
            RandomUtil.uuid(),
            TransmitStatus.PENDING);
        submission = submission.copy().receipts(attRejected).build();
        submission = this.store.save(submission);

        attRejected = getReceiptByMessageId(submission.getReceipts(), attRejected.getMessageId());
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        attRejected = attRejected.copy().transmitStatus(TransmitStatus.REJECTED).build();
        submission = submission.copy().receipts(attRejected).build();
        submission = this.store.save(submission);

        assertEquals(SubmissionStatus.ERROR, submission.getSubmissionStatus());
    }

    @Test
    public void testSubmissionStatusSubmitted() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity submission = createTobaccoSubmission("12345").product(product).build();
        submission = this.store.save(submission);
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        TransmitReceiptEntity receipt = TransmitReceiptEntity.create(submission,
            PayloadType.SUBMISSION,
            submission.getProductId(),
            RandomUtil.uuid(),
            TransmitStatus.AWAITING);
        submission = submission.copy().receipts(receipt).build();
        submission = this.store.save(submission);

        receipt = getReceiptByMessageId(submission.getReceipts(), receipt.getMessageId());
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        TransmitReceiptEntity attachmentReceipt = TransmitReceiptEntity.create(submission,
            PayloadType.ATTACHMENT,
            "attachment-rejected.pdf",
            RandomUtil.uuid(),
            TransmitStatus.PENDING);
        submission = submission.copy().receipt(attachmentReceipt).build();
        submission = this.store.save(submission);

        attachmentReceipt = getReceiptByMessageId(submission.getReceipts(), attachmentReceipt.getMessageId());
        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        attachmentReceipt = attachmentReceipt.copy().transmitStatus(TransmitStatus.RECEIVED).build();
        submission = submission.copy().receipt(attachmentReceipt).build();
        submission = this.store.save(submission);

        assertEquals(SubmissionStatus.PENDING, submission.getSubmissionStatus());

        // set submission as PENDING
        receipt = receipt.copy().transmitStatus(TransmitStatus.PENDING).build();
        submission = submission.copy().receipt(receipt).build();
        submission = this.store.save(submission);

        assertEquals(SubmissionStatus.SUBMITTING, submission.getSubmissionStatus());

        // set submission as RECEIVED
        receipt = receipt.copy().transmitStatus(TransmitStatus.RECEIVED).build();
        submission = submission.copy().receipt(receipt).build();
        submission = this.store.save(submission);

        assertEquals(SubmissionStatus.SUBMITTED, submission.getSubmissionStatus());
    }

    @Test
    public void testPendingOrSubmittingSubmissionsBeforeDate() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity pendingSubmission = createTobaccoSubmission("12345").product(product).build();
        pendingSubmission = this.store.save(pendingSubmission);

        assertEquals(SubmissionStatus.PENDING, pendingSubmission.getSubmissionStatus());

        SubmissionEntity submittingSubmission = createTobaccoSubmission("12345").product(product).build();
        submittingSubmission = this.store.save(submittingSubmission.copy()
                .receipt(TransmitReceiptEntity.create(submittingSubmission,
                    PayloadType.SUBMISSION,
                    "submission",
                    RandomUtil.uuid(),
                    TransmitStatus.PENDING))
                .build());

        assertEquals(SubmissionStatus.SUBMITTING, submittingSubmission.getSubmissionStatus());

        List<SubmissionEntity> submissions = Lists
                .newArrayList(this.store.getPendingOrSubmittingSubmissionsBefore(DateTime.now().plusHours(1)));
        assertNotNull(submissions);
        assertEquals(2, submissions.size());

        submissions = Lists
                .newArrayList(this.store.getPendingOrSubmittingSubmissionsBefore(DateTime.now().minusMinutes(1)));
        assertEquals(0, submissions.size());

    }

    @Test
    public void testGetSubmissionPayload() {
        final ProductEntity product = productStore.create(createTobaccoProduct("12345").build());

        SubmissionEntity submission = createTobaccoSubmission("12345").product(product).build();
        submission = this.store.save(submission);

        final IPayloadEntity payloadEntity = this.store.getSubmissionPayload(submission.getId());
        assertNotNull(payloadEntity);

    }

    private SubmissionEntity.Builder createTobaccoSubmission(final String submitterId) {
        return createTobaccoSubmission(submitterId, SendSubmissionType.IMMEDIAT);
    }

    private SubmissionEntity.Builder createTobaccoSubmission(final String submitterId,
        final SendSubmissionType sendType) {
        final TobaccoProductSubmission submission = ProductSubmissionHelper.okTobaccoProductFirstSubmission();
        submission.getSubmitter().withSubmitterID(submitterId);
        return SubmissionEntity.builder()
                .productType(ProductType.TOBACCO)
                .submissionType(SubmissionTypeEnum.NEW)
                .sendType(sendType)
                .submitterId(submitterId)
                .submission(submission)
                .sentBy("user");
    }

    private SubmissionEntity.Builder createEcigSubmission(final String submitterId) {
        final EcigProductSubmission submission = ProductSubmissionHelper.okEcigProductFirstSubmission();
        submission.getSubmitter().withSubmitterID(submitterId);
        return SubmissionEntity.builder()
                .productType(ProductType.ECIGARETTE)
                .sendType(SendSubmissionType.IMMEDIAT)
                .submissionType(SubmissionTypeEnum.NEW)
                .submitterId(submitterId)
                .submission(submission)
                .sentBy("user");
    }

    private ProductEntity.Builder createTobaccoProduct(final String submitterId) {
        return ProductEntity.builder()
                .productNumber(Eucegs.uuid())
                .submitterId(submitterId)
                .status(ProductStatus.VALID)
                .product(ProductSubmissionHelper.okFirstTobaccoProduct());
    }

    private ProductEntity.Builder createEcigProduct(final String submitterId) {
        return ProductEntity.builder()
                .productNumber(Eucegs.uuid())
                .submitterId(submitterId)
                .status(ProductStatus.VALID)
                .product(ProductSubmissionHelper.okFirstEcigProduct());
    }

    private TransmitReceiptEntity getReceiptByMessageId(final List<TransmitReceiptEntity> receipts,
        final String messageId) {
        for (final TransmitReceiptEntity receipt : receipts) {
            if (receipt.getMessageId().equals(messageId)) {
                return receipt;
            }
        }
        return null;
    }

}
