package com.pmi.tpd.euceg.backend.core.domibus.plugin.ws;

import static com.pmi.tpd.api.util.Assert.checkHasText;

import java.text.MessageFormat;
import java.util.Set;

import javax.activation.DataHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.ws.Holder;

import org.apache.cxf.endpoint.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.scheduler.ISchedulerService;
import com.pmi.tpd.euceg.api.BackendNotStartedException;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.ISenderMessageCreator;
import com.pmi.tpd.euceg.backend.core.domibus.plugin.jms.SubmitMessage;
import com.pmi.tpd.euceg.backend.core.domibus.support.AbstractWsMessageSender;
import com.pmi.tpd.euceg.backend.core.event.EventBackendReceived;
import com.pmi.tpd.euceg.backend.core.internal.WebServiceFactory;
import com.pmi.tpd.euceg.backend.core.message.MessageCurrentStatus;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Payload;
import com.pmi.tpd.euceg.backend.core.message.Response;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;

import eu.domibus.backend.ws.BackendInterface;
import eu.domibus.plugin.ws.GetMessageErrorsFault;
import eu.domibus.plugin.ws.ListPendingMessagesFault;
import eu.domibus.plugin.ws.MarkMessageAsDownloadedFault;
import eu.domibus.plugin.ws.RetrieveMessageFault;
import eu.domibus.plugin.ws.StatusFault;
import eu.domibus.plugin.ws.SubmitMessageFault;
import eu.domibus.plugin.ws.WebServicePlugin;
import eu.domibus.plugin.ws.WebServicePluginInterface;
import eu.domibus.plugin.ws.message.CollaborationInfo;
import eu.domibus.plugin.ws.message.ErrorResultImplArray;
import eu.domibus.plugin.ws.message.From;
import eu.domibus.plugin.ws.message.GetErrorsRequest;
import eu.domibus.plugin.ws.message.LargePayloadType;
import eu.domibus.plugin.ws.message.ListPendingMessagesRequest;
import eu.domibus.plugin.ws.message.ListPendingMessagesResponse;
import eu.domibus.plugin.ws.message.MarkMessageAsDownloadedRequest;
import eu.domibus.plugin.ws.message.MarkMessageAsDownloadedResponse;
import eu.domibus.plugin.ws.message.MessageInfo;
import eu.domibus.plugin.ws.message.MessageProperties;
import eu.domibus.plugin.ws.message.MessageStatus;
import eu.domibus.plugin.ws.message.Messaging;
import eu.domibus.plugin.ws.message.PartInfo;
import eu.domibus.plugin.ws.message.PartProperties;
import eu.domibus.plugin.ws.message.PartyId;
import eu.domibus.plugin.ws.message.PartyInfo;
import eu.domibus.plugin.ws.message.PayloadInfo;
import eu.domibus.plugin.ws.message.Property;
import eu.domibus.plugin.ws.message.RetrieveMessageRequest;
import eu.domibus.plugin.ws.message.RetrieveMessageResponse;
import eu.domibus.plugin.ws.message.Service;
import eu.domibus.plugin.ws.message.StatusRequest;
import eu.domibus.plugin.ws.message.SubmitRequest;
import eu.domibus.plugin.ws.message.To;
import eu.domibus.plugin.ws.message.UserMessage;

