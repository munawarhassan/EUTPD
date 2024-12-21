package com.pmi.tpd.euceg.backend.core.domibus.plugin.jms;

import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import com.google.common.io.ByteStreams;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.euceg.backend.core.message.MessageReceiveFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSendFailure;
import com.pmi.tpd.euceg.backend.core.message.MessageSent;
import com.pmi.tpd.euceg.backend.core.message.Payload;
import com.pmi.tpd.euceg.backend.core.message.PayloadByte;
import com.pmi.tpd.euceg.backend.core.message.PayloadDataSource;
import com.pmi.tpd.euceg.backend.core.message.PayloadFile;
import com.pmi.tpd.euceg.backend.core.message.SubmitResponse;

public class JmsHelper {

    public static Message convertFrom(final SubmitMessage submitMessage, final Session session) throws JMSException {
        final MapMessage messageMap = session.createMapMessage();
        // Declare message as submit
        messageMap.setStringProperty("messageType", submitMessage.getMessageType());
        // Set up the Communication properties for the message
        messageMap.setStringProperty("service", submitMessage.getService());
        messageMap.setStringProperty("serviceType", submitMessage.getServiceType());
        messageMap.setStringProperty("action", submitMessage.getAction());

        messageMap.setStringProperty("fromPartyId", submitMessage.getFromPartyId());
        messageMap.setStringProperty("fromPartyType", submitMessage.getFromPartyType());
        messageMap.setStringProperty("toPartyId", submitMessage.getToPartyId());
        messageMap.setStringProperty("toPartyType", submitMessage.getFromPartyType());
        messageMap.setStringProperty("fromRole", submitMessage.getFromRole());
        messageMap.setStringProperty("toRole", submitMessage.getToRole());
        messageMap.setStringProperty("originalSender", submitMessage.getOriginalSender());
        messageMap.setStringProperty("finalRecipient", submitMessage.getFinalRecipient());
        messageMap.setStringProperty("protocol", submitMessage.getProtocol());

        messageMap.setStringProperty("conversationId", submitMessage.getConversationId());
        messageMap.setStringProperty("refToMessageId", submitMessage.getRefToMessageId());
        messageMap.setStringProperty("messageId", submitMessage.getMessageId());

        messageMap.setJMSCorrelationID(submitMessage.getJmsCorrelationId());
        messageMap.setStringProperty("P1InBody", "true");
        // messageMap.setStringProperty("putAttachmentInQueue", "true");

        messageMap.setStringProperty("totalNumberOfPayloads", String.valueOf(submitMessage.getPayloads().size()));
        int index = 1;
        for (final Payload payload : submitMessage.getPayloads()) {
            final String prefix = MessageFormat.format("payload_{0}", index);
            messageMap.setStringProperty(prefix + "_mimeContentId", payload.getMimeContentId());
            messageMap.setStringProperty(prefix + "_mimeType", payload.getMimeType());
            messageMap.setStringProperty(prefix + "_fileName", payload.getFileName());
            messageMap.setStringProperty(prefix + "_PayloadName", payload.getName());
            messageMap.setBytes(prefix, toByteArray(payload));
            index++;
        }

        return messageMap;
    }

    public static Message convertFrom(final ResponseMessage responseMessage, final Session session)
            throws JMSException {
        final MapMessage messageMap = session.createMapMessage();
        // Declare message as submit
        messageMap.setStringProperty("messageType", responseMessage.getMessageType());
        // Set up the Communication properties for the message
        messageMap.setStringProperty("service", responseMessage.getService());
        messageMap.setStringProperty("serviceType", responseMessage.getServiceType());
        messageMap.setStringProperty("action", responseMessage.getAction());

        messageMap.setStringProperty("fromPartyId", responseMessage.getFromPartyId());
        messageMap.setStringProperty("fromPartyType", responseMessage.getFromPartyType());
        messageMap.setStringProperty("toPartyId", responseMessage.getToPartyId());
        messageMap.setStringProperty("toPartyType", responseMessage.getFromPartyType());
        messageMap.setStringProperty("fromRole", responseMessage.getFromRole());
        messageMap.setStringProperty("toRole", responseMessage.getToRole());
        messageMap.setStringProperty("originalSender", responseMessage.getOriginalSender());
        messageMap.setStringProperty("finalRecipient", responseMessage.getFinalRecipient());
        messageMap.setStringProperty("protocol", responseMessage.getProtocol());

        messageMap.setStringProperty("conversationId", responseMessage.getConversationId());
        messageMap.setStringProperty("refToMessageId", responseMessage.getRefToMessageId());
        messageMap.setStringProperty("messageId", responseMessage.getMessageId());

        messageMap.setStringProperty("P1InBody", "true");
        // messageMap.setStringProperty("putAttachmentInQueue", "true");

        messageMap.setStringProperty("totalNumberOfPayloads", String.valueOf(responseMessage.getPayloads().size()));
        int index = 1;
        for (final Payload payload : responseMessage.getPayloads()) {
            final String prefix = MessageFormat.format("payload_{0}", index);
            messageMap.setStringProperty(prefix + "_mimeContentId", payload.getMimeContentId());
            messageMap.setStringProperty(prefix + "_mimeType", payload.getMimeType());
            messageMap.setStringProperty(prefix + "_fileName", payload.getFileName());
            messageMap.setStringProperty(prefix + "_PayloadName", payload.getName());
            messageMap.setBytes(prefix, toByteArray(payload));
            index++;
        }

        return messageMap;
    }

