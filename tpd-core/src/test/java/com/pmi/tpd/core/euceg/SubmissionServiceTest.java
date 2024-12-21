package com.pmi.tpd.core.euceg;

import org.eu.ceg.AttachmentRef;
import org.eu.ceg.Product.MarketResearchFiles;
import org.eu.ceg.SubmissionTypeEnum;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.transaction.PlatformTransactionManager;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.core.euceg.impl.ProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.euceg.spi.ISubmitterStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;

public class SubmissionServiceTest extends AbstractServiceTest {

    @Mock
    private IApplicationProperties applicationProperties;

    @Mock
    private IAttachmentService attachmentService;

    @Mock
    private IAttachmentStore attachmentStore;

    @Mock
    private ISubmitterStore submitterStore;

    @Mock(lenient = true)
    private IProductSubmissionStore productSubmissionStore;

    @Mock
    private IProductStore productStore;

    @Mock
    private IBackendManager backendManager;

    private final I18nService i18nService = new SimpleI18nService();

    @Mock
    private IEventPublisher publisher;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private IAuthenticationContext authContext;

    @Mock(lenient = true)
    private IEucegConstraintRuleManager constraintRuleManager;

    private DefaultSubmissionService submissionService;

    public SubmissionServiceTest() {
        super(DefaultSubmissionService.class, ISubmissionService.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        submissionService = new DefaultSubmissionService(applicationProperties, attachmentService, attachmentStore,
                submitterStore, productSubmissionStore, productStore, backendManager, i18nService, publisher,
                transactionManager, authContext, constraintRuleManager);
    }

    /**
     * Test verify can not possible to send a product with a attachment that associates to another submission that is
     * Sending.
     */
    @Test
    public void sendSubmissionWithAttachmentIsSending() {
        final String productNumber = "POM.00001";
        final String submitterId = "12345";
        final String sendingAttachment = RandomUtil.uuid();
        when(backendManager.isRunning()).thenReturn(true);
        when(productStore.get(productNumber)).thenReturn(ProductEntity.builder()
                .productNumber(productNumber)
                .status(ProductStatus.VALID)
                .productType(ProductType.TOBACCO)
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstTobaccoProduct()
                        .withMarketResearchFiles(new MarketResearchFiles()
                                .withAttachment(new AttachmentRef().withAttachmentID(sendingAttachment))))
                .build());
        doThrow(new AttachmentIsSendingException(
                i18nService.createKeyedMessage("app.service.euceg.submission.attachment.issending", sendingAttachment)))
                        .when(constraintRuleManager)
                        .checkAttachmentIsSending(any(), any());
        when(submitterStore.get(submitterId))
                .thenReturn(SubmitterEntity.builder().submitterId(submitterId).status(SubmitterStatus.SENT).build());
        final ArgumentCaptor<SubmissionEntity> submissionCreateCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        when(productSubmissionStore.create(submissionCreateCaptor.capture()))
                .thenAnswer(invocation -> ProductSubmissionStore.applyForAllAttachments(false)
                        .apply(submissionCreateCaptor.getValue()));

        try {
            submissionService.createOrSendSubmission(SubmissionSendRequest.builder()
                    .productNumber(productNumber)
                    .submissionType(SubmissionTypeEnum.NEW)
                    .sendType(SendSubmissionType.IMMEDIAT)
                    .build());
            fail();
        } catch (final AttachmentIsSendingException e) {
            assertEquals("app.service.euceg.submission.attachment.issending", e.getMessageKey());
        }
    }

    @Test
    public void sendSubmissionWithSubmitterNotValid() {
        final String productNumber = "POM.00001";
        final String submitterId = "12345";
        when(backendManager.isRunning()).thenReturn(true);
        when(productStore.get(productNumber)).thenReturn(ProductEntity.builder()
                .productNumber(productNumber)
                .status(ProductStatus.VALID)
                .productType(ProductType.TOBACCO)
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstTobaccoProduct())
                .build());

        when(submitterStore.get(submitterId)).thenReturn(
            SubmitterEntity.builder().submitterId(submitterId).status(SubmitterStatus.IMPORTED).build());
        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        when(productSubmissionStore.create(submissionCaptor.capture()))
                .thenAnswer(invocation -> submissionCaptor.getValue());