@Transactional
public class WsPluginMessageSender<REQUEST, RESPONSE> extends AbstractWsMessageSender<REQUEST, RESPONSE> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsPluginMessageSender.class);

    /** */
    private WebServicePluginInterface interfaceWs;

    public WsPluginMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IEventPublisher eventPublisher) {
        this(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher, null, null);
    }

    public WsPluginMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties, @Nonnull final IEventPublisher eventPublisher,
            @Nullable final IKeyManagerProvider keyManagerProvider) {
        this(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher,
                keyManagerProvider, null);
    }

    public WsPluginMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties, @Nonnull final IEventPublisher eventPublisher,
            @Nullable final IKeyManagerProvider keyManagerProvider,
            @Nullable final ISchedulerService schedulerService) {
        super(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher,
                keyManagerProvider, schedulerService);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            if (interfaceWs != null && interfaceWs instanceof Client) {
                ((Client) interfaceWs).destroy();
            }
        } catch (final Exception ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
        interfaceWs = null;
    }

    @Override
    public void healthCheck() throws Exception {
        getPendingMessages();
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    protected void acknowledge(@Nonnull final String messageId) {
        checkHasText(messageId, "messageId");
        this.markMessageAsDownloaded(messageId);
    }

    @Override
    protected @Nonnull TransmitStatus checkPendingMessage(final @Nonnull String messageId) {
        // Try to retrieve message from Domibus if exists
        final MessageStatus messageStatus = this.getMessageStatus(messageId);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Check message {} status: {}", messageId, messageStatus);
        }
        // Convert message status to Transmit status.
        final TransmitStatus status = from(messageStatus);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Convert status '{}' to Transmit status: {}", messageStatus, status);
        }
        // update only if REJECTED
        if (TransmitStatus.REJECTED.equals(status)) {
            EventBackendReceived<?> event = null;
            switch (messageStatus) {
                case SEND_FAILURE:
                    event = new EventBackendReceived<>(this,
                            MessageSendFailure.builder().status(status).messageId(messageId).build());
                    break;
                case SEND_ATTEMPT_FAILED:
                default:
                    event = new EventBackendReceived<>(this,
                            MessageReceiveFailure.builder().status(status).messageId(messageId).build());
                    break;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Publish rejected event: {}", event);
            }
            getEventPublisher().publish(event);
        } else {
            // publish current status
            getEventPublisher().publish(new EventBackendReceived<>(this,
                    MessageCurrentStatus.builder().messageId(messageId).status(status).build()));
        }
        return status;
    }

    @Nonnull
    protected MessageStatus getMessageStatus(@Nonnull final String messageId) {
        checkStarted();

        checkHasText(messageId, "messageId");
        final StatusRequest request = new StatusRequest().withMessageID(messageId);
        try {
            return getOrCreateService().getStatus(request);
        } catch (final StatusFault e) {
            LOGGER.error("Error while get message status", e);
            throw new EucegException(
                    i18nService.createKeyedMessage("app.service.euceg.backend.message-status.failed", messageId), e);
        }

    }

    @Override
    protected @Nonnull Response<RESPONSE> getResponse(@Nonnull final String messageId) {
        checkStarted();
        checkHasText(messageId, "messageId");
        final RetrieveMessageResponseWrapper messageResponse = this.getMessage(messageId);
        final LargePayloadType payloadType = messageResponse.getMessageResponse()
                .getPayload()
                .stream()
                .findFirst()
                .orElseThrow();

        final RESPONSE response = getMessageCreator().createPayloadResponse(payloadType.getValue().getDataSource());
        return Response.<RESPONSE> builder()
                .conversationId(messageResponse.getConversationId())
                .response(response)
                .build();

    }

    @Override
    @Nonnull
    protected Set<String> getPendingMessages() throws BackendNotStartedException {
        checkStarted();

        // TODO add final partyID filter
        final ListPendingMessagesRequest listPendingMessagesRequest = new ListPendingMessagesRequest();
        ListPendingMessagesResponse listPendingMessages = null;
        try {
            listPendingMessages = getOrCreateService().listPendingMessages(listPendingMessagesRequest);
        } catch (final ListPendingMessagesFault ex) {
            LOGGER.error("pending message has failed", ex);
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.backend.pendingmessage.failed"),
                    ex);
        }
        return Sets.newHashSet(listPendingMessages.getMessageID());

    }

    @Nullable
    protected ErrorResultImplArray getError(@Nonnull final String messageId) throws GetMessageErrorsFault {
        checkStarted();
        checkHasText(messageId, "messageId");

        final GetErrorsRequest getErrorsRequest = new GetErrorsRequest().withMessageID(messageId);
        final ErrorResultImplArray messageErrors = getOrCreateService().getMessageErrors(getErrorsRequest);
        if (messageErrors == null) {
            return null;
        }
        return messageErrors;

    }

    @Override
    protected void send(@Nonnull final SubmitMessage submitMessage) {
        checkStarted();
        final String messageId = submitMessage.getMessageId();
        final Payload payload = submitMessage.getPayloads().stream().findFirst().orElseThrow();
        eu.domibus.plugin.ws.message.SubmitResponse response = null;
        try {
            final SubmitRequest request = new SubmitRequest()
                    .withPayload(new LargePayloadType().withPayloadId(payload.getMimeContentId())
                            .withContentType(payload.getMimeType())
                            .withValue(new DataHandler(payload.getDataSource())));
            final Messaging messaging = createMessaging(submitMessage, payload);
            response = getOrCreateService().submitMessage(request, messaging);

            // publish response
            getEventPublisher().publish(new EventBackendReceived<>(this,
                    SubmitResponse.builder().correlationId(messageId).messageId(messageId).build()));
            // publish send message
            getEventPublisher().publish(new EventBackendReceived<>(this,
                    MessageSent.builder().messageId(messageId).correlationId(messageId).build()));
        } catch (final SubmitMessageFault ex) {
            getEventPublisher().publish(new EventBackendReceived<>(this,
                    SubmitResponse.builder()
                            .errorMessage(MessageFormat.format("Error: {0}, message: {1}",
                                ex.getFaultInfo().getCode(),
                                ex.getFaultInfo().getMessage()))
                            .correlationId(messageId)
                            .messageId(messageId)
                            .build()));

        }

    }

    /**
     * Gets the message for the specific {@code messageId}.
     *
     * @param messageId
     *                  the identifier of message to retrieve.
     * @return Returns a instance of {@link RetrieveMessageResponseWrapper} representing the message.
     */
    @Nonnull
    protected RetrieveMessageResponseWrapper getMessage(@Nonnull final String messageId) {
        checkStarted();

        checkHasText(messageId, "messageId");
        final RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest().withMessageID(messageId)
                .withMarkAsDownloaded(Boolean.toString(false));
        final Holder<RetrieveMessageResponse> downloadMessageResponse = new Holder<>();
        final Holder<Messaging> ebMSHeaderInfo = new Holder<>();
        try {
            getOrCreateService().retrieveMessage(retrieveMessageRequest, downloadMessageResponse, ebMSHeaderInfo);
            final String conversationId = ebMSHeaderInfo.value.getUserMessage()
                    .getCollaborationInfo()
                    .getConversationId();
            return new RetrieveMessageResponseWrapper(downloadMessageResponse.value, conversationId);
        } catch (final RetrieveMessageFault e) {
            LOGGER.error("download message " + messageId + " has failed", e);
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.backend.downloadmessage.failed"),
                    e);
        }

    }

    protected void markMessageAsDownloaded(@Nonnull final String messageId) {
        checkStarted();

        checkHasText(messageId, "messageId");
        try {
            final MarkMessageAsDownloadedRequest asDownloadedRequest = new MarkMessageAsDownloadedRequest()
                    .withMessageID(messageId);
            final Holder<MarkMessageAsDownloadedResponse> holder = new Holder<>();
            final Holder<Messaging> ebMSHeaderInfo = new Holder<>();
            getOrCreateService().markMessageAsDownloaded(asDownloadedRequest, holder, ebMSHeaderInfo);
        } catch (final MarkMessageAsDownloadedFault e) {
            LOGGER.error("Mark message " + messageId + " as downloaded has failed", e);
            throw new EucegException(i18nService.createKeyedMessage("app.service.euceg.backend.downloadmessage.failed"),
                    e);
        }

    }

    /**
     * @return Returns {@link BackendInterface} instance if exist or create.
     */
    @Nonnull
    protected synchronized WebServicePluginInterface getOrCreateService() {
        if (interfaceWs == null) {
            interfaceWs = createInterface();
        }
        return interfaceWs;
    }

    @VisibleForTesting
    protected void setBackendInterface(final WebServicePluginInterface interfaceWs) {
        this.interfaceWs = interfaceWs;
    }

    @Nonnull
    protected WebServicePluginInterface createInterface() {
        return new WebServiceFactory(getKeyManagerProvider(), getBackendProperties())
                .createInterface(WebServicePluginInterface.class, WebServicePlugin.class);
    }

    private static PartyId partyId(final String partyType, final String value) {
        return new PartyId().withType(partyType).withValue(value);
    }

    private static PartyInfo partyInfo() {
        return new PartyInfo();
    }

    private static Property property(final String name, final String value) {
        return new Property().withName(name).withValue(value);
    }

    private static Service service(final String type, final String value) {
        return new Service().withType(type).withValue(value);
    }

    @Nonnull
    private static TransmitStatus from(@Nonnull final MessageStatus messageStatus) {
        switch (messageStatus) {
            case READY_TO_SEND:
            case SEND_ENQUEUED:
            case SEND_IN_PROGRESS:
            case WAITING_FOR_RECEIPT:
            case WAITING_FOR_RETRY:
            case ACKNOWLEDGED:
            case ACKNOWLEDGED_WITH_WARNING:
                return TransmitStatus.PENDING;
            case RECEIVED:
            case RECEIVED_WITH_WARNINGS:
                return TransmitStatus.RECEIVED;
            case SEND_ATTEMPT_FAILED:
            case SEND_FAILURE:
            case NOT_FOUND:
                return TransmitStatus.REJECTED;
            case DELETED:
                return TransmitStatus.DELETED;
            default:
                break;
        }
        throw new IllegalArgumentException("messageStatus");
    }

    /**
     * <p>
     * create user message.
     * </p>
     * <p>
     * <b>note</b>: the message id is used also as conversation id to facilitate the reception of response.
     * </p>
     *
     * @param submitMessage
     *                      the message
     * @return Returns a new instance of {@link Messaging} containing only one {@link UserMessage}.
     * @see SubmissionService#updatePendingMessage()
     */
    private Messaging createMessaging(final SubmitMessage submitMessage, final Payload payload) {
        return new Messaging()
                .withUserMessage(
                    new UserMessage()
                            .withPayloadInfo(
                                new PayloadInfo().withPartInfo(new PartInfo().withHref(payload.getMimeContentId())
                                        .withPartProperties(new PartProperties()
                                                .withProperty(property("MimeType", payload.getMimeType())))))
                            .withMessageInfo(new MessageInfo().withMessageId(submitMessage.getMessageId())
                                    .withRefToMessageId(submitMessage.getRefToMessageId()))
                            .withCollaborationInfo(new CollaborationInfo()
                                    .withConversationId(submitMessage.getConversationId())
                                    .withService(service(submitMessage.getServiceType(), submitMessage.getService()))
                                    .withAction(submitMessage.getAction()))
                            .withMessageProperties(new MessageProperties()
                                    .withProperty(property("originalSender", submitMessage.getOriginalSender()),
                                        property("finalRecipient", submitMessage.getFinalRecipient())))
                            .withPartyInfo(partyInfo()
                                    .withFrom(new From().withRole(submitMessage.getFromRole())
                                            .withPartyId(partyId(submitMessage.getFromPartyType(),
                                                submitMessage.getFromPartyId())))
                                    .withTo(new To().withRole(submitMessage.getToRole())
                                            .withPartyId(partyId(submitMessage.getToPartyType(),
                                                submitMessage.getToPartyId())))));
    }

    /**
     * Wrapper for {@link DownloadMessageResponse response} allowing to associate the {@code conversationId}.
     *
     * @author Christophe Friederich
     */
    protected static class RetrieveMessageResponseWrapper {

        /** */
        private final RetrieveMessageResponse retrieveMessageResponse;

        /** */
        private final String conversationId;

        /**
         * Default constructor.
         *
         * @param retrieveMessageResponse
         *                                a downloaded message response.
         * @param conversationId
         *                                the identifier of response.
         */
        public RetrieveMessageResponseWrapper(final RetrieveMessageResponse retrieveMessageResponse,
                final String conversationId) {
            this.retrieveMessageResponse = retrieveMessageResponse;
            this.conversationId = conversationId;
        }

        /**
         * @return Returns {@link RetrieveMessageResponse} instance representing the downloaded message response.
         */
        public RetrieveMessageResponse getMessageResponse() {
            return retrieveMessageResponse;
        }

        /**
         * @return Returns a {@link String} representing the idenfier of response.
         */
        public String getConversationId() {
            return conversationId;
        }

    }

}
