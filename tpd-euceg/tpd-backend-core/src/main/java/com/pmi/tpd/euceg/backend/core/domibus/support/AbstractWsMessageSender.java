package com.pmi.tpd.euceg.backend.core.domibus.support;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;
import static com.pmi.tpd.euceg.backend.core.message.MessageHelper.createSubmitMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.common.logging.Slf4jLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.api.scheduler.IScheduledJobSource;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.euceg.api.BackendNotStartedException;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.ISender;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.SubmitMessage;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.internal.GracefullyScheduledFuture;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;

public abstract class AbstractWsMessageSender<REQUEST, RESPONSE> implements ISender<REQUEST> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWsMessageSender.class);

    /** */
    protected final I18nService i18nService;

    /** */
    @Nonnull
    private IApplicationProperties applicationProperties;

    /** */
    @Nullable
    private final IKeyManagerProvider keyManagerProvider;

    /** */
    @Nonnull
    private final IEventPublisher eventPublisher;

    /** */
    @Nonnull
    private final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator;

    /** */
    private IScheduledJobSource pendingScheduler;

    /** */
    @Nonnull
    private final IPendingMessageProvider pendingMessageProvider;

    /** */
    @Nullable
    private final ISchedulerService schedulerService;

    /** */
    private ScheduledExecutorService taskScheduler;

    /** */
    protected GracefullyScheduledFuture currentTask;

    /** */
    private BackendProperties backendProperties;

    /** */
    protected boolean started = false;

    /** */
    private boolean autoStartup = false;

    public AbstractWsMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IEventPublisher eventPublisher) {
        this(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher, null, null);
    }

    public AbstractWsMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties, @Nonnull final IEventPublisher eventPublisher,
            @Nullable final IKeyManagerProvider keyManagerProvider) {
        this(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher,
                keyManagerProvider, null);
    }

    public AbstractWsMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties, @Nonnull final IEventPublisher eventPublisher,
            @Nullable final IKeyManagerProvider keyManagerProvider,
            @Nullable final ISchedulerService schedulerService) {
        this.schedulerService = schedulerService;
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.keyManagerProvider = keyManagerProvider;
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.messageCreator = checkNotNull(messageCreator, "messageCreator");
        this.pendingMessageProvider = checkNotNull(pendingMessageProvider, "pendingMessageProvider");
    }

    @Nonnull
    protected final IEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    @Nonnull
    protected final ISenderMessageCreator<REQUEST, RESPONSE> getMessageCreator() {
        return messageCreator;
    }

    @Nullable
    protected final IKeyManagerProvider getKeyManagerProvider() {
        return keyManagerProvider;
    }

    public final void setBackendProperties(final BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    @Nonnull
    public final BackendProperties getBackendProperties() {
        if (this.backendProperties != null) {
            return this.backendProperties;
        }
        return applicationProperties.getConfiguration(BackendProperties.class);
    }

    @Override
    public final void setAutoStartup(final boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    @Override
    public final boolean isAutoStartup() {
        return autoStartup;
    }

    @Override
    public final void stop() {
        shutdown();
    }

    @Override
    public final boolean isRunning() {
        return started;
    }

    @Override
    public final void destroy() throws Exception {
        shutdown();
    }

    @Override
    public final void start() {
        state(!started, "WS Backend has already started");
        LOGGER.info("WS Backend Service is starting");
        final BackendProperties properties = getBackendProperties();
        System.setProperty("org.apache.cxf.stax.allowInsecureParser", Boolean.toString(properties.isTlsInsecure()));
        System.setProperty("org.apache.cxf.Logger", Slf4jLogger.class.getName());

        if (properties == null || !properties.isEnable()) {
            return;
        }
        try {
            if (schedulerService != null) {
                pendingScheduler = new WsMessageReceiverScheduler(this, applicationProperties);
                pendingScheduler.schedule(schedulerService);
            } else {
                final Duration pendingInterval = Duration.ofSeconds(properties.getWsOptions().getPendingInterval());
                taskScheduler = Executors.newScheduledThreadPool(1);
                currentTask = GracefullyScheduledFuture.scheduleWithFixedDelay(taskScheduler,
                    this::updatePendingMessage,
                    pendingInterval,
                    pendingInterval);
            }

            LOGGER.info("WS Backend Service started");
            this.started = true;
        } catch (final SchedulerServiceException ex) {
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
        }

    }

    @Override
    public void shutdown() {
        this.started = false;
        try {
            if (schedulerService != null && pendingScheduler != null) {
                pendingScheduler.unschedule(schedulerService);
            }
        } catch (final Exception ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
        try {
            if (currentTask != null) {
                currentTask.cancelAndBeSureOfTermination(false);
                taskScheduler.shutdown();
            }
        } catch (final Exception ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
        pendingScheduler = null;
        taskScheduler = null;
    }

    @Override
    public void healthCheck() throws Exception {
        getPendingMessages();
    }

    @EventListener
    public final void onDomibusConfigurationChangedEvent(
        @Nonnull final ConfigurationChangedEvent<BackendProperties> event) throws Exception {
        if (event == null || !event.isAssignable(BackendProperties.class)) {
            return;
        }
        if (started) {
            shutdown();
        }
        start();
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public final void send(@Nonnull final String messageId, @Nonnull final REQUEST payload)
            throws IOException, BackendException {
        this.send(messageId, payload, null);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public final void send(@Nonnull final String messageId,
        @Nonnull final REQUEST payload,
        final @Nullable Path workingDirectory) throws IOException, BackendException {
        checkHasText(messageId, "messageId");
        checkNotNull(payload, "payload");

        // submit request
        final DataSource ds = this.messageCreator.createRequestPayload(payload, workingDirectory);
        this.send(createSubmitMessage(getBackendProperties(), messageId, ds));
    }

    protected abstract void acknowledge(final @Nonnull String messageId);

    public final void updatePendingMessage() {
        if (!this.isRunning()) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduling pending messages: starting");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduling pending messages: Verify status of local pending message");
        }
        // step 1
        // publish rejected message ( error of transmission).

        // gets all messages waiting response
        final var pendings = this.pendingMessageProvider.getPendingMessageIds();
        // Check the status of transmission for all pending messages.
        for (final String messageId : pendings) {
            checkPendingMessage(messageId);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduling pending messages: Retrieve Reponse of pending message");
        }
        // step 2
        // Retrieve pending response from backend

        // Get pending messages from Domibus
        final var pendingMessagesIds = this.getPendingMessages();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduling pending messages id : {}", pendingMessagesIds);
        }
        for (final String pendingMessageId : pendingMessagesIds) {

            Response<RESPONSE> response = null;
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Get Response, pending message id : {}", pendingMessageId);
                }
                response = this.getResponse(pendingMessageId);
                final var conversionId = response.getConversationId();
                if (!this.pendingMessageProvider.isOwner(conversionId)) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("pending message id : {} is not recognized", conversionId);
                    }
                    continue;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("sender: reponse received {}", response);
                }
                eventPublisher.publish(new EventBackendReceived<>(this, response));
                this.acknowledge(pendingMessageId);
            } catch (final SOAPFaultException | EucegException e) {
                // Will be treated on next iteration
                LOGGER.warn("Retrieve Message '{}' has failed for unexpected error : {}",
                    pendingMessageId,
                    e.getMessage());
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scheduling pending messages: end");
        }
    }

    /**
     * @param messageId
     * @return
     */
    @Nonnull
    protected abstract TransmitStatus checkPendingMessage(@Nonnull String messageId);

    @Nonnull
    protected abstract Response<RESPONSE> getResponse(@Nonnull final String messageId);

    @Nonnull
    protected abstract Set<String> getPendingMessages() throws BackendNotStartedException;

    protected abstract void send(@Nonnull final SubmitMessage submitMessage);

    /**
     * check if backend service has started.
     *
     * @throws BackendNotStartedException
     *                                    if the service has not started.
     */
    public final void checkStarted() throws BackendNotStartedException {
        if (!started) {
            throw new BackendNotStartedException("Backend is not started");
        }
    }

}
