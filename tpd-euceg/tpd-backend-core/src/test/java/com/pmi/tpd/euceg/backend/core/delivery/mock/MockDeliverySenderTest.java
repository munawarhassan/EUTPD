package com.pmi.tpd.euceg.backend.core.delivery.mock;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.eu.ceg.AppResponse;
import org.eu.ceg.Attachment;
import org.eu.ceg.AttachmentResponse;
import org.eu.ceg.EcigPresentation;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.EcigProductSubmissionResponse;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.SubmissionType;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.SubmitterDetails;
import org.eu.ceg.SubmitterDetailsResponse;
import org.eu.ceg.TobaccoPresentation;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.TestEventPublisher;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.testing.query.Conditions;
import com.pmi.tpd.testing.query.Poller;
import com.pmi.tpd.testing.query.TimedCondition;

public class MockDeliverySenderTest extends MockitoTestCase {

    @Spy
    private final TestEventPublisher<EventBackendReceived<?>> eventPublisher = new TestEventPublisher<>();

    @InjectMocks
    private MockDeliverySender sender;

    @BeforeEach
    public void beforeEach() {
        eventPublisher.clear();
        sender.start();
    }

    @Override
    @AfterEach
    public void afterEach() {
        sender.stop();
    }

    @Test
    public void shouldRunning() {
        this.sender.isRunning();
    }

    @Test
    public void testSendAttachment() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid, new Attachment(), null);
        Poller.waitUntilTrue(this.waitOnPublish(Response.class));

        final Response<AppResponse> response = getResponse();

        assertThat(response.getResponses().stream().findFirst().orElseThrow(), Matchers.isA(AttachmentResponse.class));
    }

    @Test
    public void testSendSubmitterDetails() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid, new SubmitterDetails(), null);
        Poller.waitUntilTrue(this.waitOnPublish(Response.class));

        final Response<AppResponse> response = getResponse();

        assertThat(response.getResponses().stream().findFirst().orElseThrow(),
            Matchers.isA(SubmitterDetailsResponse.class));
    }

    @Test
    public void testSendTobaccoProduct() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid,
            new TobaccoProductSubmission().withSubmissionType(new SubmissionType().withValue(SubmissionTypeEnum.NEW))
                    .withProduct(new TobaccoProduct().withProductID(Eucegs.productNumber("99962-21-00003"))
                            .withPresentations(new TobaccoProduct.Presentations().withPresentation(
                                new TobaccoPresentation().withProductNumber(Eucegs.string40("POM.00001"))))),
            null);
        Poller.waitUntilTrue(this.waitOnPublish(Response.class));

        final Response<AppResponse> response = getResponse();

        assertThat(response.getResponses().stream().findFirst().orElseThrow(),
            Matchers.isA(TobaccoProductSubmissionResponse.class));
    }

    @Test
    public void testSendRejectedTobaccoProduct() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid,
            new TobaccoProductSubmission().withSubmissionType(new SubmissionType().withValue(SubmissionTypeEnum.NEW))
                    .withProduct(new TobaccoProduct().withProductID(Eucegs.productNumber("99962-21-00003"))
                            .withPresentations(new TobaccoProduct.Presentations().withPresentation(
                                new TobaccoPresentation().withProductNumber(Eucegs.string40("POM.rejected"))))),
            null);
        Poller.waitUntilTrue(this.waitOnPublish(MessageSendFailure.class));

        final MessageSendFailure response = getMessageSendFailure();

        assertEquals(TransmitStatus.REJECTED, response.getStatus());
        assertEquals(uuid, response.getMessageId());
    }

    @Test
    public void testSendFailedTobaccoProduct() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid, new TobaccoProductSubmission(), null);
        Poller.waitUntilTrue(this.waitOnPublish(Response.class));

        final Response<AppResponse> response = getResponse();

        assertThat(response.getResponses().stream().findFirst().orElseThrow(), Matchers.isA(ErrorResponse.class));
    }

    @Test
    public void testSendEcigProduct() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid,
            new EcigProductSubmission()
                    .withProduct(new EcigProduct().withProductID(Eucegs.productNumber("99962-21-00003"))
                            .withPresentations(new EcigProduct.Presentations().withPresentation(
                                new EcigPresentation().withProductNumber(Eucegs.string40("POM.00001"))))),
            null);
        Poller.waitUntilTrue(this.waitOnPublish(Response.class));

        final Response<AppResponse> response = getResponse();

        assertThat(response.getResponses().stream().findFirst().orElseThrow(),
            Matchers.isA(EcigProductSubmissionResponse.class));
    }

    @Test
    public void testSendRejectedEcigProduct() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid,
            new EcigProductSubmission()
                    .withProduct(new EcigProduct().withProductID(Eucegs.productNumber("99962-21-00003"))
                            .withPresentations(new EcigProduct.Presentations().withPresentation(
                                new EcigPresentation().withProductNumber(Eucegs.string40("POM.rejected"))))),
            null);
        Poller.waitUntilTrue(this.waitOnPublish(MessageSendFailure.class));

        final MessageSendFailure response = getMessageSendFailure();

        assertEquals(TransmitStatus.REJECTED, response.getStatus());
        assertEquals(uuid, response.getMessageId());
    }

    @Test
    public void testSendFailedEcigProduct() throws Exception {
        final String uuid = RandomUtil.uuid();
        sender.send(uuid, new EcigProductSubmission(), null);
        Poller.waitUntilTrue(this.waitOnPublish(Response.class));

        final Response<AppResponse> response = getResponse();

        assertThat(response.getResponses().stream().findFirst().orElseThrow(), Matchers.isA(ErrorResponse.class));
    }

    @SuppressWarnings("unchecked")
    private Response<AppResponse> getResponse() {
        return eventPublisher.getPublishedEvents()
                .stream()
                .filter(e -> e.isMessageInstanceOf(Response.class))
                .map(m -> (Response<AppResponse>) m.getMessage())
                .findFirst()
                .orElseThrow();
    }

    private MessageSendFailure getMessageSendFailure() {
        return eventPublisher.getPublishedEvents()
                .stream()
                .filter(e -> e.isMessageInstanceOf(MessageSendFailure.class))
                .map(m -> (MessageSendFailure) m.getMessage())
                .findFirst()
                .orElseThrow();
    }

    private <T extends IBackendMessage> TimedCondition waitOnPublish(final Class<T> cl) {
        return Conditions.forSupplier(Duration.of(10, ChronoUnit.SECONDS),
            Duration.of(100, ChronoUnit.MILLIS),
            () -> eventPublisher.getPublishedEvents().stream().anyMatch(e -> e.isMessageInstanceOf(cl)));
    }
}
