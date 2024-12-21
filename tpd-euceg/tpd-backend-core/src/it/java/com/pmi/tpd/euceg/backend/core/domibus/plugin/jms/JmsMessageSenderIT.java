package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.common.collect.Sets;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.BackendProperties.JmsOption;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;
import com.pmi.tpd.euceg.backend.core.TestEventPublisher;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.MessageHelper;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;
import com.pmi.tpd.euceg.backend.core.support.SimpleSenderMessageCreator;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.testing.query.Conditions;
import com.pmi.tpd.testing.query.Poller;
import com.pmi.tpd.testing.query.TimedCondition;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JmsMessageSenderIT.class })
@EnableJms
@Testcontainers(disabledWithoutDocker = true)
public class JmsMessageSenderIT extends MockitoTestCase {

    @Container
    private final ActiveMQContainer activeMQContainer = new ActiveMQContainer("apache/activemq-classic:5.17.6")
            .withExposedPorts(61616, 8161);

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsMessageSenderIT.class);

    @Bean("SenderEventPublisher")
    public TestEventPublisher<EventBackendReceived<IBackendMessage>> senderEventPublisher() {
        return new TestEventPublisher<>();
    }

    @Bean
    public IApplicationProperties applicationProperties() {
        return mock(IApplicationProperties.class);
    }

    @Bean
    public PendingMessageProvider pendingMessageProvider() {
        return new PendingMessageProvider();
    };

    @Bean
    public ISenderMessageCreator<String, String> getSenderMessageCreator() {
        return new SimpleSenderMessageCreator();
    }

    @Bean
    public JmsMessageSender<String, String> sender(@Named("SenderEventPublisher") final IEventPublisher eventPublisher,
        final IApplicationProperties applicationProperties,
        final PendingMessageProvider pendingMessageProvider,
        final ISenderMessageCreator<String, String> senderMessageCreator) {
        final JmsMessageSender<String, String> sender = new JmsMessageSender<>(senderMessageCreator,
                pendingMessageProvider, eventPublisher, applicationProperties);
        return sender;
    }

    @Inject
    public JmsMessageSender<String, String> sender;

    @Inject
    @Named("SenderEventPublisher")
    public TestEventPublisher<EventBackendReceived<IBackendMessage>> senderEventPublisher;

    @Inject
    public PendingMessageProvider pendingMessageProvider;

    @Inject
    public ISenderMessageCreator<String, String> senderMessageCreator;

    private BackendProperties senderBackendProperties;

    private JmsTemplate template;

    @BeforeEach
    public void setup() throws JMSException {
        final String brokerUrl = "tcp://localhost:" + activeMQContainer.getMappedPort(61616);

        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);

        template = new JmsTemplate(connectionFactory);
        template.setReceiveTimeout(3000);

        pendingMessageProvider.clear();
        senderBackendProperties = BackendProperties.builder()
                .enable(true)
                .url("http://localhost:8080")
                .username("admin")
                .password("123456")
                .action("SubmitRequest")
                .service("http://ec.europa.eu/e-delivery/services/tobacco-ecig-reporting")
                .serviceType("e-delivery")
                .originalSender("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1")
                .finalRecipient("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4")
                .fromPartyId("ACC-EUCEG-99962-AS4")
                .toPartyId("EUCEG_EC")
                .jmsOptions(JmsOption.builder()
                        .url(brokerUrl)
                        .username("domibus")
                        .password("changeit")
                        .receiveTimeout(Duration.ofSeconds(2).toMillis())
                        .build())
                .build();

        sender.setBackendProperties(senderBackendProperties);
        senderEventPublisher.getPublishedEvents().clear();
        sender.start();

    }

    @AfterEach
    public void tearDown() throws InterruptedException, JMSException {
        sender.shutdown();
    }

    @Test
    public void shouldSendSubmitMessage() throws Exception {
        final String uuid = RandomUtil.uuid();
        this.pendingMessageProvider.push(uuid);
        sender.send(uuid, "good");

        final Message msg = template.receive(JmsConstants.JMS_QUEUE_IN_NAME);

        final IncomingMessage incomingMessage = JmsHelper.convertToincomingMessage((MapMessage) msg);

        assertEquals(uuid, incomingMessage.getConversationId());
        assertEquals(uuid, incomingMessage.getMessageId());

    }

    @Test
    public void shouldReceivedSubmitResponse() throws Exception {
        final String uuid = RandomUtil.uuid();

        template.send(JmsConstants.JMS_QUEUE_REPLY_NAME, session -> {
            final Message msg = session.createMessage();
            msg.setStringProperty("messageType", JmsConstants.MESSAGE_TYPE_RESPONSE_SUBMIT);
            msg.setStringProperty("messageId", uuid);
            return msg;
        });
        Poller.waitUntilTrue(Conditions.and(this.waitOnMessagePublish(SubmitResponse.class)));

        final SubmitResponse response = findFirstPublishedEvents(SubmitResponse.class).orElseThrow();

        assertEquals(uuid, response.getMessageId());

    }

    @Test
    public void shouldReceivedMessageSent() throws Exception {
        final String uuid = RandomUtil.uuid();

        template.send(JmsConstants.JMS_QUEUE_REPLY_NAME, session -> {
            final Message msg = session.createMessage();
            msg.setStringProperty("messageType", JmsConstants.MESSAGE_TYPE_MESSAGE_SENT);
            msg.setStringProperty("messageId", uuid);
            return msg;
        });
        Poller.waitUntilTrue(Conditions.and(this.waitOnMessagePublish(MessageSent.class)));

        final MessageSent response = findFirstPublishedEvents(MessageSent.class).orElseThrow();

        assertEquals(uuid, response.getMessageId());

    }

    @Test
    public void shouldReceivedResponse() throws BackendException, IOException, JMSException {
        final String uuid = RandomUtil.uuid();

        this.pendingMessageProvider.push(uuid);

        final ResponseMessage responseMessage = MessageHelper.createResponseMessage(senderBackendProperties,
            uuid,
            uuid,
            Arrays.asList(senderMessageCreator.createRequestPayload("good")));
        template.send(JmsConstants.JMS_QUEUE_OUT_NAME, session -> JmsHelper.convertFrom(responseMessage, session));

        Poller.waitUntilTrue(Conditions.and(this.waitOnMessagePublish(Response.class)));

        final Response<?> response = findFirstPublishedEvents(Response.class).orElseThrow();

        assertEquals(uuid, response.getConversationId());
    }

    @Test
    public void shouldReceiveMessageReceiveFailure() throws Exception {
        final String uuid = RandomUtil.uuid();

        template.send(JmsConstants.JMS_QUEUE_ERROR_NOTIFY_CONSUMER_NAME, session -> {
            final Message msg = session.createMessage();
            msg.setStringProperty("messageId", uuid);
            msg.setStringProperty("errorCode", "EBMS:0003");
            msg.setStringProperty("errorDetail", "unknown partId");
            msg.setStringProperty("endPoint", "jms plugin");
            return msg;
        });
        Poller.waitUntilTrue(Conditions.and(this.waitOnMessagePublish(MessageReceiveFailure.class)));

        final MessageReceiveFailure response = findFirstPublishedEvents(MessageReceiveFailure.class).orElseThrow();

        assertNotNull(response, "MessageReceiveFailure can not be null");
        assertEquals(uuid, response.getMessageId(), "messageId should be the same");
        assertEquals(TransmitStatus.REJECTED, response.getStatus());

    }

    @Test
    public void shouldReceiveMMessageSendFailure() throws Exception {
        final String uuid = RandomUtil.uuid();

        template.send(JmsConstants.JMS_QUEUE_ERROR_NOTIFY_PRODUCER_NAME, session -> {
            final Message msg = session.createMessage();
            msg.setStringProperty("messageId", uuid);
            msg.setStringProperty("errorCode", "EBMS:0003");
            msg.setStringProperty("errorDetail", "unknown partId");
            return msg;
        });
        Poller.waitUntilTrue(Conditions.and(this.waitOnMessagePublish(MessageSendFailure.class)));

        final MessageSendFailure response = findFirstPublishedEvents(MessageSendFailure.class).orElseThrow();

        assertNotNull(response, "MessageSendFailure can not be null");
        assertEquals(uuid, response.getMessageId(), "messageId should be the same");
        assertEquals(TransmitStatus.REJECTED, response.getStatus());

    }

    private <T extends IBackendMessage> TimedCondition waitOnMessagePublish(final Class<T> cl) {
        return Conditions.forSupplier(Duration.of(10, ChronoUnit.SECONDS),
            Duration.of(100, ChronoUnit.MILLIS),
            () -> senderEventPublisher.getPublishedEvents().stream().anyMatch(e -> e.isMessageInstanceOf(cl)));
    }

    private <R extends IBackendMessage> Optional<R> findFirstPublishedEvents(final Class<R> type) {
        return (Optional<R>) senderEventPublisher.getPublishedEvents()
                .stream()
                .filter(e -> e.isMessageInstanceOf(type))
                .findFirst()
                .map(EventBackendReceived::getMessage);
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
