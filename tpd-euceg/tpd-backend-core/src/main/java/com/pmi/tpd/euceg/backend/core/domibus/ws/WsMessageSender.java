package com.pmi.tpd.euceg.backend.core.domibus.ws;

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
import eu.domibus.backend.ws.BackendService11;
import eu.domibus.backend.ws.ErrorResultImplArray;
import eu.domibus.backend.ws.GetErrorsRequest;
import eu.domibus.backend.ws.GetMessageErrorsFault;
import eu.domibus.backend.ws.LargePayloadType;
import eu.domibus.backend.ws.ListPendingMessagesResponse;
import eu.domibus.backend.ws.MessageStatus;
import eu.domibus.backend.ws.RetrieveMessageFault;
import eu.domibus.backend.ws.RetrieveMessageRequest;
import eu.domibus.backend.ws.RetrieveMessageResponse;
import eu.domibus.backend.ws.StatusFault;
import eu.domibus.backend.ws.StatusRequest;
import eu.domibus.backend.ws.SubmitMessageFault;
import eu.domibus.backend.ws.SubmitRequest;
import eu.domibus.backend.ws.message.CollaborationInfo;
import eu.domibus.backend.ws.message.From;
import eu.domibus.backend.ws.message.MessageInfo;
import eu.domibus.backend.ws.message.MessageProperties;
import eu.domibus.backend.ws.message.Messaging;
import eu.domibus.backend.ws.message.PartInfo;
import eu.domibus.backend.ws.message.PartProperties;
import eu.domibus.backend.ws.message.PartyId;
import eu.domibus.backend.ws.message.PartyInfo;
import eu.domibus.backend.ws.message.PayloadInfo;
import eu.domibus.backend.ws.message.Property;
import eu.domibus.backend.ws.message.Service;
import eu.domibus.backend.ws.message.To;
import eu.domibus.backend.ws.message.UserMessage;

@Transactional
public class WsMessageSender<REQUEST, RESPONSE> extends AbstractWsMessageSender<REQUEST, RESPONSE> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsMessageSender.class);

    /** */
    private BackendInterface backendInterface;

    public WsMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties,
            @Nonnull final IEventPublisher eventPublisher) {
        this(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher, null, null);
    }

    public WsMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
            @Nonnull final IPendingMessageProvider pendingMessageProvider, @Nonnull final I18nService i18nService,
            @Nonnull final IApplicationProperties applicationProperties, @Nonnull final IEventPublisher eventPublisher,
            @Nullable final IKeyManagerProvider keyManagerProvider) {
        this(messageCreator, pendingMessageProvider, i18nService, applicationProperties, eventPublisher,
                keyManagerProvider, null);
    }

    public WsMessageSender(@Nonnull final ISenderMessageCreator<REQUEST, RESPONSE> messageCreator,
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
            if (backendInterface != null && backendInterface instanceof Client) {
                ((Client) backendInterface).destroy();
            }
        } catch (final Exception ex) {
            LOGGER.warn(ex.getMessage(), ex);
        }
        backendInterface = null;
    }

    @Override
    public void healthCheck() throws Exception {
        getPendingMessages();
    }

    @Override
    protected TransmitStatus checkPendingMessage(final String messageId) {
        // Try to retrieve message from backend if exists
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
    protected void acknowledge(final String messageId) {
        // nothing
    }

    @Override
    protected Response<RESPONSE> getResponse(@Nonnull final String messageId) {
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

        final Object listPendingMessagesRequest = ""; // Need to exist and be empty
        final ListPendingMessagesResponse listPendingMessages = getOrCreateService()
                .listPendingMessages(listPendingMessagesRequest);
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
        eu.domibus.backend.ws.SubmitResponse response = null;
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
        final RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest().withMessageID(messageId);
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

    /**
     * @return Returns {@link BackendInterface} instance if exist or create.
     */
    @Nonnull
    protected synchronized BackendInterface getOrCreateService() {
        if (backendInterface == null) {
            backendInterface = createInterface();
        }
        return backendInterface;
    }

    @VisibleForTesting
    protected void setBackendInterface(final BackendInterface backendInterface) {
        this.backendInterface = backendInterface;
    }

    @Nonnull
    protected BackendInterface createInterface() {
        return new WebServiceFactory(getKeyManagerProvider(), getBackendProperties())
                .createInterface(BackendInterface.class, BackendService11.class);

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
        return null;
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