        try {
            submissionService.createOrSendSubmission(SubmissionSendRequest.builder()
                    .productNumber(productNumber)
                    .submissionType(SubmissionTypeEnum.NEW)
                    .sendType(SendSubmissionType.IMMEDIAT)
                    .build());
            fail();
        } catch (final EucegException e) {
            assertEquals("app.service.euceg.submission.submitter.notvalid", e.getMessageKey());
        }
    }

    @Test
    public void shouldCreateEcigaretteSubmission() {
        final String productNumber = "POM.00001";
        final String submitterId = "12345";

        when(productStore.get(productNumber)).thenReturn(ProductEntity.builder()
                .productNumber(productNumber)
                .status(ProductStatus.VALID)
                .productType(ProductType.ECIGARETTE)
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstEcigProduct())
                .build());

        when(submitterStore.get(submitterId)).thenReturn(SubmitterEntity.builder().submitterId(submitterId).build());
        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        when(productSubmissionStore.create(submissionCaptor.capture()))
                .thenAnswer(invocation -> submissionCaptor.getValue());

        final SubmissionSendRequest request = SubmissionSendRequest.builder()
                .productNumber(productNumber)
                .sendType(SendSubmissionType.MANUAL)
                .submissionType(SubmissionTypeEnum.NEW)
                .build();

        final SubmissionEntity submissionEntity = submissionService.createSubmission(request);
        assertThat(submissionEntity, Matchers.notNullValue());
    }

    /**
     * Should not possible create submission when product is already submitted for {@link SubmissionTypeEnum#NEW}.
     *
     * @see TPD-143
     * @since 1.7
     */
    @Test
    public void shouldNotPossibleCreateSubmissionWhenProductAlreadySubmittedForNew() {
        final String productNumber = "POM.00001";
        final String submitterId = "12345";

        when(productStore.get(productNumber)).thenReturn(ProductEntity.builder()
                .productNumber(productNumber)
                .status(ProductStatus.VALID)
                .productType(ProductType.ECIGARETTE)
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstEcigProduct())
                .submission(ProductSubmissionHelper.entity(ProductSubmissionHelper.okEcigProductFirstSubmission()))
                .build());

        when(submitterStore.get(submitterId)).thenReturn(SubmitterEntity.builder().submitterId(submitterId).build());
        final ArgumentCaptor<SubmissionEntity> submissionCaptor = ArgumentCaptor.forClass(SubmissionEntity.class);
        when(productSubmissionStore.create(submissionCaptor.capture()))
                .thenAnswer(invocation -> submissionCaptor.getValue());

        final SubmissionSendRequest request = SubmissionSendRequest.builder()
                .productNumber(productNumber)
                .sendType(SendSubmissionType.MANUAL)
                .submissionType(SubmissionTypeEnum.NEW)
                .build();

        try {
            submissionService.createSubmission(request);
        } catch (final EucegException ex) {
            assertThat(ex.getMessageKey(), Matchers.is("app.service.euceg.submission.send.newsubmissionnotaccepted"));
        }
    }

    /**
     * Should not possible create submission when have previous TPD-ID for {@link SubmissionTypeEnum#MODIFICATION_NEW}.
     *
     * @see TPD-143
     * @since 1.7
     */
    @Test
    public void shouldNotPossibleCreateSubmissionWhenHaveNotPreviousTPIDForModificationNew() {
        final String productNumber = "POM.00001";
        final String submitterId = "12345";
        final ProductEntity entity = ProductEntity.builder()
                .productNumber(productNumber)
                .status(ProductStatus.VALID)
                .productType(ProductType.ECIGARETTE)
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstEcigProduct())
                .build();

        when(productStore.get(productNumber)).thenReturn(entity);
        when(submitterStore.get(submitterId)).thenReturn(SubmitterEntity.builder().submitterId(submitterId).build());

        final SubmissionSendRequest request = SubmissionSendRequest.builder()
                .productNumber(productNumber)
                .sendType(SendSubmissionType.MANUAL)
                .submissionType(SubmissionTypeEnum.MODIFICATION_NEW)
                .build();
        try {
            submissionService.createSubmission(request);
        } catch (final EucegException ex) {
            assertThat(ex.getMessageKey(), Matchers.is("app.service.euceg.submission.send.previous-tpid.required"));
        }
    }

    /**
     * Should not possible create submission from product No Sendable
     *
     * @see TPD-143
     * @since 1.7
     */
    @Test
    public void shouldNotPossibleCreateSubmissionFromProductNotSendable() {
        final String productNumber = "POM.00001";

        when(productStore.get(productNumber)).thenReturn(ProductEntity.builder()
                .productNumber(productNumber)
                .status(ProductStatus.IMPORTED)
                .productType(ProductType.TOBACCO)
                .product(ProductSubmissionHelper.okFirstTobaccoProduct())
                .build());

        final SubmissionSendRequest request = SubmissionSendRequest.builder()
                .productNumber(productNumber)
                .sendType(SendSubmissionType.MANUAL)
                .submissionType(SubmissionTypeEnum.NEW)
                .build();
        try {
            submissionService.createSubmission(request);
        } catch (final EucegException ex) {
            assertThat(ex.getMessageKey(), Matchers.is("app.service.euceg.submission.notsendable"));
        }
    }

}
