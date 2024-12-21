package com.pmi.tpd.euceg.backend.core.delivery;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;
import static com.pmi.tpd.api.util.FileUtils.deleteDirectory;
import static com.pmi.tpd.euceg.api.Eucegs.uuid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.eu.ceg.AppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.euceg.api.BackendNotStartedException;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.backend.core.BackendException;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.IBackendManager;
import com.pmi.tpd.euceg.backend.core.IEncryptionProvider;
import com.pmi.tpd.euceg.backend.core.ISender;
import com.pmi.tpd.euceg.backend.core.domibus.api.IClientRest;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.JmsMessageSender;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.ws.WsPluginMessageSender;
import com.pmi.tpd.euceg.backend.core.domibus.ws.WsMessageSender;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;
import com.pmi.tpd.euceg.backend.core.message.MessageCurrentStatus;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;
import com.pmi.tpd.euceg.backend.core.spi.ISenderMessageHandler;

public class DefaultDeliveryBackendManager implements IBackendManager {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDeliveryBackendManager.class);

    /** */
    private final IClientRest client;

    /** */
    @Nonnull
    protected final I18nService i18nService;

    /** */
    @Nonnull
    protected final IEventPublisher eventPublisher;

    /** */
    protected final Provider<ISchedulerService> schedulerServiceProvider;

    /** */
    protected final IApplicationConfiguration applicationConfiguration;

    /** */
    @Nonnull
    protected IApplicationProperties applicationProperties;

    /** */
    private final IKeyManagerProvider keyManagerProvider;

    /** */
    private final IEncryptionProvider encryptionProvider;

    /** */
    @Nonnull
    private final IPendingMessageProvider pendingMessageProvider;

    /** */
    private ISender<Object> sender;

    /** */
    private boolean senderCalled = false;

    /** */
    private ISenderMessageHandler<AppResponse> delegate;

    /** */
    private boolean started = false;

    /** */
    private boolean autoStartup = false;

    public DefaultDeliveryBackendManager(final IClientRest client,
            @Nonnull final Provider<ISchedulerService> schedulerServiceProvider,
            @Nonnull final IEventPublisher eventPublisher, @Nonnull final I18nService i18nService,
            @Nonnull final IEncryptionProvider encryptionProvider,
            @Nonnull final IApplicationConfiguration applicationConfiguration,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IPendingMessageProvider pendingMessageProvider) {
        this(client, schedulerServiceProvider, eventPublisher, i18nService, encryptionProvider,
                applicationConfiguration, applicationProperties, pendingMessageProvider, null);

    }

    public DefaultDeliveryBackendManager(final IClientRest client,
            @Nonnull final Provider<ISchedulerService> schedulerServiceProvider,
            @Nonnull final IEventPublisher eventPublisher, @Nonnull final I18nService i18nService,
            @Nonnull final IEncryptionProvider encryptionProvider,
            @Nonnull final IApplicationConfiguration applicationConfiguration,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IPendingMessageProvider pendingMessageProvider,
            @Nullable final IKeyManagerProvider keyManagerProvider) {
        this.client = checkNotNull(client, "client");
        this.schedulerServiceProvider = checkNotNull(schedulerServiceProvider, "schedulerService");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.encryptionProvider = checkNotNull(encryptionProvider, "encryptionProvider");
        this.keyManagerProvider = checkNotNull(keyManagerProvider, "keyManagerProvider");
        this.pendingMessageProvider = checkNotNull(pendingMessageProvider, "pendingMessageProvider");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.applicationConfiguration = checkNotNull(applicationConfiguration, "applicationConfiguration");

    }

    protected void initialize() {
        if (!senderCalled) {
            this.sender = createSender();

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
    public boolean isRunning() {
        return started && sender.isRunning();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        state(!isRunning(), "Backend Manager has already started");
        final BackendProperties properties = getBackendProperties();
        LOGGER.info("Backend Service is starting");
        if (properties == null || !properties.isEnable()) {
            LOGGER.info("Backend Service is disabled");
            return;
        }
        LOGGER.info("Backend Manager started");
        initialize();
        this.sender.start();
        if (!this.client.isStarted()) {
            this.client.start();
        }
        this.started = true;
    }

    @Override
    public void stop() {
        shutdown();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        state(isRunning(), "Backend Manager has not yet start");
        this.sender.shutdown();
        this.client.shutdown();
        this.started = false;
    }

    @EventListener
    public void onDomibusConfigurationChangedEvent(@Nonnull final ConfigurationChangedEvent<BackendProperties> event)
            throws Exception {
        if (event == null || !event.isAssignable(BackendProperties.class)) {
            return;
        }
        final BackendProperties properties = event.getNewConfiguration();
        if (started) {
            shutdown();
        }
        // start only if service is enable
        if (properties.isEnable()) {
            start();
        }
    }

    @EventListener()
    public void onMessage(final EventBackendReceived<?> event) {
        if (delegate == null) {
            return;
        }
        dispach(event.getMessage());
    }

    public ISender<Object> getSender() {
        return sender;
    }

    public void setSender(final ISender<Object> sender) {
        this.sender = sender;
        senderCalled = true;
    }

    @Override
    public ISenderMessageHandler<AppResponse> getMessageHandler() {
        return delegate;
    }

    @Override
    public void setMessageHandler(final ISenderMessageHandler<AppResponse> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void healthCheck(final boolean checkOnlyServer) throws Exception {

        final BackendProperties backendProperties = getBackendProperties();
        final String healthCheckUrl = backendProperties.getUrl();
        // TODO healthcheck should have a disabled status.
        if (!backendProperties.isEnable() || Strings.isNullOrEmpty(healthCheckUrl)) {
            return;
        }

        healthCheck(healthCheckUrl);

        if (checkOnlyServer) {
            return;
        }

        if (sender != null) {
            sender.healthCheck();
        }

    }

    @Override
    public void healthCheck(@Nonnull final String url) throws Exception {
        this.client.healthCheck(url);
    }

    @Override
    public void sendPayload(@Nonnull final String messageId, @Nonnull final Object payload)
            throws BackendNotStartedException {
        checkStarted();
        checkHasText(messageId, "messageId");
        checkNotNull(payload, "payload");

        Path workingDirectory = null;
        try {
            workingDirectory = Files.createTempDirectory(applicationConfiguration.getWorkingDirectory(), uuid());
            // submit request
            this.sender.send(messageId, payload, workingDirectory);
        } catch (final BackendException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new EucegException(
                    i18nService.createKeyedMessage("app.service.euceg.backend.encryptioncontent.failed"), e);
        } finally {
            // remove working directory and files
            if (workingDirectory != null) {
                deleteDirectory(workingDirectory);
            }
        }

    }

    protected ISender<Object> createSender() {
        final BackendProperties properties = getBackendProperties();
        switch (properties.getConnectionType()) {
            case Jms:
                return new JmsMessageSender<>(new DefaultDeliverySenderCreator(encryptionProvider, i18nService),
                        pendingMessageProvider, eventPublisher, applicationProperties);
            case WsPlugin:
                return new WsPluginMessageSender<>(new DefaultDeliverySenderCreator(encryptionProvider, i18nService),
                        pendingMessageProvider, i18nService, applicationProperties, eventPublisher, keyManagerProvider,
                        schedulerServiceProvider.get());
            case Ws:
                return new WsMessageSender<>(new DefaultDeliverySenderCreator(encryptionProvider, i18nService),
                        pendingMessageProvider, i18nService, applicationProperties, eventPublisher, keyManagerProvider,
                        schedulerServiceProvider.get());

            default:
                throw new IllegalArgumentException();
        }
    }

    protected BackendProperties getBackendProperties() {
        return applicationProperties.getConfiguration(BackendProperties.class);
    }

    /**
     * check if backend service has started.
     *
     * @throws BackendNotStartedException
     *                                    if the service has not started.
     */
    protected void checkStarted() throws BackendNotStartedException {
        if (!this.sender.isRunning()) {
            throw new BackendNotStartedException("Backend is not started");
        }
    }

    @SuppressWarnings("unchecked")
    protected void dispach(final IBackendMessage message) {
        if (delegate == null) {
            return;
        }
        if (message instanceof SubmitResponse) {
            delegate.handleSubmitResponse((SubmitResponse) message);
        } else if (message instanceof Response<?>) {
            delegate.handleResponse((Response<AppResponse>) message);
        } else if (message instanceof MessageReceiveFailure) {
            delegate.handleMessageReceiveFailure((MessageReceiveFailure) message);
        } else if (message instanceof MessageSendFailure) {
            delegate.handleMessageSendFailure((MessageSendFailure) message);
        } else if (message instanceof MessageCurrentStatus) {
            delegate.handleCurrentStatus((MessageCurrentStatus) message);
        } else if (message instanceof MessageSent) {
            delegate.handleMessageSent((MessageSent) message);
        } else {
            LOGGER.warn("unknown message {}", message);
        }
    }

}
