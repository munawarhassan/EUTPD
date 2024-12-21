package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertFrom;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToMessageReceiveFailure;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToMessageSendFailure;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToMessageSent;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToResponseMessage;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToSubmitResponse;
import static com.pmi.tpd.euceg.backend.core.message.MessageHelper.createSubmitMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ErrorHandler;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.ISender;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Payload;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;

@Transactional
public class JmsMessageSender<REQUEST, RESPONSE> implements ISender<REQUEST>, ErrorHandler, ExceptionListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsMessageSender.class);

    /** */
    private JmsTemplate template;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final IApplicationProperties applicationProperties;

    /** */
    private final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator;

    /** */
    @Nonnull
    private final IPendingMessageProvider pendingMessageProvider;

    /** */
    private List<DefaultMessageListenerContainer> containers;

    /** */
    private SingleConnectionFactory connectionFactory;

    /** */
    private PlatformTransactionManager transactionManager;

    /** */
    private BackendProperties backendProperties;

    /** */
    private boolean started = false;

    /** */
    private boolean autoStartup = false;

    /** */
    private boolean startupFailed = false;

    /** */
    private String startupFailedMessage = null;

    public JmsMessageSender(final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, final IEventPublisher eventPublisher,
            @Nonnull final IApplicationProperties applicationProperties) {
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.messageCreator = checkNotNull(messageCreator, "messageCreator");
        this.pendingMessageProvider = checkNotNull(pendingMessageProvider, "pendingMessageProvider");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    }

    @Override
    public void setAutoStartup(final boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    @Override
    public void stop() {
        shutdown();
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    @Override
    public void start() {
        this.startupFailed = false;
        this.startupFailedMessage = null;
        try {
            initialize();
            containers.stream().forEach(DefaultMessageListenerContainer::start);
        } catch (final Exception ex) {
            startupFailedMessage = ex.getLocalizedMessage();
            startupFailed = true;
            throw ex;
        }
        this.started = true;
    }

    @Override
    public void shutdown() {
        if (!started) {
            return;
        }
        containers.stream().forEach(DefaultMessageListenerContainer::destroy);
        containers.clear();
        this.connectionFactory.destroy();
        this.connectionFactory = null;
        this.template = null;
        this.started = false;
    }

    public void setBackendProperties(final BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    public BackendProperties getBackendProperties() {
        if (this.backendProperties != null) {
            return this.backendProperties;
        }
        return applicationProperties.getConfiguration(BackendProperties.class);
    }

    @Override
    public void healthCheck() throws Exception {
        if (startupFailed) {
            throw new BackendException(startupFailedMessage);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void send(@Nonnull final String messageId, @Nonnull final REQUEST payload)
            throws IOException, BackendException {
        this.send(messageId, payload, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void send(@Nonnull final String messageId,
        @Nonnull final REQUEST payload,
        final @Nullable Path workingDirectory) throws IOException, BackendException {
        checkHasText(messageId, "messageId");
        checkNotNull(payload, "payload");

        // submit request
        this.send(createSubmitMessage(getBackendProperties(),
            messageId,
            this.messageCreator.createRequestPayload(payload, workingDirectory)));
    }

    protected void send(@Nonnull final SubmitMessage submitMessage) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Submit message:{}", submitMessage);
        }
        this.template.send(JmsConstants.JMS_QUEUE_IN_NAME, session -> convertFrom(submitMessage, session));
    }

    protected void initialize() {
        final BackendProperties properties = getBackendProperties();
        // TODO need find solution to check if url is correct before considers it is
        // running
        // the creation of connection factory with wrong url doesn't fail, only listener
        // container but it is too late.
        final ActiveMQConnectionFactory conn = new ActiveMQConnectionFactory(properties.getJmsOptions().getUrl());
        if (!Strings.isNullOrEmpty(properties.getJmsOptions().getUsername())) {
            conn.setUserName(properties.getJmsOptions().getUsername());
            conn.setPassword(properties.getJmsOptions().getPassword());
        }
        this.connectionFactory = new CachingConnectionFactory(conn);

        this.transactionManager = new JmsTransactionManager(connectionFactory);

        this.template = new JmsTemplate(connectionFactory);
        this.template.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        template.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        template.setSessionTransacted(true);
        template.setReceiveTimeout(Duration.ofSeconds(properties.getJmsOptions().getReceiveTimeout()).toMillis());

        containers = Lists.newArrayListWithCapacity(4);
        containers.add(createMessageListenerContainer(JmsConstants.JMS_QUEUE_REPLY_NAME,
            new ReceiveMessageResponseSentListener(),
            null));
        containers.add(
            createMessageListenerContainer(JmsConstants.JMS_QUEUE_OUT_NAME, new ReceiveResponseListener(), null));
        containers.add(createMessageListenerContainer(JmsConstants.JMS_QUEUE_ERROR_NOTIFY_CONSUMER_NAME,
            new ReceiveMessageReceiveFailureListener(),
            null));
        containers.add(createMessageListenerContainer(JmsConstants.JMS_QUEUE_ERROR_NOTIFY_PRODUCER_NAME,
            new ReceiveMessageSendFailureListener(),
            null));

        containers.forEach(DefaultMessageListenerContainer::afterPropertiesSet);

    }

    @Override
    public void handleError(final Throwable t) {
        LOGGER.warn("handle error", t);
    }

    @Override
    public void onException(final JMSException exception) {
        if (exception != null && exception.getCause() instanceof InterruptedException) {
            return;
        }
        LOGGER.warn("handle exception", exception);
    }

    protected DefaultMessageListenerContainer createMessageListenerContainer(final @Nonnull String queue,
        @Nonnull final Object messageListener,
        @Nullable final String messageSelector) {
        Assert.checkHasText(queue, "queue");
        final BackendProperties properties = getBackendProperties();
        final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setErrorHandler(this);
        container.setExceptionListener(this);
        container.setDestination(new ActiveMQQueue(queue));
        if (!Strings.isNullOrEmpty(messageSelector)) {
            container.setMessageSelector(messageSelector);
        }
        container.setMessageListener(messageListener);
        container.setAutoStartup(false);
        container.setConcurrency(properties.getJmsOptions().getConcurrency());
        container.setTransactionManager(transactionManager);
        return container;
    }

    /**
     * listener to consume the received acknowledge messages from responder
     */
    private class ReceiveResponseListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            if (!(message instanceof MapMessage)) {
                throw new IllegalArgumentException();
            }
            try {
                final ResponseMessage response = convertToResponseMessage((MapMessage) message);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ResponseMessage receive:{}", response);
                }
                final var conversionId = response.getConversationId();
                if (!pendingMessageProvider.isOwner(conversionId)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("pending message id : {} is not recognized", conversionId);
                    }
                    throw new RuntimeException("Unrecogniized message");
                }
                eventPublisher.publish(new EventBackendReceived<>(this,
                        Response.builder()
                                .conversationId(response.getConversationId())
                                .responses(response.getPayloads()
                                        .stream()
                                        .map(Payload::getDataSource)
                                        .map(messageCreator::createPayloadResponse)
                                        .collect(Collectors.toList()))
                                .build()));

            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }

    /**
     * This listener is used to inform about the message status after sending a message to Domibus
     */
    private class ReceiveMessageResponseSentListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            try {
                final String messageType = message.getStringProperty("messageType");
                switch (messageType) {
                    case JmsConstants.MESSAGE_TYPE_RESPONSE_SUBMIT:
                        final SubmitResponse submitResponse = convertToSubmitResponse(message);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Response Submit message:{}", submitResponse);
                        }
                        eventPublisher.publish(new EventBackendReceived<>(this, submitResponse));
                        break;
                    case JmsConstants.MESSAGE_TYPE_MESSAGE_SENT:
                        final MessageSent response = convertToMessageSent(message);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("MessageSent receive:{}", response);
                        }
                        eventPublisher.publish(new EventBackendReceived<>(this, response));
                        break;
                    default:
                        break;
                }
            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }

    private class ReceiveMessageSendFailureListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            try {
                final MessageSendFailure response = convertToMessageSendFailure(message);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("MessageSendFailure receive:{}", response);
                }
                eventPublisher.publish(new EventBackendReceived<>(this, response));
            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }

    private class ReceiveMessageReceiveFailureListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            try {
                final MessageReceiveFailure response = convertToMessageReceiveFailure(message);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("MessageReceiveFailure receive:{}", response);
                }
                eventPublisher.publish(new EventBackendReceived<>(this, response));
            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }

}
