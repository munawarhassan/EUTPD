package com.pmi.tpd.euceg.backend.core.domibus.ws;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.BackendProperties.ConnectionType;
import com.pmi.tpd.euceg.backend.core.BackendProperties.JmsOption;
import com.pmi.tpd.euceg.backend.core.BackendProperties.WsOption;
import com.pmi.tpd.euceg.backend.core.TestEventPublisher;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsMessageReceiver;
import com.pmi.tpd.euceg.backend.core.domibus.ws.WsMessageSender;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;
import com.pmi.tpd.euceg.backend.core.support.ISenderMessageResolver;
import com.pmi.tpd.euceg.backend.core.support.SimpleReceiverMessageCreator;
import com.pmi.tpd.euceg.backend.core.support.SimpleSenderMessageCreator;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.testing.query.Conditions;
import com.pmi.tpd.testing.query.Poller;
import com.pmi.tpd.testing.query.TimedCondition;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WsMessageSenderIT.class })
@EnableJms
public class WsMessageSenderIT extends MockitoTestCase {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(WsMessageSenderIT.class);

    @Bean("SenderEventPublisher")
    public TestEventPublisher<EventBackendReceived<IBackendMessage>> senderEventPublisher() {
        return new TestEventPublisher<>();
    }

    @Bean
    public IApplicationProperties applicationProperties() {
        return mock(IApplicationProperties.class);
    }

    @Bean
    public I18nService i18nService() {
        return new SimpleI18nService();
    }

    @Bean
    public PendingMessageProvider pendingMessageProvider() {
        return new PendingMessageProvider();
    };

    @Bean
    public WsMessageSender<String, String> sender(@Named("SenderEventPublisher") final IEventPublisher eventPublisher,
        final IPendingMessageProvider pendingMessageProvider,
        final I18nService i18nService,
        final IApplicationProperties applicationProperties) {
        final WsMessageSender<String, String> sender = new WsMessageSender<>(new SimpleSenderMessageCreator(),
                pendingMessageProvider, i18nService, applicationProperties, eventPublisher);
        return sender;
    }

    @Bean("ReceiverEventPublisher")
    public TestEventPublisher<EventBackendReceived<?>> receiverEventPublisher() {
        return new TestEventPublisher<>();
    }

    @Bean("receiver")
    public JmsMessageReceiver<String> receiverListener(
        @Named("ReceiverEventPublisher") final IEventPublisher eventPublisher,
        final IApplicationProperties applicationProperties) {
        final JmsMessageReceiver<String> receiver = new JmsMessageReceiver<>(
                new SimpleReceiverMessageCreator(new SenderResponseResolver()), eventPublisher, applicationProperties);
        return receiver;
    }

    @Inject
    public WsMessageSender<String, String> sender;

    @Inject
    public JmsMessageReceiver<String> receiver;

    @Inject
    public PendingMessageProvider pendingMessageProvider;

    @Inject
    @Named("SenderEventPublisher")
    public TestEventPublisher<EventBackendReceived<?>> senderEventPublisher;

    @Inject
    @Named("ReceiverEventPublisher")
    public TestEventPublisher<EventBackendReceived<?>> receiverEventPublisher;

    private BackendProperties senderBackendProperties;

    private BackendProperties recieverBackendProperties;

