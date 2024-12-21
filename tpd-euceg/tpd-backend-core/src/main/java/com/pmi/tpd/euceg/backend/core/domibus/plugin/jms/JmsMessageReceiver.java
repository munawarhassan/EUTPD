package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.notNull;
import static com.pmi.tpd.api.util.FileUtils.deleteDirectory;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToMessageReceiveFailure;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToMessageSendFailure;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToMessageSent;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToSubmitResponse;
import static com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsHelper.convertToincomingMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import javax.jms.MessageProducer;
import javax.jms.Queue;
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
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ErrorHandler;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.IReceiver;
import com.pmi.tpd.euceg.backend.core.IReceiverMessageCreator;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.MessageHelper;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Payload;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;

public class JmsMessageReceiver<R>
        implements IReceiver, SessionAwareMessageListener<Message>, ErrorHandler, ExceptionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsMessageReceiver.class);

    private final IApplicationProperties applicationProperties;

    private final IEventPublisher eventPublisher;

    private JmsTemplate template;

    private String concurencyListener = "1-1";

    private final IReceiverMessageCreator<R> messageCreator;

    private List<DefaultMessageListenerContainer> containers;

    private SingleConnectionFactory connectionFactory;

    private PlatformTransactionManager transactionManager;

    private BackendProperties backendProperties;

    private boolean started = false;

    private boolean autoStartup = false;

    private final Path workingDirectory;

    public JmsMessageReceiver(@Nonnull final IReceiverMessageCreator<R> messageCreator) {
        this(messageCreator, null, null);
    }

    public JmsMessageReceiver(@Nonnull final IReceiverMessageCreator<R> messageCreator,
            @Nullable final IEventPublisher eventPublisher,
            @Nullable final IApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.messageCreator = checkNotNull(messageCreator, "messageCreator");
        try {
            workingDirectory = Files.createTempDirectory("receiver-" + System.nanoTime());
            workingDirectory.toFile().deleteOnExit();
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
        this.started = true;
        init();
        containers.stream().forEach(DefaultMessageListenerContainer::start);
    }

    @Override
    public void shutdown() {
        if (!started) {
            return;
        }
        containers.stream().forEach(DefaultMessageListenerContainer::destroy);
        containers.clear();
        if (connectionFactory != null) {
            this.connectionFactory.destroy();
        }
        this.connectionFactory = null;
        this.template = null;
        this.started = false;
        if (workingDirectory != null) {
            deleteDirectory(workingDirectory);
        }
    }

    public void setConcurencyListener(final String concurencyListener) {
        this.concurencyListener = concurencyListener;
    }

    public String getConcurencyListener() {
        return concurencyListener;
    }

    public void setBackendProperties(final BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    public BackendProperties getBackendProperties() {
        if (this.backendProperties != null) {
            return this.backendProperties;
        }
        notNull(applicationProperties);
        return applicationProperties.getConfiguration(BackendProperties.class);
    }

    @Override
    public void onMessage(final Message message, final Session session) {
        IncomingMessage incomingMessage = null;
        Path localWorkingDirectory = null;
        if (message instanceof MapMessage) {
            try {
                incomingMessage = convertToincomingMessage((MapMessage) message);
                localWorkingDirectory = workingDirectory.resolve(incomingMessage.getMessageId());
                localWorkingDirectory.toFile().mkdir();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("IncomingMessage receive:{}", incomingMessage);
                }

                final ResponseMessage responseMessage = createResponseMessage(incomingMessage, workingDirectory);

                sendResponse(responseMessage, session);
            } catch (final Exception ex) {
                throw new RuntimeException(ex.getLocalizedMessage(), ex);
            } finally {
                if (localWorkingDirectory != null) {
                    deleteDirectory(localWorkingDirectory);
                }
            }
        } else {
            throw new IllegalArgumentException("Message Error");
        }
    }

    @Nonnull
    public ResponseMessage createResponseMessage(@Nonnull final IncomingMessage incomingMessage,
        @Nullable final Path workingDirectory) throws IOException, BackendException {

        final ResponseMessage responseMessage = MessageHelper.createResponseMessage(getBackendProperties(),
            incomingMessage.getConversationId(),
            RandomUtil.uuid(),
            incomingMessage.getPayloads()
                    .stream()
                    .map(Payload::getDataSource)
                    .map(messageCreator::createIncommingPayload)
                    .map(p -> messageCreator
                            .createResponsePayload(p, incomingMessage.getConversationId(), workingDirectory))
                    .collect(Collectors.toList()));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Receiver  responseMessage:{}", responseMessage);
        }
        return responseMessage;

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

    protected void init() {
        final BackendProperties properties = getBackendProperties();
        final ActiveMQConnectionFactory conn = new ActiveMQConnectionFactory(properties.getJmsOptions().getUrl());
        conn.setUserName(properties.getJmsOptions().getUsername());
        conn.setPassword(properties.getJmsOptions().getPassword());
        this.connectionFactory = new CachingConnectionFactory(conn);

        this.transactionManager = new JmsTransactionManager(connectionFactory);

        this.template = new JmsTemplate(connectionFactory);
        // this.template.setSessionTransacted(true);
        this.template.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        template.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);

        containers = Lists.newArrayListWithCapacity(4);
        containers.add(
            createMessageListenerContainer(JmsConstants.JMS_QUEUE_REPLY_NAME, new ReceiveMessageSentListener(), null));
        containers.add(createMessageListenerContainer(JmsConstants.JMS_QUEUE_OUT_NAME, this, null));
        containers.add(createMessageListenerContainer(JmsConstants.JMS_QUEUE_ERROR_NOTIFY_CONSUMER_NAME,
            new ReceiveMessageReceiveFailureListener(),
            null));
        containers.add(createMessageListenerContainer(JmsConstants.JMS_QUEUE_ERROR_NOTIFY_PRODUCER_NAME,
            new ReceiveMessageSendFailureListener(),
            null));

        containers.forEach(DefaultMessageListenerContainer::afterPropertiesSet);
    }

    protected DefaultMessageListenerContainer createMessageListenerContainer(final String queue,
        final Object messageListener,
        final String messageSelector) {
        final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setErrorHandler(this);
        container.setExceptionListener(this);
        if (!Strings.isNullOrEmpty(queue)) {
            container.setDestination(new ActiveMQQueue(queue));
        }
        if (!Strings.isNullOrEmpty(messageSelector)) {
            container.setMessageSelector(messageSelector);
        }
        container.setMessageListener(messageListener);
        container.setAutoStartup(false);
        container.setConcurrency(concurencyListener);
        container.setTransactionManager(transactionManager);
        return container;
    }

    private void sendResponse(@Nonnull final ResponseMessage responseMessage, final Session session)
            throws JMSException {
        final Queue queue = session.createQueue(JmsConstants.JMS_QUEUE_IN_NAME);
        final MessageProducer producer = session.createProducer(queue);
        producer.send(JmsHelper.convertFrom(responseMessage, session));
        publish(new EventBackendReceived<>(this, responseMessage));
        producer.close();
    }

    private void publish(@Nonnull final EventBackendReceived<?> event) {
        if (this.eventPublisher != null) {
            eventPublisher.publish(event);
        }
    }

    /**
     * Jms Listener on answer on SubmitRequest
     */
    private class ReceiveMessageSentListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            try {
                final String messageType = message.getStringProperty("messageType");
                switch (messageType) {
                    case JmsConstants.MESSAGE_TYPE_RESPONSE_SUBMIT:
                        final SubmitResponse submitResponse = convertToSubmitResponse(message);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Receiver  submitResponse:{}", submitResponse);
                        }
                        publish(new EventBackendReceived<>(this, submitResponse));
                        break;
                    case JmsConstants.MESSAGE_TYPE_MESSAGE_SENT:
                        final MessageSent response = convertToMessageSent(message);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Receiver MessageSent receive:{}", response);
                        }
                        publish(new EventBackendReceived<>(this, response));
                        break;
                    default:
                        break;
                }

            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }

    /**
     * Jms listener on error notification
     */
    private class ReceiveMessageSendFailureListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            try {
                final MessageSendFailure response = convertToMessageSendFailure(message);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Receiver MessageSendFailure receive:{}", response);
                }
                publish(new EventBackendReceived<>(this, response));
            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }

    /**
     * Jms listener when receiver returns a error
     */
    private class ReceiveMessageReceiveFailureListener implements MessageListener {

        @Override
        public void onMessage(final Message message) {
            try {
                final MessageReceiveFailure response = convertToMessageReceiveFailure(message);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Receiver MessageReceiveFailure receive:{}", response);
                }
                publish(new EventBackendReceived<>(this, response));
            } catch (final JMSException ex) {
                throw JmsUtils.convertJmsAccessException(ex);
            }
        }

    }
}