    public static ResponseMessage convertToResponseMessage(final MapMessage message) throws JMSException {
        return ResponseMessage.builder()
                .action(message.getStringProperty("action"))
                .conversationId(message.getStringProperty("conversationId"))
                .finalRecipient(message.getStringProperty("finalRecipient"))
                .fromPartyId(message.getStringProperty("fromPartyId"))
                .fromPartyType(message.getStringProperty("fromPartyType"))
                .fromRole(message.getStringProperty("fromRole"))
                .messageId(message.getStringProperty("messageId"))
                .messageType(message.getStringProperty("messageType"))
                .originalSender(message.getStringProperty("originalSender"))
                .payloads(convertToPayload(message))
                .protocol(message.getStringProperty("protocol"))
                .refToMessageId(message.getStringProperty("refToMessageId"))
                .service(message.getStringProperty("service"))
                .serviceType(message.getStringProperty("serviceType"))
                .toPartyId(message.getStringProperty("toPartyId"))
                .toPartyType(message.getStringProperty("toPartyType"))
                .toRole(message.getStringProperty("toRole"))
                .build();
    }

    @SuppressWarnings("null")
    @Nonnull
    public static SubmitResponse convertToSubmitResponse(final Message message) throws JMSException {
        return SubmitResponse.builder()
                .correlationId(message.getJMSCorrelationID())
                .messageId(message.getStringProperty("messageId"))
                .errorMessage(message.getStringProperty("ErrorMessage"))
                .build();
    }

    @SuppressWarnings("null")
    @Nonnull
    public static MessageSent convertToMessageSent(final Message message) throws JMSException {
        return MessageSent.builder()
                .correlationId(message.getJMSCorrelationID())
                .messageId(message.getStringProperty("messageId"))
                .build();
    }

    @Nonnull
    public static MessageReceiveFailure convertToMessageReceiveFailure(final Message message) throws JMSException {
        return MessageReceiveFailure.builder()
                .status(TransmitStatus.REJECTED)
                .messageId(message.getStringProperty("messageId"))
                .errorCode(message.getStringProperty("errorCode"))
                .errorDetail(message.getStringProperty("errorDetail"))
                .endPoint(message.getStringProperty("endPoint"))
                .build();
    }

    @Nonnull
    public static MessageSendFailure convertToMessageSendFailure(final Message message) throws JMSException {
        return MessageSendFailure.builder()
                .status(TransmitStatus.REJECTED)
                .messageId(message.getStringProperty("messageId"))
                .errorCode(message.getStringProperty("errorCode"))
                .errorDetail(message.getStringProperty("errorDetail"))
                .build();
    }

    @SuppressWarnings("null")
    @Nonnull
    public static List<Payload> convertToPayload(@Nonnull final MapMessage message) throws JMSException {
        final int totalNumberOfPayloads = message.getIntProperty("totalNumberOfPayloads");
        return IntStream.rangeClosed(1, totalNumberOfPayloads).mapToObj(i -> {
            try {
                final String prefix = MessageFormat.format("payload_{0}", i);
                return PayloadByte.builder()
                        .name(message.getStringProperty(prefix + "_name"))
                        .fileName(message.getStringProperty(prefix + "_fileName"))
                        .mimeContentId(message.getStringProperty(prefix + "_mimeContentId"))
                        .mimeType(message.getStringProperty(prefix + "_mimeType"))
                        .content(message.getBytes(prefix))
                        .build();
            } catch (final JMSException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }).collect(Collectors.toList());

    }

    @Nonnull
    public static IncomingMessage convertToincomingMessage(final MapMessage message) throws JMSException {
        return IncomingMessage.builder()
                .action(message.getStringProperty("action"))
                .agreementRefType(message.getStringProperty("agreementRefType"))
                .agreementRef(message.getStringProperty("agreementRef"))
                .conversationId(message.getStringProperty("conversationId"))
                .finalRecipient(message.getStringProperty("finalRecipient"))
                .fromPartyId(message.getStringProperty("fromPartyId"))
                .fromPartyType(message.getStringProperty("fromPartyType"))
                .fromRole(message.getStringProperty("fromRole"))
                .messageId(message.getStringProperty("messageId"))
                .messageType(message.getStringProperty("messageType"))
                .originalSender(message.getStringProperty("originalSender"))
                .payloads(convertToPayload(message))
                .protocol(message.getStringProperty("protocol"))
                .refToMessageId(message.getStringProperty("refToMessageId"))
                .service(message.getStringProperty("service"))
                .serviceType(message.getStringProperty("serviceType"))
                .toPartyId(message.getStringProperty("toPartyId"))
                .toPartyType(message.getStringProperty("toPartyType"))
                .toRole(message.getStringProperty("toRole"))
                .build();
    }

    private static byte[] toByteArray(final Payload payload) {
        byte[] bytes = null;
        if (payload instanceof PayloadByte) {
            bytes = ((PayloadByte) payload).getContent();
        } else if (payload instanceof PayloadFile) {
            final File file = ((PayloadFile) payload).getContent();
            if (file != null) {
                try {
                    bytes = readAllBytes(file.toPath());
                } catch (final IOException ex) {
                    throw new RuntimeException(ex.getLocalizedMessage(), ex);
                }
            }
        } else if (payload instanceof PayloadDataSource) {
            final DataSource source = ((PayloadDataSource) payload).getContent();
            if (source != null) {
                try {
                    bytes = ByteStreams.toByteArray(source.getInputStream());
                } catch (final IOException ex) {
                    throw new RuntimeException(ex.getLocalizedMessage(), ex);
                }
            }
        }
        return bytes;
    }
}