    @BeforeEach
    public void setup() throws Exception {
        assumeTrue(!Strings.isNullOrEmpty(System.getenv("MODE_ID")));
        pendingMessageProvider.clear();
        senderBackendProperties = BackendProperties.builder()
                .enable(true)
                .connectionType(ConnectionType.Ws)
                .url("http://domibus-blue-192-168-1-175.traefik.me")
                .username("admin")
                .password("123456")
                .action("SubmitRequest")
                .service("http://ec.europa.eu/e-delivery/services/tobacco-ecig-reporting")
                .serviceType("e-delivery")
                .originalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1")
                .finalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4")
                .fromPartyId("domibus-blue")
                .toPartyId("domibus-red")
                .wsOptions(WsOption.builder().pendingInterval(2).build())
                .build();
        recieverBackendProperties = BackendProperties.builder()
                .enable(true)
                .connectionType(ConnectionType.Jms)
                .url("http://domibus-red-192-168-1-175.traefik.me")
                .username("admin")
                .password("123456")
                .action("SubmitResponse")
                .service("http://ec.europa.eu/e-delivery/services/tobacco-ecig-reporting")
                .serviceType("e-delivery")
                .originalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4")
                .finalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1")
                .fromPartyId("domibus-red")
                .toPartyId("domibus-blue")
                .jmsOptions(JmsOption.builder()
                        .url("tcp://neptune.local:6363")
                        .username("domibus")
                        .password("changeit")
                        .receiveTimeout(Duration.ofSeconds(2).toMillis())
                        .build())
                .build();
        sender.setBackendProperties(senderBackendProperties);
        receiver.setBackendProperties(recieverBackendProperties);
        senderEventPublisher.getPublishedEvents().clear();
        receiverEventPublisher.getPublishedEvents().clear();
        sender.start();
        receiver.start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        sender.shutdown();
        receiver.shutdown();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSendAndReceived() throws Exception {
        final String uuid = RandomUtil.uuid();

        sender.send(uuid, "good", null);

        final SubmitResponse response = (SubmitResponse) senderEventPublisher.getPublishedEvents()
                .stream()
                .filter(e -> e.isMessageInstanceOf(SubmitResponse.class))
                .findFirst()
                .map(EventBackendReceived::getMessage)
                .orElse(null);

        pendingMessageProvider.push(uuid);

        Poller.waitUntilTrue(Conditions.and(this.waitOnMessageSenderPublish(Response.class),
            this.waitOnMessageReceiverPublish(MessageSent.class)));

        assertNotNull(response, "Submit response can not be null");
        assertEquals(uuid, response.getMessageId(), "messageId should be the same");
        assertEquals(false, response.isErrorMessage(), "Submit response has not error message");

        assertEquals(1,
            senderEventPublisher.getPublishedEvents()
                    .stream()
                    .filter(e -> e.isMessageInstanceOf(Response.class))
                    .filter(e -> ((Response<String>) e.getMessage()).getResponses()
                            .stream()
                            .anyMatch("good response"::equals))
                    .count());
    }

    @Test
    public void shouldFailedForWrongReceiver() throws Exception {
        final String uuid = RandomUtil.uuid();
        final BackendProperties properties = sender.getBackendProperties();
        properties.setToPartyId("unknow-ap");
        sender.send(uuid, "good", null);

        final SubmitResponse response = (SubmitResponse) senderEventPublisher.getPublishedEvents()
                .stream()
                .filter(e -> e.isMessageInstanceOf(SubmitResponse.class))
                .findFirst()
                .map(EventBackendReceived::getMessage)
                .orElse(null);

        assertNotNull(response, "Submit response can not be null");
        assertNull(response.getMessageId(), "messageId should be null");
        assertEquals(true, response.isErrorMessage(), "Submit response has error message");

        assertThat(response.getErrorMessage(), containsString("EBMS:0003"));

    }

    @Test
    public void shouldFailedUnavailableReceiver() throws Exception {
        final BackendProperties properties = sender.getBackendProperties();
        properties.setToPartyId("domibus-green");
        properties.setAction("SubmitRequestImmediate");

        final String uuid = RandomUtil.uuid();
        sender.send(uuid, "good", null);

        final SubmitResponse response = (SubmitResponse) senderEventPublisher.getPublishedEvents()
                .stream()
                .filter(e -> e.isMessageInstanceOf(SubmitResponse.class))
                .findFirst()
                .map(EventBackendReceived::getMessage)
                .orElse(null);

        pendingMessageProvider.push(uuid);
        Poller.waitUntilTrue(Conditions.and(this.waitOnMessageSenderPublish(MessageSendFailure.class)));

        assertNotNull(response, "Submit response can not be null");

    }

    private <T extends IBackendMessage> TimedCondition waitOnMessageReceiverPublish(final Class<T> cl) {
        return Conditions.forSupplier(Duration.of(10, ChronoUnit.SECONDS),
            Duration.of(100, ChronoUnit.MILLIS),
            () -> receiverEventPublisher.getPublishedEvents().stream().anyMatch(e -> e.isMessageInstanceOf(cl)));
    }

    private <T extends IBackendMessage> TimedCondition waitOnMessageSenderPublish(final Class<T> cl) {
        return Conditions.forSupplier(Duration.of(10, ChronoUnit.SECONDS),
            Duration.of(100, ChronoUnit.MILLIS),
            () -> senderEventPublisher.getPublishedEvents().stream().anyMatch(e -> e.isMessageInstanceOf(cl)));
    }

    private static class SenderResponseResolver implements ISenderMessageResolver<String, String> {

        @Override
        public String apply(final String t) {
            String response;
            switch (t) {
                case "good":
                    response = "good response";
                    break;
                default:
                    response = "response";
                    break;
            }
            return response;
        }

    }

    public static class PendingMessageProvider implements IPendingMessageProvider {

        private final @Nonnull Set<String> pendingMessageIds = Sets.newHashSet();

        @Override
        public @Nonnull Set<String> getPendingMessageIds() {
            return pendingMessageIds;
        }

        @Override
        public boolean isOwner(@Nonnull final String messageId) {
            return this.pendingMessageIds.contains(messageId);
        }

        public void clear() {
            this.pendingMessageIds.clear();
        }

        public void push(final String id) {
            pendingMessageIds.add(id);
        }

    }

}
